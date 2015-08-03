package com.PopCorp.Sales.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.CursorLoader;

import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.SD;

public class CategoriesLoader extends CursorLoader {

    private final DB db;
    private String city;

    public CategoriesLoader(Context context, DB db) {
        super(context);
        city = PreferenceManager.getDefaultSharedPreferences(context).getString(SD.PREFS_CITY, "");
        this.db = db;
    }

    @Override
    public Cursor loadInBackground() {
        return db.getCategories(city);
    }
}