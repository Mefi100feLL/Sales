package com.PopCorp.Sales.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "PopCorp.Sales.DB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB.CREATE_TABLE_CATEGORIES);
        db.execSQL(DB.CREATE_TABLE_CITYS);
        db.execSQL(DB.CREATE_TABLE_COORDINATES);
        db.execSQL(DB.CREATE_TABLE_GROUPS);
        db.execSQL(DB.CREATE_TABLE_SALES);
        db.execSQL(DB.CREATE_TABLE_SHOPS);
        db.execSQL(DB.CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
