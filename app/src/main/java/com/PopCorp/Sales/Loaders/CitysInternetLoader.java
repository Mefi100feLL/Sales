package com.PopCorp.Sales.Loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.PopCorp.Sales.Data.City;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.Utilites.InternetConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CitysInternetLoader extends AsyncTaskLoader<ArrayList<City>> {

    public CitysInternetLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<City> loadInBackground() {
        return getCitys();
    }

    private ArrayList<City> getCitys() {
        ArrayList<City> result = new ArrayList<>();
        ArrayList<String> urlsPages = getPagesUrls();
        if (urlsPages==null){
            return null;
        }
        for (String urlPage : urlsPages){
            InternetConnection connection = null;
            String page = null;
            try{
                connection = new InternetConnection(urlPage);
                page = connection.getPageFromGzip();
            } catch(IOException e) {
                return null;
            } finally {
                if (connection!=null){
                    connection.disconnect();
                }
            }
            Matcher matcher = Pattern.compile("<a data-toggle=\"tooltip\" title=\"[^\"]*\" href=\"[^\"]+\"><span class=\"text\">[^<]+</span>").matcher(page);
            while (matcher.find()){
                String region = "";
                String name = "";
                String url = "";
                String findedString = matcher.group();
                Matcher matcherRegion = Pattern.compile("title=\"[^\"]*").matcher(findedString);
                if (matcherRegion.find()){
                    region = matcherRegion.group().substring(7);
                }
                Matcher matcherName = Pattern.compile("text\">[^<]+").matcher(findedString);
                if (matcherName.find()){
                    name = matcherName.group().substring(6);
                }
                Matcher matcherUrl = Pattern.compile("href=\"[^\"]+").matcher(findedString);
                if (matcherUrl.find()){
                    url = matcherUrl.group().substring(6);
                }
                City city = new City(region, name, url);
                if (!result.contains(city)){
                    result.add(city);
                }
            }
        }
        return result;
    }

    private ArrayList<String> getPagesUrls() {
        ArrayList<String> result = new ArrayList<>();
        InternetConnection connection = null;
        String page = null;
        try{
            connection = new InternetConnection(SD.URL_CITYS);
            page = connection.getPageFromGzip();
        } catch(IOException e) {
            return null;
        } finally {
            if (connection!=null){
                connection.disconnect();
            }
        }
        if (page!=null && !page.isEmpty()){
            Matcher matcherUrls = Pattern.compile("cities/\\?a=[^\"]+").matcher(page);
            while (matcherUrls.find()){
                String findedString = matcherUrls.group();
                result.add(SD.BASE_URL + findedString);
            }
        }
        return result;
    }
}