package com.example.user.ncloudandroidapp;

import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.TabPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";


    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

     @BindView(R.id.toolbar)
     Toolbar mToolbar;

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.bottom_nav)
    BottomNavigationView mBottomNavigationView;
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
        actionBar.setDisplayShowTitleEnabled(false);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu_dark);

        // Initializing the TabLayout
        mTabLayout.addTab(mTabLayout.newTab().setText("구글 드라이브"));
        mTabLayout.addTab(mTabLayout.newTab().setText("내 사진첩"));

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        // Set TabSelectedListener
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
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
                Log.i(TAG, "Checked!");
                if (mBottomNavigationView.getVisibility() == View.GONE) {
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    mBottomNavigationView.bringToFront();
                } else {
                    mBottomNavigationView.setVisibility(View.GONE);
                }

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "정렬 버튼", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
        }

    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_choose:
                // User chose the "Settings" item, show the app settings UI...
              //  mCheckBox.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "사진 선택 버튼", Toast.LENGTH_LONG).show();
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);
                Log.i(TAG, "Checked!");
                if(mBottomNavigationView.getVisibility() == View.GONE){
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    mBottomNavigationView.bringToFront();
                }
                else{
                    mBottomNavigationView.setVisibility(View.GONE);
                }

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "정렬 버튼", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);

    }*/

}

