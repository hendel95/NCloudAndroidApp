package com.example.user.ncloudandroidapp;

import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.DownloadResultRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;
import com.example.user.ncloudandroidapp.Model.LocalGalleryItem;
import com.example.user.ncloudandroidapp.Room.DownloadFile;
import com.example.user.ncloudandroidapp.Room.FileDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadResultActivity extends AppCompatActivity {

    @BindView(R.id.download_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.nav_delete_download)
    ImageButton mImageButton;

    @BindView(R.id.nav_remove_all_download)
    TextView mTextView;

    DownloadResultRecyclerViewAdapter mDownloadResultRecyclerViewAdapter;
    List<Item> mItemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_result);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.custom_decorator));

        mDownloadResultRecyclerViewAdapter = new DownloadResultRecyclerViewAdapter(getApplicationContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mDownloadResultRecyclerViewAdapter);


        List<DownloadFile> downloadFileList = FileDatabase.getDatabase(getApplicationContext()).getFileDao().getAllDownloadedFiles();

        if(!downloadFileList.isEmpty()) {
            String tempDate = downloadFileList.get(0).getMDate();

            mItemArrayList.add(new HeaderItem(tempDate));

            for (DownloadFile downloadFile : downloadFileList) {
                GalleryItem item = new GalleryItem();
                item.setThumbnailLink(downloadFile.getMThumbnailPath());
                item.setName(downloadFile.getMName());
                if (!tempDate.equals(downloadFile.getMDate())) {
                    mItemArrayList.add(new HeaderItem(downloadFile.getMDate()));
                    tempDate = downloadFile.getMDate();
                }
                item.setDownloadTime(downloadFile.getMDate());
                mItemArrayList.add(item);
            }

            mDownloadResultRecyclerViewAdapter.addAll(mItemArrayList);

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
                FileDatabase.getDatabase(getApplicationContext()).getFileDao().clearAllDownloadedFiles();
                mDownloadResultRecyclerViewAdapter.clear();
                mDownloadResultRecyclerViewAdapter.notifyDataSetChanged();
            }
        }, 1000);
    }

    private void dialogBuilderDeleteAllFiles() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("다운로드 목록 삭제");
        builder.setMessage("다운로드 목록 전체를 삭제하시겠습니까?");
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


