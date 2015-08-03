package com.PopCorp.Sales.Callbacks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.PopCorp.Sales.Controllers.CategoryController;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.Loaders.ShopsInternetLoader;

import java.util.ArrayList;

public class CallBackForShopsFromNet implements LoaderManager.LoaderCallbacks<ArrayList<Shop>> {

    private Context context;
    private CategoryController controller;
    private Category category;

    public CallBackForShopsFromNet(Context context, CategoryController controller, Category category){
        super();
        this.context = context;
        this.controller = controller;
        this.category = category;
    }

    @Override
    public Loader<ArrayList<Shop>> onCreateLoader(int id, Bundle args) {
        Loader<ArrayList<Shop>> result = null;
        if (id == CategoryController.ID_FOR_CREATE_SHOPS_LOADER_FROM_NET) {
            result = new ShopsInternetLoader(context, category);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Shop>> loader, ArrayList<Shop> shops) {
        controller.updateShops(shops);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Shop>> loader) {

    }
}