package com.PopCorp.Sales.Data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.PopCorp.Sales.DB.DB;

public class Shop implements Comparable, Parcelable {

    private String name;
    private String image;
    private String city;
    private String category;
    private String url;
    private boolean favorite;

    public Shop(String name, String image, String city, String category, String url, String favorite) {
        setName(name);
        setImage(image);
        setCity(city);
        setCategory(category);
        setUrl(url);
        setFavorite(favorite);
    }

    public Shop(Cursor cursor) {
        this(
                cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_NAME)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_IMAGE)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_CATEGORY)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_URL)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_FAVORITE))
        );
    }

    public void putInDB(DB db) {
        db.addRec(DB.TABLE_SHOPS, DB.COLUMNS_SHOPS, getFields());
    }

    private String[] getFields() {
        return new String[]{
                getName(),
                getImage(),
                getCity(),
                getCategory(),
                getUrl(),
                getFavorite()
        };
    }

    public void update(Shop shop) {
        setName(shop.getName());
        setImage(shop.getImage());
        setCity(shop.getCity());
        setCategory(shop.getCategory());
        setUrl(shop.getUrl());
    }

    public void updateInDB(DB db){
        db.update(DB.TABLE_SHOPS, DB.COLUMNS_SHOPS, DB.KEY_CITY + "='" + getCity() + "' AND " + DB.KEY_SHOP_URL + "='" + getUrl() + "'", getFields());
    }

    public boolean equalsContent(Shop shop) {
        for (int i = 0; i < getFields().length - 2; i++) {
            if (!getFields()[i].equals(shop.getFields()[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        Shop shop = (Shop) object;
        if (getCity().equals(shop.getCity())) {
            if (getUrl().equals(shop.getUrl())) {
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
        dest.writeString(getName());
        dest.writeString(getImage());
        dest.writeString(getCity());
        dest.writeString(getCategory());
        dest.writeString(getUrl());
        dest.writeString(getFavorite());
    }

    public static final Parcelable.Creator<Shop> CREATOR = new Parcelable.Creator<Shop>() {
        public Shop createFromParcel(Parcel in) {
            return new Shop(in);
        }

        public Shop[] newArray(int size) {
            return new Shop[size];
        }
    };

    private Shop(Parcel parcel) {
        setName(parcel.readString());
        setImage(parcel.readString());
        setCity(parcel.readString());
        setCategory(parcel.readString());
        setUrl(parcel.readString());
        setFavorite(parcel.readString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = Boolean.valueOf(favorite);
    }

    public String getFavorite() {
        return String.valueOf(favorite);
    }

    @Override
    public int compareTo(Object another) {
        return getName().compareToIgnoreCase(((Shop) another).getName());
    }
}
