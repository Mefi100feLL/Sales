package com.PopCorp.Sales.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.PopCorp.Sales.DB.DB;

public class CitysLoader extends CursorLoader {

    private final DB db;

    public CitysLoader(Context context, DB db) {
        super(context);
        this.db = db;
    }

    @Override
    public Cursor loadInBackground() {
        return db.getAllData(DB.TABLE_CITYS);
    }
}