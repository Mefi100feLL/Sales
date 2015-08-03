package com.PopCorp.Sales.Callbacks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.PopCorp.Sales.Controllers.CitysController;
import com.PopCorp.Sales.Data.City;
import com.PopCorp.Sales.Loaders.CitysInternetLoader;

import java.util.ArrayList;

public class CallBackForCitysFromNet implements LoaderManager.LoaderCallbacks<ArrayList<City>> {

    private Context context;
    private CitysController controller;

    public CallBackForCitysFromNet(Context context, CitysController controller){
        super();
        this.context = context;
        this.controller = controller;
    }

    @Override
    public Loader<ArrayList<City>> onCreateLoader(int id, Bundle args) {
        Loader<ArrayList<City>> result = null;
        if (id == CitysController.ID_FOR_CREATE_CITYS_LOADER_FROM_NET) {
            result = new CitysInternetLoader(context);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<City>> loader, ArrayList<City> citys) {
        controller.updateCitys(citys);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<City>> loader) {

    }
}