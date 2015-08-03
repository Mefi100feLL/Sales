package com.PopCorp.Sales.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.PopCorp.Sales.Data.City;

import java.util.ArrayList;

public class DB {

    public static final String TABLE_SHOPS = "shops";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_COORDINATES = "coordinates";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_CITYS = "citys";
    public static final String TABLE_SALES = "sales";
    public static final String TABLE_USERS = "users";

    public static final String KEY_ID = "_id";
    public static final String KEY_CITY = "city";

    //////////////////////////////////////////////////////// USERS ///////////////////////////////////////////////////////
    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_COOKIE = "cookie";

    public static final String[] COLUMNS_USERS = new String[]{KEY_USER_ID, KEY_USER_NAME, KEY_USER_EMAIL, KEY_USER_COOKIE};

    public static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_USER_ID + " text, "
            + KEY_USER_NAME + " text, "
            + KEY_USER_EMAIL + " text, "
            + KEY_USER_COOKIE + " text);";

    //////////////////////////////////////////////////////// CATEGORIES ///////////////////////////////////////////////////////
    public static final String KEY_CATEGORY_NAME = "name";
    public static final String KEY_CATEGORY_URL = "url";

    public static final String[] COLUMNS_CATEGORIES = new String[]{KEY_CITY, KEY_CATEGORY_NAME, KEY_CATEGORY_URL};

    public static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_CITY + " text, "
            + KEY_CATEGORY_NAME + " text, "
            + KEY_CATEGORY_URL + " text);";


    //////////////////////////////////////////////////////// COORDINATES ///////////////////////////////////////////////////////
    public static final String KEY_COORDINATE_SHOP_ID = "shop_id";
    public static final String KEY_COORDINATE_LATITUDE = "latitude";
    public static final String KEY_COORDINATE_LONGITUDE = "longitude";
    public static final String KEY_COORDINATE_ADDRESS = "address";
    public static final String KEY_COORDINATE_DESCRIPTION = "description";

    public static final String[] COLUMNS_COORDINATES = new String[]{KEY_COORDINATE_SHOP_ID, KEY_COORDINATE_LATITUDE, KEY_COORDINATE_LONGITUDE, KEY_COORDINATE_ADDRESS, KEY_COORDINATE_DESCRIPTION};

    public static final String CREATE_TABLE_COORDINATES = "CREATE TABLE IF NOT EXISTS " + TABLE_COORDINATES +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_CITY + " text, "
            + KEY_COORDINATE_SHOP_ID + " text, "
            + KEY_COORDINATE_LATITUDE + " text, "
            + KEY_COORDINATE_LONGITUDE + " text, "
            + KEY_COORDINATE_ADDRESS + " text, "
            + KEY_COORDINATE_DESCRIPTION + " text);";


    ///////////////////////////////////////////////////////////// GROUPS ////////////////////////////////////////////////
    public static final String KEY_GROUP_NAME = "name";
    public static final String KEY_GROUP_PERIOD = "period";
    public static final String KEY_GROUP_URL = "url";

    public static final String[] COLUMNS_GROUPS = new String[]{KEY_CITY, KEY_GROUP_NAME, KEY_GROUP_PERIOD, KEY_GROUP_URL};

    public static final String CREATE_TABLE_GROUPS = "CREATE TABLE IF NOT EXISTS " + TABLE_GROUPS +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_CITY + " text, "
            + KEY_GROUP_NAME + " text, "
            + KEY_GROUP_PERIOD + " text, "
            + KEY_GROUP_URL + " text);";


    ////////////////////////////////////////////////////////////// CITYS ///////////////////////////////////////////////////
    public static final String KEY_CITY_REL = "rel";
    public static final String KEY_CITY_LATITUDE = "latitude";
    public static final String KEY_CITY_LONGITUDE = "longitude";
    public static final String KEY_CITY_REGION = "region";
    public static final String KEY_CITY_NAME = "name";
    public static final String KEY_CITY_URL = "url";

    public static final String[] COLUMNS_CITYS = new String[]{KEY_CITY_REL, KEY_CITY_LATITUDE, KEY_CITY_LONGITUDE, KEY_CITY_REGION, KEY_CITY_NAME, KEY_CITY_URL};

    public static final String CREATE_TABLE_CITYS = "CREATE TABLE IF NOT EXISTS " + TABLE_CITYS +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_CITY_REL + " text, "
            + KEY_CITY_LATITUDE + " text, "
            + KEY_CITY_LONGITUDE + " text, "
            + KEY_CITY_REGION + " text, "
            + KEY_CITY_NAME + " text, "
            + KEY_CITY_URL + " text);";

    ///////////////////////////////////////////////////////////// SALES /////////////////////////////////////////////////////////////////
    public static final String KEY_SALE_ID = "id";
    public static final String KEY_SALE_SHOP = "shop";
    public static final String KEY_SALE_GROUP = "_group";
    public static final String KEY_SALE_PERIOD = "period";
    public static final String KEY_SALE_IMAGE = "image";
    public static final String KEY_SALE_SMALL_IMAGE = "small_image";
    public static final String KEY_SALE_WIDTH = "width";
    public static final String KEY_SALE_HEIGHT = "height";
    public static final String KEY_SALE_FAVORITE = "favorite";

