package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.TabPagerAdapter;
import com.example.user.ncloudandroidapp.Model.Item;
import com.google.android.gms.vision.text.Line;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private TabPagerAdapter pagerAdapter;
    private static final int GDRIVE = 0;
    private static final int LOCAL = 1;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.pager)
    CustomViewPager mViewPager;
    @BindView(R.id.bottom_nav_gdrive)
    ConstraintLayout mConstraintLayoutGDrive;
    @BindView(R.id.bottom_nav_local)
    ConstraintLayout mConstraintLayoutLocal;
    @BindView(R.id.nav_upload_local)
    ImageButton mUploadLocal;
    @BindView(R.id.nav_delete_local)
    ImageButton mDeleteLocal;
    @BindView(R.id.nav_download_gdrive)
    ImageButton mDownloadGDrive;
    @BindView(R.id.nav_delete_gdrive)
    ImageButton mDeleteGDrive;
    @BindView(R.id.multiple_selection_bar)
    ConstraintLayout mMultipleSelection;
    @BindView(R.id.cancel_button)
    ImageButton mCancelButton;

    // @BindView(R.id.check_box)
    // CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(true);
        setTitle("WM Gallery APP");


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_dark);

        // Initializing the TabLayout
        mTabLayout.addTab(mTabLayout.newTab().setText("구글 드라이브"));
        mTabLayout.addTab(mTabLayout.newTab().setText("내 사진첩"));

        mConstraintLayoutGDrive.bringToFront();
        mConstraintLayoutLocal.bringToFront();

        mToolbar.setOnClickListener(this);

        // Creating TabPagerAdapter adapter
        pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mToolbar.setOnClickListener(this);

        mUploadLocal.setOnClickListener(this);
        mDeleteLocal.setOnClickListener(this);
        mDownloadGDrive.setOnClickListener(this);
        mDeleteGDrive.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_choose:
                setVisibility();
                return true;
            default:
                Toast.makeText(getApplicationContext(), "정렬 버튼", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.nav_delete_local:
                //Toast.makeText(MainActivity.this, "DELETE", Toast.LENGTH_LONG).show();
                dialogBuilderDeleteFiles();

                break;

            case R.id.nav_upload_local:
                Toast.makeText(MainActivity.this, "UPLOAD", Toast.LENGTH_LONG).show();
                dialogBuilderUploadFiles();
                break;

            case R.id.nav_delete_gdrive:
                //Toast.makeText(MainActivity.this, "DELETE", Toast.LENGTH_LONG).show();
                dialogBuilderDeleteFiles();


                break;

            case R.id.nav_download_gdrive:
                Toast.makeText(MainActivity.this, "DOWNLOAD", Toast.LENGTH_LONG).show();
                dialogBuilderDownloadFiles();
                break;

            case R.id.cancel_button:
                setVisibility();

                break;

            case R.id.toolbar:
                int position = mTabLayout.getSelectedTabPosition();
                Fragment fragment = pagerAdapter.getItem(position);

                if (position == GDRIVE) {
                    //Fragment fragment = pagerAdapter.getItem(position);

                    GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) fragment);
                    gDriveGalleryFragment.moveToTopOfThePage();

                } else if (position == LOCAL) {
                    //Fragment fragment = pagerAdapter.getItem(position);

                    LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) fragment);
                    localGalleryFragment.moveToTopOfThePage();

                }

                break;
        }
    }


    private void setVisibility() {
        int position = mTabLayout.getSelectedTabPosition();

        if (position == LOCAL) {

            Fragment localFragment = pagerAdapter.getItem(position);
            LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);

            if (mToolbar.getVisibility() == View.VISIBLE) {
                mConstraintLayoutLocal.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.GONE);
                mViewPager.setSwipeLocked(true);
                mMultipleSelection.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.GONE);
                setTitle("내 사진첩");
                localGalleryFragment.changeMode(true);
                localGalleryFragment.refresh();
                localGalleryFragment.clearCheckBoxes();
            } else {
                mConstraintLayoutLocal.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.VISIBLE);
                mViewPager.setSwipeLocked(false);
                mMultipleSelection.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                setTitle("WM Gallery APP");
                localGalleryFragment.changeMode(false);
                localGalleryFragment.refresh();
                localGalleryFragment.clearCheckBoxes();
            }
        } else if (position == GDRIVE) {

            Fragment gdriveFragment = pagerAdapter.getItem(position);
            GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);
            gDriveGalleryFragment.deleteItems();

            if (mToolbar.getVisibility() == View.VISIBLE) {
                mConstraintLayoutGDrive.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.GONE);
                mViewPager.setSwipeLocked(true);
                mMultipleSelection.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.GONE);
                setTitle("구글 드라이브");
                gDriveGalleryFragment.changeMode(true);
                gDriveGalleryFragment.refresh();
                gDriveGalleryFragment.clearCheckBoxes();
            } else {
                mConstraintLayoutGDrive.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.VISIBLE);
                mViewPager.setSwipeLocked(false);
                mMultipleSelection.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                setTitle("WM Gallery APP");
                gDriveGalleryFragment.changeMode(false);
                gDriveGalleryFragment.refresh();
                gDriveGalleryFragment.clearCheckBoxes();
            }
        }

    }

    private void dialogBuilderDeleteFiles(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("삭제하기");
        builder.setMessage("사진을 삭제 하시겠습니까?");
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int position = mTabLayout.getSelectedTabPosition();

                if(position == GDRIVE) {
                    Fragment gdriveFragment = pagerAdapter.getItem(position);
                    GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);
                    gDriveGalleryFragment.deleteItems();
                    gDriveGalleryFragment.onRefresh();
                    Toast.makeText(MainActivity.this, "사진 삭제를 완료하였습니다.", Toast.LENGTH_LONG).show();

                }
                else if(position == LOCAL){
                    Fragment localFragment = pagerAdapter.getItem(position);
                    LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);
                    localGalleryFragment.deleteItems();
                    localGalleryFragment.onRefresh();
                    Toast.makeText(MainActivity.this, "사진 삭제를 완료하였습니다.", Toast.LENGTH_LONG).show();

                }
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    private void dialogBuilderUploadFiles(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("업로드");
        builder.setMessage("사진을 업로드 하시겠습니까?");
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int position = mTabLayout.getSelectedTabPosition();

               if(position == LOCAL){
                    Fragment localFragment = pagerAdapter.getItem(position);
                    LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);
                    //localGalleryFragment.resumableUpload();
                    //localGalleryFragment.multipleFilesUpload();

                   localGalleryFragment.multipleFilesUpload();
                    localGalleryFragment.onRefresh();
                    Toast.makeText(MainActivity.this, "사진 업로드를 완료하였습니다.", Toast.LENGTH_LONG).show();

                }
            }
        });

        builder.setNegativeButton(R.string.cancel_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    private void dialogBuilderDownloadFiles(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("다운로드");
        builder.setMessage("사진을 다운로드 하시겠습니까?");
        builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int position = mTabLayout.getSelectedTabPosition();

                if(position == GDRIVE) {
                    Fragment gdriveFragment = pagerAdapter.getItem(position);
                    GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);
                    gDriveGalleryFragment.downloadMultipleFiles();
                    gDriveGalleryFragment.onRefresh();
                    Toast.makeText(MainActivity.this, "사진 다운로드를 완료하였습니다.", Toast.LENGTH_LONG).show();

                }

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

