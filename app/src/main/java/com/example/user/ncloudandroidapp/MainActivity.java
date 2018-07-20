package com.example.user.ncloudandroidapp;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.user.ncloudandroidapp.Model.GalleryItem;
import com.example.user.ncloudandroidapp.Model.GalleryItems;
import com.example.user.ncloudandroidapp.Model.HeaderItem;
import com.example.user.ncloudandroidapp.Model.Item;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String FIELDS = "nextPageToken, files/id, files/name, files/mimeType, files/thumbnailLink, files/createdTime";
    private static final String Q = "mimeType contains 'image' and trashed = false";
    private static final String ORDER = "createdTime desc";
    private static final Integer PAGE_SIZE = 10;
    private String PAGE_TOKEN = null;

    private static final int DEFAULT_SPAN_COUNT = 3;
    private List<Item> mItemList;
    private CustomRecyclerViewAdapter mAdapter;
    private CustomDateFormat mCustomDateFormat = new CustomDateFormat();
    private GridLayoutManager gridLayoutManager;

    private boolean isLastPage = false;
    private boolean isLoading = false;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

   // @BindView(R.id.progressBar)
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

*/ ////
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), DEFAULT_SPAN_COUNT);

        mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                OAuthServerIntf server = RetrofitBuilder.getOAuthClient(MainActivity.this);
                Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER, PAGE_SIZE, PAGE_TOKEN);
                galleryItemCall.enqueue(findNextImagesCallback);
            }

            @Override
            public int getTotalPageCount() {
                return 0;
            }

            @Override
            public boolean isLastPage() {
                return false;
            }

            @Override
            public boolean isLoading() {
                return false;
            }
        });

      // listGDriveUserFiles();
        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(this);
        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER, PAGE_SIZE, PAGE_TOKEN);
        galleryItemCall.enqueue(findFirstImagesCallback);

    }

    private Callback<GalleryItems> findFirstImagesCallback = new Callback<GalleryItems>(){

            @Override
            public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response) {
                GalleryItems galleryItems = response.body();
                isLoading = false;
                if (response.code() == 200 && galleryItems != null) {
                    PAGE_TOKEN = galleryItems.getNextPageToken();
                    List<GalleryItem> items = galleryItems.getFiles();
                    if (items != null) {
                        Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();
                        if(items.size() > 0)
                            configViews(items);

                        if (items.size() >= PAGE_SIZE) {
                            //add footer
                        } else {
                            isLastPage = true;
                        }
                    }

                } else if (response.code() == 400) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 403) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
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
                isLoading = false;

                if (response.code() == 200 && galleryItems != null) {
                    PAGE_TOKEN = galleryItems.getNextPageToken();
                    List<GalleryItem> items = galleryItems.getFiles();
                    if (items != null) {
                        Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();
                        if(items.size() > 0)
                            configViews(items);

                        if (items.size() >= PAGE_SIZE) {
                            //add footer
                        } else {
                            isLastPage = true;
                        }
                    }

                } else if (response.code() == 400) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 403) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 504) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" +"Can't load data. Check your network connection !! 504", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GalleryItems> call, Throwable t) {
                Log.e(TAG, "The call listFilesCall failed", t);
            }

    };


    private void listGDriveUserFiles() {
        OAuthServerIntf server = RetrofitBuilder.getOAuthClient(this);
        Call<GalleryItems> galleryItemCall = server.getFileDescription(FIELDS, Q, ORDER , PAGE_SIZE, PAGE_TOKEN);
        galleryItemCall.enqueue(new Callback<GalleryItems>() {
            @Override
            public void onResponse(Call<GalleryItems> call, Response<GalleryItems> response) {
                GalleryItems body = response.body();
                Log.d("size of files", Integer.toString(body.getFiles().size()));

                if (response.code() == 200 && response.body() != null) {
                    List<GalleryItem> items = response.body().getFiles();
                 //   PAGE_TOKEN = response.body().getNextPageToken();
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + Integer.toString(body.getFiles().size()), Toast.LENGTH_SHORT).show();
                  //  Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_200), Toast.LENGTH_SHORT).show();
                    configViews(items);

                } else if (response.code() == 400) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_400), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_401), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 403) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_403), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(MainActivity.this, response.message() + "\r\n" + getString(R.string.http_code_404), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GalleryItems> call, Throwable t) {
                Log.e(TAG, "The call listFilesCall failed", t);
            }

        });

    }

    private void configViews(List<GalleryItem> items) {


        List<GalleryItem> itemsGallery = new ArrayList<>();
        itemsGallery.clear();
        itemsGallery.addAll(items);

        mItemList = new ArrayList<>();
        mItemList.clear();

        for (int i = 0; i < items.size(); i++) {
            GalleryItem galleryItem = itemsGallery.get(i);
            if (i != 0) {
                //이전 것과 compare 했을 때, 날짜가 같지 않다면 header 생성해주기
                if (mCustomDateFormat.compareTime(itemsGallery.get(i - 1).getCreatedTime(), itemsGallery.get(i).getCreatedTime()) != 0) {
                    mItemList.add(new HeaderItem(mCustomDateFormat.dateFormatting(galleryItem.getCreatedTime(), Item.HEADER_ITEM_TYPE)));
                }
                mItemList.add(galleryItem);

            } else {
                mItemList.add(new HeaderItem(mCustomDateFormat.dateFormatting(galleryItem.getCreatedTime(), Item.HEADER_ITEM_TYPE)));
                mItemList.add(galleryItem);
            }
        }
        mAdapter = new CustomRecyclerViewAdapter(getApplicationContext(), mItemList, gridLayoutManager, DEFAULT_SPAN_COUNT);

        mRecyclerView.setAdapter(mAdapter);

    }


    public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {

        GridLayoutManager layoutManager;

        public PaginationScrollListener(GridLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading() && !isLastPage()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0 && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }
        }

        protected abstract void loadMoreItems();

        public abstract int getTotalPageCount();

        public abstract boolean isLastPage();

        public abstract boolean isLoading();
    }


}

