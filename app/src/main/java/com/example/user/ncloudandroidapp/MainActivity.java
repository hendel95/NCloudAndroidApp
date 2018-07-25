package com.example.user.ncloudandroidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ncloudandroidapp.Adapter.CustomRecyclerViewAdapter;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.GalleryItems;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String FIELDS = "nextPageToken, files/id, files/name, files/mimeType, files/thumbnailLink, files/createdTime";
    private static final String Q = "mimeType contains 'image' and trashed = false";
    private static final String ORDER = "createdTime desc";
    private static final Integer PAGE_SIZE = 100;
    private String PAGE_TOKEN = null;

    private static final int DEFAULT_SPAN_COUNT = 3;
    private CustomRecyclerViewAdapter mAdapter;
    private CustomDateFormat mCustomDateFormat = new CustomDateFormat();
    private GridLayoutManager gridLayoutManager;

    private String currentDate;

    private boolean isLastPage = false;
    private boolean isLoading = false;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.main_text)
    TextView mTextView;
    //@BindView(R.id.progressBar)
    //ProgressBar mProgressBar;

    //  @BindView(R.id.tabLayout)
    // TabLayout mTabLayout;

    //@BindView(R.id.toolbar)
    //Toolbar mToolbar;

    // @BindView(R.id.pager)
    //ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
/*
        // Initializing the TabLayout
        mTabLayout.addTab(mTabLayout.newTab().setText("Tab One"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Tab Two"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


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

*/
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        mAdapter = new CustomRecyclerViewAdapter(getApplicationContext(), gridLayoutManager, DEFAULT_SPAN_COUNT);
        mRecyclerView.setAdapter(mAdapter);

        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(this);
        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER, PAGE_SIZE, PAGE_TOKEN);
        galleryItemCall.enqueue(findFirstImagesCallback);

    }

    private Callback<GalleryItems> findFirstImagesCallback = new Callback<GalleryItems>() {

        @Override
        public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response) {
            GalleryItems galleryItems = response.body();
            isLoading = false;

            if (response.code() == 200 && galleryItems != null) {
                PAGE_TOKEN = galleryItems.getNextPageToken();
                List<GalleryItem> items = galleryItems.getFiles();
                if (items != null) {
                   // Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();

                    if (items.size() > 0) {
                        currentDate = items.get(0).getCreatedTime();
                        mAdapter.add(new HeaderItem(mCustomDateFormat.dateFormatting(currentDate, Item.HEADER_ITEM_TYPE)));
                        configViews(items);
                    }else{
                        mTextView.setText(R.string.empty_file);
                        mTextView.bringToFront();
                    }
                    if (items.size() >= PAGE_SIZE) {
                        mAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }else{
                    mTextView.setText(R.string.empty_file);
                    mTextView.bringToFront();
                }

            } else if (response.code() == 400) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 401) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 403) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 404) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
            }else if (response.code() == 504) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_504), Toast.LENGTH_SHORT).show();
            }else if(response.code() == 200 && galleryItems == null){
                mTextView.setText(R.string.empty_file);
                mTextView.bringToFront();
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
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 401) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 403) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 404) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
            } else if (response.code() == 504) {
                Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_504), Toast.LENGTH_SHORT).show();
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


    protected void loadMoreItems() {
        isLoading = true;
        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(MainActivity.this);
        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER, PAGE_SIZE, PAGE_TOKEN);
        galleryItemCall.enqueue(findNextImagesCallback);
    }


}

