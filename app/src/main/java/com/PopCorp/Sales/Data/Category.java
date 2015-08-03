package com.PopCorp.Sales.Data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.PopCorp.Sales.DB.DB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Category implements Parcelable {

    private String city = "";
    private String name = "";
    private String url = "";

    public Category(String city, String name, String url){
        setCity(city);
        setName(name);
        setUrl(url);
    }

    public Category(Cursor cursor){
        this(cursor.getString(cursor.getColumnIndex(DB.KEY_CITY)), cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGORY_NAME)), cursor.getString(cursor.getColumnIndex(DB.KEY_CATEGORY_URL)));
    }

    public void putInDB(DB db){
        db.addRec(DB.TABLE_CATEGORIES, DB.COLUMNS_CATEGORIES, getFields());
    }

    private String[] getFields() {
        return new String[] {
                getCity(),
                getName(),
                getUrl()
        };
    }

    @Override
    public boolean equals(Object object){
        Category category = (Category) object;
        if (category.getCity().equals(getCity())){
            if (category.getUrl().equals(getUrl())){
                return true;
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getCity());
        dest.writeString(getName());
        dest.writeString(getUrl());
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    private Category(Parcel parcel) {
        setCity(parcel.readString());
        setName(parcel.readString());
        setUrl(parcel.readString());
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
