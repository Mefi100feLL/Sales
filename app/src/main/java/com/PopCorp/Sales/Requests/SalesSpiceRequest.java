package com.PopCorp.Sales.Requests;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Group;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Data.Sales;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.Utilites.InternetConnection;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.request.SpiceRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalesSpiceRequest extends SpiceRequest<Sales> {

    private final Context context;
    private final ArrayList<Shop> shops;
    private final Category category;
    private final DB db;
    private final String city;
    private final ImageLoader imageLoader;
    private final DisplayImageOptions options;

    public SalesSpiceRequest(Context context, ArrayList<Shop> shopes, Category category, DB db) {
        super(Sales.class);
        this.context = context;
        this.shops = shopes;
        this.category = category;
        this.db = db;
        city = PreferenceManager.getDefaultSharedPreferences(context).getString(SD.PREFS_CITY, "");
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public Sales loadDataFromNetwork() throws Exception {
        ArrayList<Sale> result = new ArrayList<>();
        for (Shop shop : shops){
            result.addAll(getSales(shop));
        }
        return (Sales) result;
    }

    private ArrayList<Sale> getSales(Shop shop) {
        ArrayList<Sale> result = new ArrayList<>();
        int page = 1;
        int count = 0;
        ArrayList<Group> groups = new ArrayList<>();
        while (true) {
            InternetConnection connection = null;
            String pageInString = null;
            try {
                connection = new InternetConnection(SD.BASE_URL + shop.getUrl() + "?page=" + page++ + "&is_ajax=1", InternetConnection.REQUEST_METHOD_POST);
                pageInString = connection.getPageFromGzip();
            } catch (IOException e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            ArrayList<Group> allGroups = new ArrayList<>();
            Matcher matcherGroup = Pattern.compile("<a class=\"discount-link\" href=\"[^\"]+\">(<strong>)?[^<]+(</strong>)?(<br/>)?\\([^\\)]+\\)</a>").matcher(pageInString);
            while (matcherGroup.find()) {
                String url = "";
                String name = "";
                String period = "";
                String foundedString = matcherGroup.group();
                Matcher matcherUrl = Pattern.compile("href=\"[^\"]+").matcher(foundedString);
                if (matcherUrl.find()) {
                    url = matcherUrl.group().substring(6);
                }
                Matcher matcherName = Pattern.compile("\">(<strong>)?[^<]+").matcher(foundedString);
                if (matcherName.find()) {
                    String tmpString = matcherName.group();
                    if (tmpString.contains("strong")) {
                        name = tmpString.substring(10);
                    } else {
                        name = tmpString.substring(2);
                    }
                }
                Matcher matcherPeriod = Pattern.compile("<br/>\\([^\\)]+\\)+").matcher(foundedString);
                if (matcherPeriod.find()) {
                    String tmpString = matcherPeriod.group();
                    period = tmpString.substring(6, tmpString.length() - 1);
                }
                Group group = new Group(city, name, period, url);
                allGroups.add(group);
                if (!groups.contains(group)){
                    groups.add(group);
                }
            }

            ArrayList<Sale> sales = new ArrayList<>();
            Matcher matcherSale = Pattern.compile("<a href=\"[^\"]+\" class=\"image-link\"><span class=\"over\">&nbsp;</span> <img src=\"[^\"]+\" width=\"[0-9]*\" height=\"[0-9]*\"").matcher(pageInString);
            while (matcherSale.find()) {
                String id = "";
                String smallImage = "";
                String width = "";
                String height = "";
                String foundedString = matcherSale.group();
                Matcher matcherId = Pattern.compile("<a href=\"[^\"]+").matcher(foundedString);
                if (matcherId.find()) {
                    String tmpString = matcherId.group();
                    String[] array = tmpString.split("/");
                    id = array[array.length - 1];
                }
                Matcher matcherImage = Pattern.compile("<img src=\"[^\"]+").matcher(foundedString);
                if (matcherImage.find()) {
                    smallImage = matcherImage.group().substring(10);
                }
                Matcher matcherWidth = Pattern.compile("width=\"[0-9]*").matcher(foundedString);
                if (matcherWidth.find()) {
                    width = matcherWidth.group().substring(7);
                }
                Matcher matcherHeight = Pattern.compile("height=\"[0-9]*").matcher(foundedString);
                if (matcherHeight.find()) {
                    height = matcherHeight.group().substring(8);
                }
                String image = smallImage.replaceAll("\\-[0-9]+\\.", ".");
                Sale sale = new Sale(id, "", "", "", "false", city, image, smallImage, width, height);
                sales.add(sale);
            }

            if (allGroups.size() == sales.size()) {
                for (int i = 0; i < sales.size(); i++) {
                    Sale sale = sales.get(i);
                    if (result.contains(sale)) {
                        count++;
                    } else {
                        sale.setShop(shop.getUrl());
                        sale.setGroup(allGroups.get(i).getUrl());
                        sale.setPeriod(allGroups.get(i).getPeriod());
                        result.add(sale);
                        imageLoader.loadImage(sale.getSmallImageUrl(), options, null);
                        if (sale.updateInDB(db)==0){
                            sale.putInDB(db);
                        }
                    }
                }
                if (count >=30 || sales.size()==0) {
                    break;
                }
            } else {
                break;
            }
        }
        ArrayList<Group> tmpGroups = new ArrayList<>();
        Cursor cursor = db.getData(DB.TABLE_GROUPS, DB.COLUMNS_GROUPS, DB.KEY_CITY + "='" + city + "'");
        if (cursor!=null){
            if (cursor.moveToFirst()){
                tmpGroups.add(new Group(cursor));
                while (cursor.moveToNext()){
                    tmpGroups.add(new Group(cursor));
                }
            }
            cursor.close();
        }
        for (Group group : groups){
            if (tmpGroups.contains(group)){
                Group tmpGroup = tmpGroups.get(tmpGroups.indexOf(group));
                if (!group.equalsContent(tmpGroup)){
                    tmpGroup.update(group);
                    tmpGroup.updateInDB(db);
                }
            } else{
                group.putInDB(db);
            }
        }
        return result;
    }

}