package com.example.user.ncloudandroidapp;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.LocalRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Model.LocalHeaderItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalGalleryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocalGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocalGalleryFragment extends Fragment implements Toolbar.OnMenuItemClickListener , SwipeRefreshLayout.OnRefreshListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
    private GridLayoutManager gridLayoutManager;

    private int PICK_IMAGE_REQUEST = 1;
    private String TAG = "LocalGalleryActivity";
    private static final int DEFAULT_SPAN_COUNT = 3;

    private Date compareDate = new Date();
    private Date date = new Date();
   // public static List<Item> sItemList = new ArrayList<>();

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


        Toolbar toolbar= (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        toolbar.setOnMenuItemClickListener(this);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        gridLayoutManager = new GridLayoutManager(getActivity(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mLocalRecyclerViewAdapter = new LocalRecyclerViewAdapter(getActivity(), gridLayoutManager, DEFAULT_SPAN_COUNT);

        mRecyclerView.setAdapter(mLocalRecyclerViewAdapter);

        permissionCheck();

        return view;
    }

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
                //mLocalRecyclerViewAdapter.clear();
                mLocalRecyclerViewAdapter.notifyDataSetChanged();
                permissionCheck();

                //  getImagePath();
                //mRecyclerView.setAdapter(mLocalRecyclerViewAdapter);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);

    }

    public void permissionCheck(){
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
                requestPermissions( new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
               /* ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                */
                //Toast.makeText(getActivity(), "최초 권한 요청.", Toast.LENGTH_LONG).show();

            }
        } else {
            //사용 권한이 있음을 확인하는 경우
            Log.e("Else", "Else");
            //Toast.makeText(getActivity(), "권한 확인 완료.", Toast.LENGTH_LONG).show();

            getImagePath();

        }
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefreshLayout.setOnRefreshListener(listener);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(getActivity(), "사진 선택", Toast.LENGTH_LONG).show();

                return true;
        }
        return false;
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
    public List<Item> getImagePath() {


        List<Item> sItemList = new ArrayList<>();
        sItemList.clear();
        mLocalRecyclerViewAdapter.clear();
        boolean isFirstItem = true;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_date_taken, column_index_image_id, column_index_mime_type, column_index_name;

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
        if(cursor.getCount() == 0){
            mTextView.setText(R.string.empty_file);
            mTextView.bringToFront();
        }


        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            LocalGalleryItem obj_model = new LocalGalleryItem();
            obj_model.setPath(absolutePathOfImage);

            obj_model.setName(cursor.getString(column_index_name));

            image_id = cursor.getString(column_index_image_id);
            obj_model.setThumbnailPath(uriToThumbnail(image_id).toString());
            obj_model.setMimeType(cursor.getString(column_index_mime_type));
            Log.i("LOCAL", "NAME" + obj_model.getName() + "MIME" + obj_model.getMimeType());
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
                        getImagePath();
                    } else {
                        //사용자가 권한 동의를 안함
                        //권한 동의 안함 버튼 선택
                        Toast.makeText(getActivity(), "앱을 실행하기위해 동의를 해 주셔야합니다.", Toast.LENGTH_LONG).show();

                    }
                }
            }
        }
    }

    Uri uriToThumbnail(String imageId) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = {MediaStore.Images.Thumbnails.DATA};
        ContentResolver contentResolver =getActivity().getContentResolver();

        // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
        Cursor thumbnailCursor = contentResolver.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 썸네일 컨텐트 테이블
                projection, // DATA를 출력
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // IMAGE_ID는 원본 이미지의 _ID를 나타냅니다.
                new String[]{imageId},
                null);
        if (thumbnailCursor == null) {
            return Uri.parse(imageId);
        } else if (thumbnailCursor.moveToFirst()) {
            int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);

            String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
            thumbnailCursor.close();
            return Uri.parse(thumbnailPath);
        } else {
            // thumbnailCursor가 비었습니다.
            // 이는 이미지 파일이 있더라도 썸네일이 존재하지 않을 수 있기 때문입니다.
            // 보통 이미지가 생성된 지 얼마 되지 않았을 때 그렇습니다.
            // 썸네일이 존재하지 않을 때에는 아래와 같이 썸네일을 생성하도록 요청합니다
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
            thumbnailCursor.close();
            return uriToThumbnail(imageId);
        }
    }

}
