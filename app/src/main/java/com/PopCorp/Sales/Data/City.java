package com.PopCorp.Sales.Data;

import android.database.Cursor;

import com.PopCorp.Sales.DB.DB;

public class City {

    private String rel;
    private String latitude;
    private String longitude;
    private String region;
    private String name;
    private String url;

    public City(String rel, String latitude, String longitude, String region, String name, String url){
        setRel(rel);
        setLatitude(latitude);
        setLongitude(longitude);
        setRegion(region);
        setName(name);
        setUrl(url);
    }

    public City(String region, String name, String url){
        setRel("");
        setLatitude("");
        setLongitude("");
        setRegion(region);
        setName(name);
        setUrl(url);
    }

    public City(Cursor cursor){
        this(
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_REL)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_LATITUDE)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_LONGITUDE)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_REGION)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_NAME)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_URL))
        );
    }

    public void putInDB(DB db){
        db.addRec(DB.TABLE_CITYS, DB.COLUMNS_CITYS, getFields());
    }

    public String[] getFields(){
        return new String[]{
                getRel(),
                getLatitude(),
                getLongitude(),
                getRegion(),
                getName(),
                getUrl()
        };
    }

    @Override
    public boolean equals(Object object){
        City city = (City) object;
        if (name.equals(city.getName())){
            if (region.equals(city.getRegion())){
                return true;
            }
        }
        return false;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
