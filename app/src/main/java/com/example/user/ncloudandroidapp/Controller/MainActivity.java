package com.example.user.ncloudandroidapp.Controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.TabPagerAdapter;
import com.example.user.ncloudandroidapp.CustomViewPager;
import com.example.user.ncloudandroidapp.OAuthHelper;
import com.example.user.ncloudandroidapp.OAuthToken;
import com.example.user.ncloudandroidapp.R;


import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = getClass().getSimpleName();
    private TabPagerAdapter pagerAdapter;
    private static final int GDRIVE = 0;
    private static final int LOCAL = 1;

    public final int ORDER_CREATED_DESC = 1;
    public final int ORDER_CREATED_ASC = 2;
    public final int ORDER_MODEFIED_DESC = 3;
    public final int ORDER_MODEFIED_ASC = 4;

    OAuthHelper mOAuthHelper;

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
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mOAuthHelper = new OAuthHelper(getApplicationContext());
        setSupportActionBar(mToolbar);

        // Get the ActionBar here to configure the way it behaves.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(true);
        //setTitle("WM Gallery APP");


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

        mUploadLocal.setOnClickListener(this);
        mDeleteLocal.setOnClickListener(this);
        mDownloadGDrive.setOnClickListener(this);
        mDeleteGDrive.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
       //     Toast.makeText(getApplicationContext(), "SLIDE!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), UploadResultActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {
        //    Toast.makeText(getApplicationContext(), "GALLERY!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), DownloadResultActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        Log.i(TAG, "CLOSE");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_choose:
                setVisibility();
                return true;

            case R.id.action_settings1:
                setListOrder(ORDER_CREATED_DESC);
                return true;

            case R.id.action_settings2:
                setListOrder(ORDER_CREATED_ASC);
                return true;
            case R.id.action_settings3:
                setListOrder(ORDER_MODEFIED_DESC);
                return true;
            case R.id.action_settings4:
                setListOrder(ORDER_MODEFIED_ASC);
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
              //  Toast.makeText(MainActivity.this, "UPLOAD", Toast.LENGTH_LONG).show();
                dialogBuilderUploadFiles();
                break;

            case R.id.nav_delete_gdrive:
                //Toast.makeText(MainActivity.this, "DELETE", Toast.LENGTH_LONG).show();
                dialogBuilderDeleteFiles();


                break;

            case R.id.nav_download_gdrive:
             //   Toast.makeText(MainActivity.this, "DOWNLOAD", Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void setListOrder(int order) {
        int position = mTabLayout.getSelectedTabPosition();
        if (position == GDRIVE) {
            Fragment gdriveFragment = pagerAdapter.getItem(position);
            GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);
            gDriveGalleryFragment.setOrderByNum(order);
        } else if (position == LOCAL) {
            Fragment localFragment = pagerAdapter.getItem(position);
            LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);
            localGalleryFragment.setOrderByNum(order);
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
                //setTitle("내 사진첩");
                localGalleryFragment.changeMode(true);
                localGalleryFragment.refresh();
                localGalleryFragment.clearCheckBoxes();
            } else {
                mConstraintLayoutLocal.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.VISIBLE);
                mViewPager.setSwipeLocked(false);
                mMultipleSelection.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                //setTitle("WM Gallery APP");
                localGalleryFragment.changeMode(false);
                localGalleryFragment.refresh();
                localGalleryFragment.clearCheckBoxes();
            }
        } else if (position == GDRIVE) {

            Fragment gdriveFragment = pagerAdapter.getItem(position);
            GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);

            if (mToolbar.getVisibility() == View.VISIBLE) {
                mConstraintLayoutGDrive.setVisibility(View.VISIBLE);
                mTabLayout.setVisibility(View.GONE);
                mViewPager.setSwipeLocked(true);
                mMultipleSelection.setVisibility(View.VISIBLE);
                mToolbar.setVisibility(View.GONE);
                //setTitle("구글 드라이브");
                gDriveGalleryFragment.changeMode(true);
                gDriveGalleryFragment.refresh();
                gDriveGalleryFragment.clearCheckBoxes();
            } else {
                mConstraintLayoutGDrive.setVisibility(View.GONE);
                mTabLayout.setVisibility(View.VISIBLE);
                mViewPager.setSwipeLocked(false);
                mMultipleSelection.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                //setTitle("WM Gallery APP");
                gDriveGalleryFragment.changeMode(false);
                gDriveGalleryFragment.refresh();
                gDriveGalleryFragment.clearCheckBoxes();
            }
        }

    }

    private void dialogBuilderDeleteFiles() {
        int position = mTabLayout.getSelectedTabPosition();
        int checkedItemCount = 0;
        if (position == GDRIVE) {
            Fragment gdriveFragment = pagerAdapter.getItem(position);
            GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);
            checkedItemCount = gDriveGalleryFragment.getCheckCount();
        } else {
            Fragment localFragment = pagerAdapter.getItem(position);
            LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);
            checkedItemCount = localGalleryFragment.getCheckCount();
        }
        if (checkedItemCount > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("삭제하기");
            builder.setMessage(checkedItemCount + "장의 사진을 삭제 하시겠습니까?");
            builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    int position = mTabLayout.getSelectedTabPosition();

                    if (position == GDRIVE) {
                        Fragment gdriveFragment = pagerAdapter.getItem(position);
                        GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);
                        gDriveGalleryFragment.deleteImages();
                        Toast.makeText(MainActivity.this, "사진 삭제를 완료하였습니다.", Toast.LENGTH_LONG).show();

                    } else if (position == LOCAL) {
                        Fragment localFragment = pagerAdapter.getItem(position);
                        LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);
                        localGalleryFragment.deleteImages();
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

    }

    private void dialogBuilderUploadFiles() {
        int position = mTabLayout.getSelectedTabPosition();
        final Fragment localFragment = pagerAdapter.getItem(position);
        final LocalGalleryFragment localGalleryFragment = ((LocalGalleryFragment) localFragment);

        int checkedItemCount = localGalleryFragment.getCheckCount();
        if (checkedItemCount > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("업로드");
            builder.setMessage(checkedItemCount + "장의 사진을 업로드 하시겠습니까?");
            builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    localGalleryFragment.uploadImages();
                    //localGalleryFragment.multipleFilesUpload();
                    //localGalleryFragment.onRefresh();
                    Toast.makeText(MainActivity.this, "사진 업로드 중입니다.", Toast.LENGTH_LONG).show();
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

    private void dialogBuilderDownloadFiles() {
        int position = mTabLayout.getSelectedTabPosition();
        Fragment gdriveFragment = pagerAdapter.getItem(position);
        final GDriveGalleryFragment gDriveGalleryFragment = ((GDriveGalleryFragment) gdriveFragment);

        int checkedItemCount = gDriveGalleryFragment.getCheckCount();
        if (checkedItemCount > 0) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("다운로드");
            builder.setMessage(checkedItemCount + "장의 사진을 다운로드 하시겠습니까?");
            builder.setPositiveButton(R.string.ok_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    gDriveGalleryFragment.downloadMultipleFiles();
                    //gDriveGalleryFragment.onRefresh();
                    Toast.makeText(MainActivity.this, "사진 다운로드 중입니다.", Toast.LENGTH_LONG).show();

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


}

