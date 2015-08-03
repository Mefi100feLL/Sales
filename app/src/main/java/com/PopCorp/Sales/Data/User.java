package com.PopCorp.Sales.Data;

import android.database.Cursor;

import com.PopCorp.Sales.DB.DB;

import java.net.HttpCookie;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {

    private String id;
    private String name;
    private String email;
    private String cookie;

    public User(String id, String name, String email, String cookie){
        setId(id);
        setName(name);
        setEmail(email);
        setCookie(cookie);
    }

    public User(Cursor cursor){
        this(
                cursor.getString(cursor.getColumnIndex(DB.KEY_USER_ID)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_USER_NAME)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_USER_EMAIL)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_USER_COOKIE)));
    }

    public User(String body, String email, HttpCookie cookie){
        setEmail(email);
        setCookie(cookie.getName() + "=" + cookie.getValue());

        Matcher matcherName = Pattern.compile("<h2>[^<]+<").matcher(body);
        if (matcherName.find()){
            String tmpString = matcherName.group();
            setName(tmpString.substring(4, tmpString.length()-1));
        }

        Matcher matcherId = Pattern.compile("userId: [0-9]*").matcher(body);
        if (matcherId.find()){
            String tmpString = matcherId.group();
            setId(tmpString.substring(8));
        }
    }

    public void putInDB(DB db){
        db.addRec(DB.TABLE_USERS, DB.COLUMNS_USERS, getFields());
    }

    public String[] getFields(){
        return new String[]{
                getId(),
                getName(),
                getEmail(),
                getCookie()
        };
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
