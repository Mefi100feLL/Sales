package com.PopCorp.Sales;

import android.app.Application;
import android.content.Context;

import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Net.API;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.util.HashMap;

import retrofit.RestAdapter;

public class SalesApplication extends Application {

    private API service;

    public API getService() {
        return service;
    }

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    }

    private final HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    private DB db;

    @Override
    public void onCreate() {
        super.onCreate();
        initImageLoader(getApplicationContext());
        db = new DB(this);
        db.open();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://skidkaonline.ru")
                .build();
        service = restAdapter.create(API.class);
    }

    public DB getDB() {
        if (db == null) {
            db = new DB(this);
        }
        if (db.isClosed()) {
            db.open();
        }
        return db;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        db.close();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        db.close();
    }

    public static void initImageLoader(Context context) {
        FileNameGenerator generator = new FileNameGenerator(){
            @Override
            public String generate(String s) {
                String[] split = s.split("/");
                String name = split[split.length-1];
                return name;
            }
        };
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null){
            cacheDir = context.getCacheDir();
        }
        DiskCache diskCache = new UnlimitedDiskCache(cacheDir, context.getCacheDir(), generator);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(diskCache)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(SD.TRACKER_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            t.enableExceptionReporting(true);
            t.enableAutoActivityTracking(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
}
