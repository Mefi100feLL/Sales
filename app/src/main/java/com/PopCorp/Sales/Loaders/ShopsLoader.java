package com.PopCorp.Sales.Loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

import com.PopCorp.Sales.DB.DB;

public class ShopsLoader  extends CursorLoader {

    private final DB db;
    private String selection;

    public ShopsLoader(Context context, DB db, String selection) {
        super(context);
        this.db = db;
        this.selection = selection;
    }

    @Override
    public Cursor loadInBackground() {
        return db.getData(DB.TABLE_SHOPS, DB.COLUMNS_SHOPS, selection);
    }
}