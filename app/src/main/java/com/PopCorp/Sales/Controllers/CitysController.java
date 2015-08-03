package com.PopCorp.Sales.Controllers;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.PopCorp.Sales.Adapters.CitysAdapter;
import com.PopCorp.Sales.Callbacks.CallBackForCitysFromNet;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.City;
import com.PopCorp.Sales.Loaders.CitysLoader;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;

import java.util.ArrayList;

public class CitysController implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final int ID_FOR_CREATE_CITYS_LOADER_FROM_DB = 1;
    public static final int ID_FOR_CREATE_CITYS_LOADER_FROM_NET = 2;

    private DB db;
    private final AppCompatActivity context;
    private final CitysAdapter adapter;
    private final ArrayList<City> citys = new ArrayList<>();
    private final SharedPreferences sPref;
    private final UICallback uiCallback;

    public interface UICallback extends UIFragmentCallback{
        void setSelection(int position);
    }

    public CitysController(AppCompatActivity context){
        this.context = context;
        this.uiCallback = (UICallback) context;
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        db = ((SalesApplication) context.getApplication()).getDB();
        adapter = new CitysAdapter(context, citys);
    }

    public CitysAdapter getAdapter() {
        return adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;
        if (id == ID_FOR_CREATE_CITYS_LOADER_FROM_DB) {
            result = new CitysLoader(context, db);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                addCityFromCursor(cursor);
                while (cursor.moveToNext()) {
                    addCityFromCursor(cursor);
                }
            }
        }
        if (citys.size()!=0){
            uiCallback.showListView();
        }
        adapter.setObjects(citys);
        for (City city : citys){
            if (city.getUrl().equals(sPref.getString(SD.PREFS_CITY, ""))){
                adapter.setSelectedCity(citys.indexOf(city));
            }
        }
        adapter.notifyDataSetChanged();
        if (adapter.getSelectedCity()!=null) {
            uiCallback.setSelection(citys.indexOf(adapter.getSelectedCity()));
        }
        startLoaderFromNet();
    }

    private void addCityFromCursor(Cursor cursor) {
        City city = new City(cursor);
        if (!citys.contains(city)){
            citys.add(city);
        }
    }

    private void startLoaderFromNet() {
        context.getSupportLoaderManager().initLoader(ID_FOR_CREATE_CITYS_LOADER_FROM_NET, new Bundle(), new CallBackForCitysFromNet(context, this));
        Loader<ArrayList<City>> citysLoaderFromNET = context.getSupportLoaderManager().getLoader(ID_FOR_CREATE_CITYS_LOADER_FROM_NET);
        citysLoaderFromNET.forceLoad();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateCitys(ArrayList<City> newCitys) {
        uiCallback.hideRefresh();
        if (newCitys!=null) {
            for (City city : newCitys) {
                if (!citys.contains(city)) {
                    citys.add(city);
                    city.putInDB(db);
                }
            }
        }
        if (citys.size()!=0){
            uiCallback.showListView();
        } else{
            if (newCitys!=null){
                uiCallback.showEmpty(R.string.empty_no_citys, R.drawable.ic_empty_location);
            } else{
                uiCallback.showEmpty(R.string.empty_no_internet, R.drawable.ic_empty_wifi);
            }
        }
        adapter.setObjects(citys);
        adapter.notifyDataSetChanged();
    }
}
