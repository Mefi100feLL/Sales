package com.PopCorp.Sales.Data;

import android.database.Cursor;

import com.PopCorp.Sales.DB.DB;

public class Group {

    private String city;
    private String name;
    private String period;
    private String url;

    public Group(String city, String name, String period, String url){
        setCity(city);
        setName(name);
        setPeriod(period);
        setUrl(url);
    }

    public Group(Cursor cursor){
        this(
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_GROUP_NAME)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_GROUP_PERIOD)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_GROUP_URL))
        );
    }

    public void putInDB(DB db){
        db.addRec(DB.TABLE_GROUPS, DB.COLUMNS_GROUPS, getFields());
    }

    private String[] getFields() {
        return new String[] {
                getCity(),
                getName(),
                getPeriod(),
                getUrl()
        };
    }

    public boolean equalsContent(Group group) {
        for (int i = 0; i < getFields().length; i++) {
            if (!getFields()[i].equals(group.getFields()[i])) {
                return false;
            }
        }
        return true;
    }

    public void update(Group group) {
        setPeriod(group.getPeriod());
        setUrl(group.getUrl());
    }

    public void updateInDB(DB db){
        db.update(DB.TABLE_GROUPS, DB.COLUMNS_GROUPS, DB.KEY_CITY + "='" + getCity() + "' AND " + DB.KEY_GROUP_URL + "='" + getUrl() + "'", getFields());
    }

    @Override
    public boolean equals(Object object){
        Group group = (Group) object;
        if (getUrl().equals(group.getUrl())){
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
