package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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
import com.google.android.gms.vision.text.Line;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
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

  //  @BindView(R.id.check_box)
 //   CheckBox mCheckBox;

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


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_dark);

        // Initializing the TabLayout
        mTabLayout.addTab(mTabLayout.newTab().setText("구글 드라이브"));
        mTabLayout.addTab(mTabLayout.newTab().setText("내 사진첩"));

        mConstraintLayoutGDrive.bringToFront();
        mConstraintLayoutLocal.bringToFront();


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

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()) {
            case R.id.action_choose:
                // User chose the "Settings" item, show the app settings UI...
                //  mCheckBox.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "사진 선택 버튼", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Checked!");

                int position = mViewPager.getCurrentItem();
                //Fragment fragment = pagerAdapter.getCount();
                if(position == GDRIVE) {

                    if (mConstraintLayoutGDrive.getVisibility() == View.GONE) {
                        mConstraintLayoutGDrive.setVisibility(View.VISIBLE);
                        mTabLayout.setVisibility(View.GONE);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_clear_light);
                        mViewPager.setSwipeLocked(true);
                    } else {
                        mConstraintLayoutGDrive.setVisibility(View.GONE);
                        mTabLayout.setVisibility(View.VISIBLE);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_dark);
                        mViewPager.setSwipeLocked(false);
                    }


                }
                else if(position == LOCAL){
                    if (mConstraintLayoutLocal.getVisibility() == View.GONE) {
                        mConstraintLayoutLocal.setVisibility(View.VISIBLE);
                        mTabLayout.setVisibility(View.GONE);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_clear_light);
                        mViewPager.setSwipeLocked(true);

                    } else {
                        mConstraintLayoutLocal.setVisibility(View.GONE);
                        mTabLayout.setVisibility(View.VISIBLE);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_dark);
                        mViewPager.setSwipeLocked(false);
                    }

                }


                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "정렬 버튼", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_delete_local:
                Toast.makeText(getApplicationContext(), "DELETE", Toast.LENGTH_LONG).show();

                break;

            case R.id.nav_upload_local:
                    Toast.makeText(getApplicationContext(), "UPLOAD", Toast.LENGTH_LONG).show();

                    break;

            case R.id.nav_delete_gdrive:
                Toast.makeText(getApplicationContext(), "DELETE", Toast.LENGTH_LONG).show();

                break;

            case R.id.nav_download_gdrive:
                Toast.makeText(getApplicationContext(), "DOWNLOAD", Toast.LENGTH_LONG).show();

                break;
                /*
            case R.id.toolbar:
                int position = mTabLayout.getSelectedTabPosition();
                if(position == GDRIVE){
                    GDriveGalleryFragment gDriveGalleryFragment = new GDriveGalleryFragment();
                    gDriveGalleryFragment.gridLayoutManager.scrollToPositionWithOffset(0, 0);
                }
                else if(position == LOCAL){
                    LocalGalleryFragment localGalleryFragment = new LocalGalleryFragment();
                    localGalleryFragment.gridLayoutManager.scrollToPositionWithOffset(0, 0);
                }

                break;*/
        }
    }
}