    public static final String[] COLUMNS_SALES = new String[]{KEY_SALE_ID, KEY_SALE_SHOP, KEY_SALE_GROUP, KEY_SALE_PERIOD, KEY_CITY, KEY_SALE_IMAGE, KEY_SALE_SMALL_IMAGE, KEY_SALE_WIDTH, KEY_SALE_HEIGHT, KEY_SALE_FAVORITE};

    public static final String CREATE_TABLE_SALES = "CREATE TABLE IF NOT EXISTS " + TABLE_SALES +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_CITY + " text, "
            + KEY_SALE_ID + " text, "
            + KEY_SALE_SHOP + " text, "
            + KEY_SALE_GROUP + " text, "
            + KEY_SALE_PERIOD + " text, "
            + KEY_SALE_FAVORITE + " text, "
            + KEY_SALE_IMAGE + " text, "
            + KEY_SALE_SMALL_IMAGE + " text, "
            + KEY_SALE_WIDTH + " text, "
            + KEY_SALE_HEIGHT + " text);";


    ///////////////////////////////////////////////////////////// SHOPS //////////////////////////////////////////////////////
    public static final String KEY_SHOP_NAME = "name";
    public static final String KEY_SHOP_IMAGE = "image";
    public static final String KEY_SHOP_CATEGORY = "category";
    public static final String KEY_SHOP_URL = "url";
    public static final String KEY_SHOP_FAVORITE = "favorite";

    public static final String[] COLUMNS_SHOPS = new String[]{KEY_SHOP_NAME, KEY_SHOP_IMAGE, KEY_CITY, KEY_SHOP_CATEGORY, KEY_SHOP_URL, KEY_SHOP_FAVORITE};

    public static final String CREATE_TABLE_SHOPS = "CREATE TABLE IF NOT EXISTS " + TABLE_SHOPS +
            "( " + KEY_ID + " integer primary key autoincrement, "
            + KEY_CITY + " text, "
            + KEY_SHOP_NAME + " text, "
            + KEY_SHOP_IMAGE + " text, "
            + KEY_SHOP_CATEGORY + " text, "
            + KEY_SHOP_URL + " text, "
            + KEY_SHOP_FAVORITE + " text);";

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final Context context;
    private DBHelper DBHelper;
    private SQLiteDatabase db;
    private boolean closed;

    public DB(Context context) {
        this.context = context.getApplicationContext();
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public void open() {
        DBHelper = new DBHelper(context);
        try {
            db = DBHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = DBHelper.getReadableDatabase();
        }
        closed = false;
    }

    public void openToRead() {
        DBHelper = new DBHelper(context);
        try {
            db = DBHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            return;
        }
        closed = false;
    }

    public void close() {
        if (DBHelper != null) {
            DBHelper.close();
        }
        closed = true;
    }

    public Cursor getAllData(String table) {
        try {
            return db.query(table, null, null, null, null, null, null);
        } catch (SQLiteException e) {
            return null;
        }
    }

    public long addRec(String table, String[] columns, String[] values) {
        try {
            ContentValues cv = new ContentValues();
            for (int i = 0; i < columns.length; i++) {
                cv.put(columns[i], values[i]);
            }
            return db.insert(table, null, cv);
        } catch (SQLiteException e) {
            return -1;
        }
    }

    public Cursor getData(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        try {
            return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        } catch (SQLiteException e) {
            return null;
        }
    }

    public Cursor getData(String table, String[] columns, String selection) {
        try {
            return db.query(table, columns, selection, null, null, null, null);
        } catch (SQLiteException e) {
            return null;
        }
    }

    public void deleteRows(String table, String uslovie) {
        try {
            if (uslovie == null) {
                db.execSQL("DELETE FROM " + table);
            } else {
                db.execSQL("DELETE FROM " + table + " WHERE " + uslovie);
            }
        } catch (SQLiteException ignored) {
        }
    }

    public void update(String table, String uslovie, String column, String value) {
        try {
            db.execSQL("UPDATE " + table + " SET " + column + "='" + value + "' WHERE " + uslovie + ";");
        } catch (SQLiteException ignored) {
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(table, values, whereClause, whereArgs);
    }

    public int update(String table, String[] columns, String uslovie, String[] values) {
        ContentValues cv = new ContentValues();
        for (int i = 0; i < columns.length; i++) {
            cv.put(columns[i], values[i]);
        }
        return db.update(table, cv, uslovie, null);
    }

    public ArrayList<City> getCitys() {
        ArrayList<City> result = new ArrayList<>();
        Cursor cursor = getAllData(DB.TABLE_CITYS);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result.add(new City(cursor));
                while (cursor.moveToNext()) {
                    result.add(new City(cursor));
                }
            }
            cursor.close();
        }
        return result;
    }

    public Context getContext() {
        return context;
    }

    public Cursor getCategories(String city) {
        return getData(DB.TABLE_CATEGORIES, DB.COLUMNS_CATEGORIES, DB.KEY_CITY + "='" + city + "'");
    }
}
