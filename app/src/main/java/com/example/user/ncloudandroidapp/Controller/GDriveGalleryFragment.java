package com.example.user.ncloudandroidapp.Controller;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.GDriveRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.CustomDateFormat;
import com.example.user.ncloudandroidapp.ItemDecorationAlbumColumns;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.GalleryItems;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.OAuthHelper;
import com.example.user.ncloudandroidapp.OAuthServerIntf;
import com.example.user.ncloudandroidapp.R;
import com.example.user.ncloudandroidapp.RetrofitBuilder;
import com.example.user.ncloudandroidapp.Room.DownloadFile;
import com.example.user.ncloudandroidapp.Room.FileDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.NonNull;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GDriveGalleryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GDriveGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GDriveGalleryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OAuthHelper mOAuthHelper;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int INCOMPLETE = 308;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int GATEWAY_TIMEOUT = 504;

    private final String TAG = getClass().getSimpleName();

    private static final String FIELDS = "nextPageToken, files/id, files/name, files/mimeType, files/thumbnailLink, files/createdTime";
    private static final String Q = "mimeType contains 'image' and trashed = false";
    private static String ORDER = "createdTime desc";
    private static final Integer PAGE_SIZE = 100;
    private String PAGE_TOKEN = null;

    private static final int DEFAULT_SPAN_COUNT = 3;
    private GDriveRecyclerViewAdapter mAdapter;
    private CustomDateFormat mCustomDateFormat = new CustomDateFormat();
    protected GridLayoutManager gridLayoutManager;

    private static OAuthServerIntf server;
    private String currentDate;

    private boolean isLastPage = false;
    private boolean isLoading = false;

    public final int ORDER_CREATED_DESC = 1;
    public final int ORDER_CREATED_ASC = 2;
    public final int ORDER_MODEFIED_DESC = 3;
    public final int ORDER_MODEFIED_ASC = 4;

    @BindView(R.id.gdrive_fragment_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.main_fragment_text)
    TextView mTextView;

    @BindView(R.id.swipeRefreshGdrive)
    SwipeRefreshLayout mSwipeRefreshLayout;


    public GDriveGalleryFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GDriveGalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GDriveGalleryFragment newInstance(String param1, String param2) {
        GDriveGalleryFragment fragment = new GDriveGalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOAuthHelper = new OAuthHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gdrive_gallery, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light)
        );

        server = RetrofitBuilder.getOAuthClient(getActivity());

        gridLayoutManager = new GridLayoutManager(getActivity(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        mRecyclerView.addItemDecoration(new ItemDecorationAlbumColumns(getContext(), R.dimen.gallery_item_spacing));


        mAdapter = new GDriveRecyclerViewAdapter(getActivity(), gridLayoutManager, DEFAULT_SPAN_COUNT, false);
        mRecyclerView.setAdapter(mAdapter);

        startList();
        // Inflate the layout for this fragment

        return view;
    }




    public void setOrderByNum(int order){
        switch (order){
            case ORDER_CREATED_DESC:
                setORDER("createdTime desc");
                onRefresh();
                break;

            case ORDER_CREATED_ASC:
                setORDER("createdTime asc");
                onRefresh();
                break;

            case ORDER_MODEFIED_DESC:
                setORDER("modifiedTime desc");
                onRefresh();
                break;

            case ORDER_MODEFIED_ASC:
                setORDER("modifiedTime asc");
                onRefresh();
                break;
        }
    }
    public static void setORDER(String ORDER) {
        GDriveGalleryFragment.ORDER = ORDER;
    }



    private void startList() {
        PAGE_TOKEN = null;
        //OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getActivity());
        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER, PAGE_SIZE, PAGE_TOKEN);
        galleryItemCall.enqueue(findFirstImagesCallback);
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefreshLayout.setOnRefreshListener(listener);
    }


    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                mAdapter.clearStateArray();
                startList();
                //mRecyclerView.setAdapter(mAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private Callback<GalleryItems> findFirstImagesCallback = new Callback<GalleryItems>() {

        @Override
        public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response) {
            GalleryItems galleryItems = response.body();
            isLoading = false;

            if (response.code() == OK && galleryItems != null) {
                mTextView.setVisibility(View.GONE);

                PAGE_TOKEN = galleryItems.getNextPageToken();
                List<GalleryItem> items = galleryItems.getFiles();
                if (items != null) {
                    // Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();

                    if (items.size() > 0) {
                        currentDate = items.get(0).getCreatedTime();
                        mAdapter.add(new HeaderItem(mCustomDateFormat.dateFormatting(currentDate, Item.HEADER_ITEM_TYPE)));
                        configViews(items);
                    }
                    if (items.size() >= PAGE_SIZE) {
                        mAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }

                }

            } else if (response.code() == BAD_REQUEST) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
            } else if (response.code() == UNAUTHORIZED) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
            } else if (response.code() == FORBIDDEN) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
            } else if (response.code() == NOT_FOUND) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
            } else if (response.code() == GATEWAY_TIMEOUT) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_504), Toast.LENGTH_SHORT).show();
            } else if (response.code() == OK && galleryItems == null) {
                mTextView.setVisibility(View.VISIBLE);
                //mTextView.bringToFront();
            }
        }

        @Override
        public void onFailure(Call<GalleryItems> call, Throwable t) {
            Log.e(TAG, "The call listFilesCall failed", t);
        }


    };

    private Callback<GalleryItems> findNextImagesCallback = new Callback<GalleryItems>() {

        @Override
        public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response) {
            GalleryItems galleryItems = response.body();
            mAdapter.removeFooter();
            isLoading = false;

            if (response.code() == 200 && galleryItems != null) {
                PAGE_TOKEN = galleryItems.getNextPageToken();
                List<GalleryItem> items = galleryItems.getFiles();
                // mProgressBar.setVisibility(View.GONE);

                if (items != null) {
                    //  Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();
                    if (items.size() > 0)
                        configViews(items);
                    if (items.size() >= PAGE_SIZE) {
                        mAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }

            } else if (response.code() == 400) {
                isLastPage = true;
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 401) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 403) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 404) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 504) {
                Toast.makeText(getActivity(), response.message() + "\r\n" + getString(R.string.http_code_504), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<GalleryItems> call, Throwable t) {
            Log.e(TAG, "The call listFilesCall failed", t);
        }

    };


    private void configViews(List<GalleryItem> items) {

        List<Item> mItemList = new ArrayList<>();
        mItemList.clear();

        for (int i = 0; i < items.size(); i++) {
            GalleryItem galleryItem = items.get(i);
            //이전 것과 compare 했을 때, 날짜가 같지 않다면 header 생성해주기
            if (mCustomDateFormat.compareTime(currentDate, galleryItem.getCreatedTime()) != 0) {
                currentDate = galleryItem.getCreatedTime();
                mItemList.add(new HeaderItem(mCustomDateFormat.dateFormatting(galleryItem.getCreatedTime(), Item.HEADER_ITEM_TYPE)));
            }
            mItemList.add(galleryItem);
        }

        mAdapter.addAll(mItemList);
    }

    // region Listeners
    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = gridLayoutManager.getChildCount();
            int totalItemCount = gridLayoutManager.getItemCount();
            int firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }
        }
    };

    public void changeMode(boolean mode){
        mAdapter.setModeChanged(mode);
    }

    public void clearCheckBoxes(){
        mAdapter.clearStateArray();
    }

    public void refresh(){
        mAdapter.notifyDataSetChanged();
    }

    protected void loadMoreItems() {
        isLoading = true;
        //OAuthServerIntf server = RetrofitBuilder.getOAuthClient(getActivity());
        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER, PAGE_SIZE, PAGE_TOKEN);
        galleryItemCall.enqueue(findNextImagesCallback);
    }

    public void moveToTopOfThePage() {
        gridLayoutManager.scrollToPositionWithOffset(0, 0);
    }



    public void downloadMultipleFiles(){
        HashMap<Integer, Boolean> itemStates = mAdapter.getItemStateArray();
        List<GalleryItem> galleryItemList = new ArrayList<>();

        Iterator<Integer> iterator = itemStates.keySet().iterator();

        while(iterator.hasNext()){

            int key = iterator.next();
            GalleryItem galleryItem = ((GalleryItem)mAdapter.getItem(key));
            galleryItem.setResult(Item.DOWNLOAD_BEFORE);
            galleryItemList.add(galleryItem);

            /*Call<ResponseBody> responseBodyCall = server.downloadFile(finalGalleryItem.getId());

            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {

                    if(response.isSuccessful()) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                boolean writtenToDisk = writeResponseBodyToDisk(response.body(), finalGalleryItem);
                                if(writtenToDisk){
                                    Date currentDate = new Date();
                                    DownloadFile downloadFile = new DownloadFile(finalGalleryItem.getName(), finalGalleryItem.getThumbnailLink(),mCustomDateFormat.DateToString(currentDate, Item.ROOM_ITEM_TYPE));
                                    FileDatabase.getDatabase(getContext()).getFileDao().insertDownloadFile(downloadFile);
                                }

                                return null;
                            }
                        }.execute();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });*/

        }

        mAdapter.clearStateArray();
        Intent intent = new Intent(getActivity(), DownloadResultActivity.class);
        intent.putParcelableArrayListExtra("DOWNLOAD_LIST", (ArrayList<? extends Parcelable>) galleryItemList);
        startActivity(intent);

/*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.clearStateArray();
                Intent intent = new Intent(getActivity(), DownloadResultActivity.class);
                startActivity(intent);
            }
        }, 5000);

*/
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, GalleryItem item) {

        try {
            //새로운 Directory 생성
            String file_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "GDRIVE" ;


            File dir = new File(file_url);
            //directory 없으면 생성
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = item.getName();
            String localPath = file_url + File.separator + fileName;

            //Local Path에 파일 생성

            File downloadFile = new File(localPath);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(downloadFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }


                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA,
                        downloadFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.MIME_TYPE, item.getMimeType());
                // values.put(MediaStore.Images.ImageColumns.ORIENTATION, galleryItem.getOrientation());
                //values.put(MediaStore.Images.Media.DATE_ADDED, galleryItem.getCreatedTime());
                getActivity().getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    protected void deleteItems() {

        HashMap<Integer, Boolean> itemStates = mAdapter.getItemStateArray();
        String checkedItemId;

        Iterator<Integer> iterator = itemStates.keySet().iterator();

        while(iterator.hasNext()){
            int key = iterator.next();
            checkedItemId = ((GalleryItem)mAdapter.getItem(key)).getId();

            Call<ResponseBody> responseBodyCall = server.deleteFile(checkedItemId);
            responseBodyCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 204) {
                        Log.i(TAG, "File deleted sucessfully!!");
                    } else {
                        Log.e(TAG, "error caused from deleting function");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "error");
                }
            });
        }

    }

    public void deleteImages(){
        AsyncTask<Void, Void, List<Item>> asyncTask = new DeleteImageTask();
        asyncTask.execute();
    }

    public class DeleteImageTask extends AsyncTask<Void, Void, List<Item>> {

        @Override
        protected void onPostExecute(List<Item> itemList){
            mAdapter.removeItems(itemList);
            mAdapter.clearStateArray();
        }

        @Override
        protected List<Item> doInBackground(Void... voids) {
            HashMap<Integer, Boolean> itemStates = mAdapter.getItemStateArray();
            String checkedItemId;
            final List<Item> deletedItems = new ArrayList<>();

                Iterator<Integer> iterator = itemStates.keySet().iterator();

                while (iterator.hasNext()) {
                    int key = iterator.next();

                    final GalleryItem item = ((GalleryItem) mAdapter.getItem(key));
                    checkedItemId = item.getId();


                    Call<ResponseBody> responseBodyCall = server.deleteFile(checkedItemId);
                    responseBodyCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            deletedItems.add(item);

                            if (response.code() == 204) {
                                Log.i(TAG, "File deleted sucessfully!!");
                            } else {
                                Log.e(TAG, "error caused from deleting function");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "error");
                        }
                    });
                }


                return deletedItems;

        }
    }

    public int getCheckCount(){
        return mAdapter.getItemStateArray().size();
    }

}
