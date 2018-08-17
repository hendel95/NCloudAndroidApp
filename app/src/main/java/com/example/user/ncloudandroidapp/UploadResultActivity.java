package com.example.user.ncloudandroidapp;

import android.content.DialogInterface;
import android.media.Image;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Adapter.UploadResultRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Room.FileDatabase;
import com.example.user.ncloudandroidapp.Room.UploadFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadResultActivity extends AppCompatActivity {

    @BindView(R.id.upload_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.nav_delete_upload)
    ImageButton mImageButton;

    @BindView(R.id.nav_remove_all_upload)
    TextView mTextView;

    UploadResultRecyclerViewAdapter mUploadResultRecyclerViewAdapter;
    List<Item> mItemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_result);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.custom_decorator));

        mUploadResultRecyclerViewAdapter = new UploadResultRecyclerViewAdapter(getApplicationContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mUploadResultRecyclerViewAdapter);


        List<UploadFile> uploadFileList = FileDatabase.getDatabase(getApplicationContext()).getFileDao().getAllUploadedFiles();

        if(!uploadFileList.isEmpty()) {
            String tempDate = uploadFileList.get(0).getMDate();

            mItemArrayList.add(new HeaderItem(tempDate));

            for (UploadFile uploadFile : uploadFileList) {
                LocalGalleryItem item = new LocalGalleryItem();
                item.setThumbnailPath(uploadFile.getMThumbnailPath());
                item.setName(uploadFile.getMName());
                if (!tempDate.equals(uploadFile.getMDate())) {
                    mItemArrayList.add(new HeaderItem(uploadFile.getMDate()));
                    tempDate = uploadFile.getMDate();
                }
                item.setUploadTime(uploadFile.getMDate());
                mItemArrayList.add(item);
            }

            mUploadResultRecyclerViewAdapter.addAll(mItemArrayList);

        }


        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTextView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilderDeleteAllFiles();
                    }
                }
        );
    }



    public void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().clearAllUploadedFiles();
                mUploadResultRecyclerViewAdapter.clear();
                mUploadResultRecyclerViewAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    private void dialogBuilderDeleteAllFiles() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("업로드 목록 삭제");
        builder.setMessage("업로드 목록 전체를 삭제하시겠습니까?");
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refresh();
                Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }


}
