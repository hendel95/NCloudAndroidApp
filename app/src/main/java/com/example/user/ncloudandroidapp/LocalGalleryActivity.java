package com.example.user.ncloudandroidapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class LocalGalleryActivity extends AppCompatActivity {

    @BindView(R.id.local_recycler_view)
    RecyclerView mRecyclerView;

    LocalRecyclerViewAdapter mLocalRecyclerViewAdapter;
    private GridLayoutManager gridLayoutManager;

    private int PICK_IMAGE_REQUEST = 1;
    private String TAG = "LocalGalleryActivity";
    private static final int DEFAULT_SPAN_COUNT = 3;

    private String currentDate;
    private Date compareDate = new Date();
    private Date date = new Date();
    public static List<Item> sItemList = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS = 100;
    CustomDateFormat dateFormat = new CustomDateFormat();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_gallery);
        ButterKnife.bind(this);
/*
        gv_folder = (GridView) findViewById(R.id.gv_folder);

        gv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), PhotosActivity.class);
                intent.putExtra("value", i);
                startActivity(intent);
            }
        });
*/
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mLocalRecyclerViewAdapter = new LocalRecyclerViewAdapter(getApplicationContext(), gridLayoutManager, DEFAULT_SPAN_COUNT);

        mRecyclerView.setAdapter(mLocalRecyclerViewAdapter);

        //갤러리 사용 권한 체크 ( 사용권한이 없을 경우 -1)
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            //권한이 없을 경우

            //최소 권한 요청인지, 혹은 사용자에 의한 재 요청인지 확인
            if ((ActivityCompat.shouldShowRequestPermissionRationale(LocalGalleryActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(LocalGalleryActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
                //사용자가 임의로 권한을 취소시킨 경우

            } else {
                //최초로 권한을 요청하는 경우 (첫 실행)
                ActivityCompat.requestPermissions(LocalGalleryActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            //사용 권한이 있음을 확인하는 경우
            Log.e("Else", "Else");
            mLocalRecyclerViewAdapter.clear();
            getImagePath();
        }

    }

    public List<Item> getImagePath() {
        sItemList.clear();
        boolean isFirstItem = true;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_date_taken, column_index_image_id;

        String absolutePathOfImage = null;
        String image_id = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.Media._ID};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndex(projection[0]);
        column_index_date_taken = cursor.getColumnIndex(projection[1]);
        column_index_image_id = cursor.getColumnIndex(projection[2]);


        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            image_id = cursor.getString(column_index_image_id);

            LocalGalleryItem obj_model = new LocalGalleryItem();
            obj_model.setPath(absolutePathOfImage);
            obj_model.setThumbnailPath(uriToThumbnail(image_id).toString());
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        getImagePath();
                    } else {
                        Toast.makeText(LocalGalleryActivity.this, "The app was not allowed to read or write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /*
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                Uri uri = data.getData();
                String[] projection = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();

                Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));

                int columnIndex = cursor.getColumnIndex(projection[0]);
                String picturePath = cursor.getString(columnIndex); // returns null
                cursor.close();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    // Log.d(TAG, String.valueOf(bitmap));

                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        List<Photo> fetchAllImages() {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };

            Cursor imageCursor = getContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection, // DATA, _ID를 출력
                    null,       // 모든 개체 출력
                    null,
                    null);      // 정렬 안 함

            ArrayList<Photo> result = new ArrayList<>(imageCursor.getCount());
            int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
            int idColumnIndex = imageCursor.getColumnIndex(projection[1]);

            if (imageCursor == null) {
                // Error 발생
                // 적절하게 handling 해주세요
            } else if (imageCursor.moveToFirst()) {
                do {
                    String filePath = imageCursor.getString(dataColumnIndex);
                    String imageId = imageCursor.getString(idColumnIndex);

                    Uri thumbnailUri = uriToThumbnail(imageId);
                    Uri imageUri = Uri.parse(filePath);
                    // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                    Photo photo = new Photo(thumbnailUri, fullImageUri);
                    result.add(photo);
                } while(imageCursor.moveToNext());
            } else {
                // imageCursor가 비었습니다.
            }
            imageCursor.close();
            return result;
        }
    */
    Uri uriToThumbnail(String imageId) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = {MediaStore.Images.Thumbnails.DATA};
        ContentResolver contentResolver = getApplicationContext().getContentResolver();

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
