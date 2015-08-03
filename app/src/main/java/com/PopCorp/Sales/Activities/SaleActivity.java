package com.PopCorp.Sales.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Fragments.SaleFragment;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.PopCorp.Sales.Utilites.ZoomOutPageTransformer;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

public class SaleActivity extends AppCompatActivity {

    public static final String CURRENT_SALE_TAG = "current_sale_tag";
    public static final String CURRENT_ARRAY_SALES_TAG = "current_array_sales_tag";

    private Sale zoomedSale;
    private ArrayList<Sale> sales;
    private Toolbar toolBar;
    private DB db;
    private ViewPager viewPager;
    private SharedPreferences sPref;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) this.getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        db = ((SalesApplication) getApplication()).getDB();
        sPref = PreferenceManager.getDefaultSharedPreferences(this);

        toolBar = (Toolbar) findViewById(R.id.activity_sale_toolbar);

        setSupportActionBar(toolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            zoomedSale = savedInstanceState.getParcelable(CURRENT_SALE_TAG);
            sales = savedInstanceState.getParcelableArrayList(CURRENT_ARRAY_SALES_TAG);
        }
        if (zoomedSale == null || sales == null) {
            zoomedSale = getIntent().getParcelableExtra(CURRENT_SALE_TAG);
            sales = getIntent().getParcelableArrayListExtra(CURRENT_ARRAY_SALES_TAG);
        }

        int time = 0;
        if (Build.VERSION.SDK_INT >= 21) {
            imageLoader = ImageLoader.getInstance();
            options = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            image = (ImageView) findViewById(R.id.activity_sale_image);
            image.setTransitionName(zoomedSale.getId());
            imageLoader.displayImage(zoomedSale.getSmallImageUrl(), image, options);
            time = 300;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager = (ViewPager) findViewById(R.id.activity_sale_viewpager);
                SampleFragmentPagerAdapter adapter = new SampleFragmentPagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(adapter);
                viewPager.addOnPageChangeListener(adapter);
                viewPager.setCurrentItem(sales.indexOf(zoomedSale));
                viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
            }
        }, time);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolBar.setTitle((sales.indexOf(zoomedSale) + 1) + " " + getString(R.string.string_from) + " " + sales.size());
        if (viewPager != null) {
            viewPager.setKeepScreenOn(sPref.getBoolean(SD.PREFS_DISPLAY_NO_OFF, true));
        }
    }

    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        public SampleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return sales.size();
        }

        @Override
        public Fragment getItem(int position) {
            SaleFragment fragment = new SaleFragment();
            Bundle args = new Bundle();
            args.putParcelable(SaleFragment.CURRENT_SALE_TAG, sales.get(position));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            toolBar.setTitle((position + 1) + " " + getString(R.string.string_from) + " " + sales.size());
            if (Build.VERSION.SDK_INT >= 21) {
                image.setTransitionName(sales.get(position).getId());
                imageLoader.displayImage(sales.get(position).getSmallImageUrl(), image, options);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        viewPager.setVisibility(View.INVISIBLE);
        Intent intent = new Intent();
        intent.putExtra(CURRENT_SALE_TAG, sales.get(viewPager.getCurrentItem()));
        intent.putExtra(CURRENT_ARRAY_SALES_TAG, sales);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(CURRENT_SALE_TAG, sales.get(viewPager.getCurrentItem()));
        outState.putParcelableArrayList(CURRENT_ARRAY_SALES_TAG, sales);
    }
}
