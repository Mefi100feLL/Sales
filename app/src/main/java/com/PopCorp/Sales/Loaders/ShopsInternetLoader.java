package com.PopCorp.Sales.Loaders;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;

import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.Utilites.InternetConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopsInternetLoader  extends AsyncTaskLoader<ArrayList<Shop>> {

    private final String city;
    private Category category;

    public ShopsInternetLoader(Context context, Category category) {
        super(context);
        this.category = category;
        city = PreferenceManager.getDefaultSharedPreferences(context).getString(SD.PREFS_CITY, "");
    }

    @Override
    public ArrayList<Shop> loadInBackground() {
        return getShops();
    }

    private ArrayList<Shop> getShops() {
        ArrayList<Shop> result = new ArrayList<>();
        InternetConnection connection = null;
        String pageInString = null;
        try {
            connection = new InternetConnection(SD.BASE_URL + category.getUrl());
            pageInString = connection.getPageFromGzip();
        } catch (IOException e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        Matcher matcherShop = Pattern.compile("<a data-placement=\"left\" data-toggle=\"tooltip\" title=\"[^\"]*\" href=\"[^\"]*\"> <span class=\"img\"><img src=\"[^\"]*\" alt=\"\" /></span> <span class=\"text\">[^<]*</span>").matcher(pageInString);
        while (matcherShop.find()) {
            String url = "";
            String name = "";
            String image = "";
            String foundedString = matcherShop.group();
            if (!foundedString.contains(category.getName())){
                continue;
            }
            Matcher matcherUrl = Pattern.compile("href=\"[^\"]+").matcher(foundedString);
            if (matcherUrl.find()) {
                url = matcherUrl.group().substring(6);
            }
            Matcher matcherName = Pattern.compile("<span class=\"text\">[^<]*").matcher(foundedString);
            if (matcherName.find()) {
                name = matcherName.group().substring(19);
            }
            Matcher matcherImage = Pattern.compile("src=\"[^\"]+").matcher(foundedString);
            if (matcherImage.find()) {
                image = matcherImage.group().substring(5).replaceFirst("\\-[0-9]+\\.", ".");
            }
            Shop shop = new Shop(name, image, city, category.getUrl(), url, "false");
            if (!result.contains(shop)) {
                result.add(shop);
            }
        }

        return result;
    }
}
