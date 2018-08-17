package com.example.user.ncloudandroidapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.LocalRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalHeaderItem;
import com.example.user.ncloudandroidapp.Room.FileDatabase;
import com.example.user.ncloudandroidapp.Room.UploadFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalGalleryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocalGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalGalleryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public final String PARCELABLE_ARRAY_LIST = "UploadResultActivity.INTENT";

    public static final int CHUNK_LIMIT = 262144; // = (256*1024)

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int INCOMPLETE = 308;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int PAYMENT_REQUIRED = 402;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int GATEWAY_TIMEOUT = 504;

    private static String ORDER = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    public final int ORDER_CREATED_DESC = 1;
    public final int ORDER_CREATED_ASC = 2;
    public final int ORDER_MODEFIED_DESC = 3;
    public final int ORDER_MODEFIED_ASC = 4;

    private static final Integer PAGE_SIZE = 100;

    private boolean isLastPage = false;
    private boolean isLoading = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @BindView(R.id.local_fragment_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.local_fragment_text)
    TextView mTextView;

    @BindView(R.id.swipeRefreshLocal)
    SwipeRefreshLayout mSwipeRefreshLayout;

    LocalRecyclerViewAdapter mLocalRecyclerViewAdapter;
    protected GridLayoutManager gridLayoutManager;
    OAuthServerIntf server;

    private int PICK_IMAGE_REQUEST = 1;
    private String TAG = "LocalGalleryFragment";
    private static final int DEFAULT_SPAN_COUNT = 3;

    private Date compareDate = new Date();
    private Date date = new Date();
    // public static List<Item> sItemList = new ArrayList<>();

    private static ArrayList<LocalGalleryItem> uploadedItems = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS = 100;
    CustomDateFormat dateFormat = new CustomDateFormat();

    public LocalGalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LocalGalleryFragment.
     */

    public void setOrderByNum(int order){
        switch (order){
            case ORDER_CREATED_DESC:
                setORDER(MediaStore.Images.Media.DATE_TAKEN + " DESC");
                onRefresh();
                break;

            case ORDER_CREATED_ASC:
                setORDER(MediaStore.Images.Media.DATE_TAKEN + " ASC");
                onRefresh();
                break;

            case ORDER_MODEFIED_DESC:
                setORDER(MediaStore.Images.Media.DATE_MODIFIED + " DESC");
                onRefresh();
                break;

            case ORDER_MODEFIED_ASC:
                setORDER(MediaStore.Images.Media.DATE_MODIFIED + " DESC");
                onRefresh();
                break;
        }
    }
    public static void setORDER(String ORDER) {
        LocalGalleryFragment.ORDER = ORDER;
    }


    // TODO: Rename and change types and number of parameters
    public static LocalGalleryFragment newInstance(String param1, String param2) {
        LocalGalleryFragment fragment = new LocalGalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_gallery, container, false);
        ButterKnife.bind(this, view);


        mSwipeRefreshLayout.setOnRefreshListener(this);
        gridLayoutManager = new GridLayoutManager(getActivity(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        mLocalRecyclerViewAdapter = new LocalRecyclerViewAdapter(getActivity(), gridLayoutManager, DEFAULT_SPAN_COUNT, false);

        mRecyclerView.setAdapter(mLocalRecyclerViewAdapter);
        server = RetrofitBuilder.getOAuthClient(getActivity());
        permissionCheck();

        return view;
    }

    /*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }*/

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
       /* mAdapter.clear();
        mAdapter.notifyDataSetChanged();
        startList();
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setRefreshing(false);*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocalRecyclerViewAdapter.clear();
                mLocalRecyclerViewAdapter.notifyDataSetChanged();
                mLocalRecyclerViewAdapter.clearStateArray();
                permissionCheck();
                //mRecyclerView.setAdapter(mLocalRecyclerViewAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);

    }

    public void permissionCheck() {
        //갤러리 사용 권한 체크 ( 사용권한이 없을 경우 -1)
        if ((ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            //권한이 없을 경우

            //최소 권한 요청인지, 혹은 사용자에 의한 재 요청인지 확인
            if ((shouldShowRequestPermissionRationale(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                //사용자가 임의로 권한을 취소시킨 경우
                //Toast.makeText(getActivity(), "사용자에 의한 권한 취소", Toast.LENGTH_LONG).show();
            } else {
                //최초로 권한을 요청하는 경우 (첫 실행)
                //Fragment일 때 ActivityCompat.requestPermissions 말고requestPermissions를 사용해야함.
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
               /* ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                */
                //Toast.makeText(getActivity(), "최초 권한 요청.", Toast.LENGTH_LONG).show();

            }
        } else {
            //사용 권한이 있음을 확인하는 경우
            Log.e(TAG, "Permission Checked");
            //Toast.makeText(getActivity(), "권한 확인 완료.", Toast.LENGTH_LONG).show();
            getImages();
            //getImagePath();

        }
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefreshLayout.setOnRefreshListener(listener);
    }

    public void refresh() {
        mLocalRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void clearCheckBoxes() {
        mLocalRecyclerViewAdapter.clearStateArray();
    }

    public void changeMode(boolean mode) {
        mLocalRecyclerViewAdapter.setModeChanged(mode);
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

    public void getImages(){
        AsyncTask<Void, Integer, List<Item>> asyncTask = new DownloadImageTask();
        asyncTask.execute();

    }

    public List<Item> getImagePath() {


        List<Item> sItemList = new ArrayList<>();
        sItemList.clear();
        mLocalRecyclerViewAdapter.clear();
        boolean isFirstItem = true;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_date_taken, column_index_image_id, column_index_mime_type, column_index_name, column_index_orientation;

        String absolutePathOfImage = null;
        String image_id = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns._ID};

        //final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getActivity().getContentResolver().query(uri, projection, null, null, ORDER);

        column_index_data = cursor.getColumnIndex(projection[0]);
        column_index_date_taken = cursor.getColumnIndex(projection[1]);
        column_index_mime_type = cursor.getColumnIndex(projection[2]);
        column_index_name = cursor.getColumnIndex(projection[3]);
        column_index_image_id = cursor.getColumnIndex(projection[4]);
        if (cursor.getCount() == 0) {
           // mTextView.bringToFront();
            mTextView.setVisibility(View.VISIBLE);
        }else{
            mTextView.setVisibility(View.GONE);

        }


        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            LocalGalleryItem obj_model = new LocalGalleryItem();
            obj_model.setPath(absolutePathOfImage);

            obj_model.setName(cursor.getString(column_index_name));

            image_id = cursor.getString(column_index_image_id);
            //obj_model.setThumbnailPath(uriToThumbnail(image_id));
            obj_model.setMimeType(cursor.getString(column_index_mime_type));
            obj_model.setThumbnailPath(getThumbnailPathForLocalFile(image_id, obj_model.getPath()));

            date.setTime(Long.parseLong(cursor.getString(column_index_date_taken)));
            obj_model.setDateTakenTime(dateFormat.DateToString(date, Item.GRID_ITEM_TYPE));


            if (isFirstItem == true) {
                compareDate.setTime(date.getTime());
                sItemList.add(new LocalHeaderItem(dateFormat.DateToString(date, Item.HEADER_ITEM_TYPE)));
                isFirstItem = false;

            }

            if (dateFormat.compareTime(compareDate, date) != 0) {
                int check = dateFormat.compareTime(compareDate, date);
                compareDate.setTime(date.getTime());
                sItemList.add(new LocalHeaderItem(dateFormat.DateToString(date, Item.HEADER_ITEM_TYPE)));
            }

            sItemList.add(obj_model);

        }

        mLocalRecyclerViewAdapter.addAll(sItemList);

        return sItemList;
    }


    String getThumbnailPathForLocalFile(String imageId, String imagePath) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = {MediaStore.Images.Thumbnails.DATA};
        ContentResolver contentResolver = getActivity().getContentResolver();

        // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
        Cursor thumbnailCursor = contentResolver.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 썸네일 컨텐트 테이블
                projection, // DATA를 출력
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // IMAGE_ID는 원본 이미지의 _ID를 나타냅니다.
                new String[]{imageId},
                null);


        if (thumbnailCursor == null) {
            return imageId;
        } else if (thumbnailCursor.moveToFirst()) { //thumbnailPath가 존재할 때..
            int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);

            String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
            thumbnailCursor.close();


            //원본 사진의 orientation 속성을 가져온다
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);


            ExifInterface exifInterface = null;
            try {
                exifInterface = new ExifInterface(thumbnailPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //thumbnail image의 orientation 속성을 원래 사진의 orientation과 같게 만들어준다.
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                    String.valueOf(orientation));
            try {
                exifInterface.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return thumbnailPath;
        } else {
            // thumbnailCursor가 비었습니다.
            // 이는 이미지 파일이 있더라도 썸네일이 존재하지 않을 수 있기 때문입니다.
            // 보통 이미지가 생성된 지 얼마 되지 않았을 때 그렇습니다.
            // 썸네일이 존재하지 않을 때에는 아래와 같이 썸네일을 생성하도록 요청합니다

            //return Bitmap
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
            thumbnailCursor.close();
            return getThumbnailPathForLocalFile(imageId, imagePath);
        }
    }


    /*권한이 없을 경우, 권한 사용 동의창을 띄우고, 아래와 같이 동의, 비동의에 대한 콜백을 받습니다.*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                //갤러리 사용 권한에 대한 콜백을 받음
                for (int i = 0; i < grantResults.length; i++) {

                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //권한 동의 버튼 선택
                        //getImagePath();
                        getImages();
                    } else {
                        //사용자가 권한 동의를 안함
                        //권한 동의 안함 버튼 선택
                        Toast.makeText(getActivity(), "앱을 실행하기위해 동의를 해 주셔야합니다.", Toast.LENGTH_LONG).show();

                    }
                }
            }
        }
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
                    //loadMoreItems();
                }
            }
        }
    };


    public void moveToTopOfThePage() {
        gridLayoutManager.scrollToPositionWithOffset(0, 0);
    }


    protected void deleteItems() {

        HashMap<Integer, Boolean> itemStates = mLocalRecyclerViewAdapter.getItemStateArray();
        String checkedItemId;

        Iterator<Integer> iterator = itemStates.keySet().iterator();
        LocalGalleryItem item;


        while (iterator.hasNext()) {
            int key = iterator.next();
            item = ((LocalGalleryItem) mLocalRecyclerViewAdapter.getItem(key));

            File file = new File(item.getPath());
            File fileThumbnail = new File(item.getThumbnailPath());
            Log.i(TAG, file.getAbsolutePath());

            if (file.exists()) {

                file.delete();
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            }

            if(fileThumbnail.exists()){
                fileThumbnail.delete();
                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(fileThumbnail)));

            }

        }

    }


    protected void multipleFilesUpload() {


        uploadedItems.clear();

        HashMap<Integer, Boolean> itemStates = mLocalRecyclerViewAdapter.getItemStateArray();

        Iterator<Integer> iterator = itemStates.keySet().iterator();
        int key;

        MediaType contentType = MediaType.parse("application/json; charset=UTF-8");

        while (iterator.hasNext()) {
            final LocalGalleryItem item;

            key = iterator.next();

            item = ((LocalGalleryItem) mLocalRecyclerViewAdapter.getItem(key));

            File file = new File(item.getPath());

            // MediaType contentType = MediaType.parse("application/json; charset=UTF-8");
            String content = "{\"name\": \"" + file.getName() + "\"}";
            MultipartBody.Part metaPart = MultipartBody.Part.create(RequestBody.create(contentType, content));

            String mineType = item.getMimeType();
            MultipartBody.Part mediaPart = MultipartBody.Part.create(RequestBody.create(MediaType.parse(mineType), file));


            Call<ResponseBody> call = server.uploadMultipleFiles(metaPart, mediaPart);
            Log.i(TAG + "call", call.request().toString());

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(getContext(), "uploading successfully", Toast.LENGTH_SHORT).show();

                    Log.i(TAG, response.headers().toString());
                    if(response.isSuccessful()){
                        uploadedItems.add(item);
                        Date currentDate = new Date();
                        UploadFile uploadFile = new UploadFile(item.getName(), item.getThumbnailPath(), dateFormat.DateToString(currentDate, Item.ROOM_ITEM_TYPE));
                        FileDatabase.getDatabase(getContext()).getFileDao().insertUploadFile(uploadFile);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), UploadResultActivity.class);
                startActivity(intent);
            }
        }, 5000);


    }


    public class DownloadImageTask extends AsyncTask<Void, Integer, List<Item>> {

        private Date compareDate = new Date();
        private Date date = new Date();
        CustomDateFormat dateFormat = new CustomDateFormat();

        @Override protected void onPreExecute() {
            mLocalRecyclerViewAdapter.clear();

        }


        @Override
        protected void onPostExecute(List<Item> itemList){
            mLocalRecyclerViewAdapter.addAll(itemList);

        }


        @Override
        protected List<Item> doInBackground(Void ... voids) {
            List<Item> sItemList = new ArrayList<>();
            sItemList.clear();
            boolean isFirstItem = true;
            Uri uri;
            Cursor cursor;
            int column_index_data, column_index_date_taken, column_index_image_id, column_index_mime_type, column_index_name, column_index_orientation;

            String absolutePathOfImage = null;
            String image_id = null;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns._ID};

            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            cursor = getActivity().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

            column_index_data = cursor.getColumnIndex(projection[0]);
            column_index_date_taken = cursor.getColumnIndex(projection[1]);
            column_index_mime_type = cursor.getColumnIndex(projection[2]);
            column_index_name = cursor.getColumnIndex(projection[3]);
            column_index_image_id = cursor.getColumnIndex(projection[4]);
       /* if (cursor.getCount() == 0) {
            mTextView.setVisibility(View.VISIBLE);
        }else{
            mTextView.setVisibility(View.GONE);

        }*/


            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);

                LocalGalleryItem obj_model = new LocalGalleryItem();
                obj_model.setPath(absolutePathOfImage);

                obj_model.setName(cursor.getString(column_index_name));

                image_id = cursor.getString(column_index_image_id);
                //obj_model.setThumbnailPath(uriToThumbnail(image_id));
                obj_model.setMimeType(cursor.getString(column_index_mime_type));
                obj_model.setThumbnailPath(getThumbnailPathForLocalFile(image_id, obj_model.getPath()));

                date.setTime(Long.parseLong(cursor.getString(column_index_date_taken)));
                obj_model.setDateTakenTime(dateFormat.DateToString(date, Item.GRID_ITEM_TYPE));


                if (isFirstItem == true) {
                    compareDate.setTime(date.getTime());
                    sItemList.add(new LocalHeaderItem(dateFormat.DateToString(date, Item.HEADER_ITEM_TYPE)));
                    isFirstItem = false;

                }

                if (dateFormat.compareTime(compareDate, date) != 0) {
                    int check = dateFormat.compareTime(compareDate, date);
                    compareDate.setTime(date.getTime());
                    sItemList.add(new LocalHeaderItem(dateFormat.DateToString(date, Item.HEADER_ITEM_TYPE)));
                }

                sItemList.add(obj_model);

            }


            return sItemList;
        }


        String getThumbnailPathForLocalFile(String imageId, String imagePath) {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            String[] projection = {MediaStore.Images.Thumbnails.DATA};
            ContentResolver contentResolver = getActivity().getContentResolver();

            // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
            Cursor thumbnailCursor = contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 썸네일 컨텐트 테이블
                    projection, // DATA를 출력
                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // IMAGE_ID는 원본 이미지의 _ID를 나타냅니다.
                    new String[]{imageId},
                    null);


            if (thumbnailCursor == null) {
                return imageId;
            } else if (thumbnailCursor.moveToFirst()) { //thumbnailPath가 존재할 때..
                int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);

                String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
                thumbnailCursor.close();

                //원본 사진의 orientation 속성을 가져온다
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);


                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(thumbnailPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //thumbnail image의 orientation 속성을 원래 사진의 orientation과 같게 만들어준다.
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION,
                        String.valueOf(orientation));
                try {
                    exifInterface.saveAttributes();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return thumbnailPath;
            } else {
                // thumbnailCursor가 비었습니다.
                // 이는 이미지 파일이 있더라도 썸네일이 존재하지 않을 수 있기 때문입니다.
                // 보통 이미지가 생성된 지 얼마 되지 않았을 때 그렇습니다.
                // 썸네일이 존재하지 않을 때에는 아래와 같이 썸네일을 생성하도록 요청합니다

                //return Bitmap
                MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MICRO_KIND, null);
                thumbnailCursor.close();
                return getThumbnailPathForLocalFile(imageId, imagePath);
            }
        }

    }

    public int getCheckCount(){
        return mLocalRecyclerViewAdapter.getItemStateArray().size();
    }


}
