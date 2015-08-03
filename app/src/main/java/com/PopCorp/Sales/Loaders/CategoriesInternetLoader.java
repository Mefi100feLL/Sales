package com.PopCorp.Sales.Loaders;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;

import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.Utilites.InternetConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoriesInternetLoader extends AsyncTaskLoader<ArrayList<Category>> {

    private final String city;

    public CategoriesInternetLoader(Context context) {
        super(context);
        city = PreferenceManager.getDefaultSharedPreferences(context).getString(SD.PREFS_CITY, "");
    }

    @Override
    public ArrayList<Category> loadInBackground() {
        return getCategories();
    }

    private ArrayList<Category> getCategories() {
        ArrayList<Category> result = new ArrayList<>();
        ArrayList<Category> tmpArray = new ArrayList<>();
        InternetConnection connection = null;
        String page = null;
        try{
            connection = new InternetConnection(SD.BASE_URL + city);
            page = connection.getPageFromGzip();
        } catch(IOException e) {
            return result;
        } finally {
            if (connection!=null){
                connection.disconnect();
            }
        }
        Matcher matcher = Pattern.compile("<a href=\"" + city + "[^\"]+\"><span class=\"text\">[^<]+").matcher(page);
        while (matcher.find()){
            String url = "";
            String name = "";
            String findedString = matcher.group();
            Matcher matcherUrl = Pattern.compile("href=\"" + city + "[^\"]+").matcher(findedString);
            if (matcherUrl.find()){
                url = matcherUrl.group().substring(6);
            }
            Matcher matcherName = Pattern.compile("text\">[^<]+").matcher(findedString);
            if (matcherName.find()){
                name = matcherName.group().substring(6);
            }
            Category category = new Category(city, name, url);
            if (!tmpArray.contains(category)){
                tmpArray.add(category);
            }
        }

        ArrayList<String> realCategories = new ArrayList<>();
        Matcher matcherReal = Pattern.compile("<a data-placement=\"left\" data-toggle=\"tooltip\" title=\"[^\"]+").matcher(page);
        while (matcherReal.find()){
            String findedString = matcherReal.group();
            Matcher matcherName = Pattern.compile("title=\"[^\"]+").matcher(findedString);
            if (matcherName.find()){
                String name = matcherName.group().substring(7);
                if (!realCategories.contains(name)) {
                    realCategories.add(name);
                }
            }
        }

        for (String name : realCategories){
            for (Category category : tmpArray){
                if (name.equals(category.getName()) && !result.contains(category)){
                    result.add(category);
                }
            }
        }
        return result;
    }
}