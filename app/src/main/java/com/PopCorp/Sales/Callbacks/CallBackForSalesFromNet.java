package com.PopCorp.Sales.Callbacks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.PopCorp.Sales.Controllers.CategoryController;
import com.PopCorp.Sales.Controllers.SalesController;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.Loaders.SalesInternetLoader;

import java.util.ArrayList;

public class CallBackForSalesFromNet  implements LoaderManager.LoaderCallbacks<ArrayList<Sale>> {

    private final DB db;
    private Context context;
    private SalesController controller;
    private Shop shop;
    private final Category category;

    public CallBackForSalesFromNet(Context context, SalesController controller, Shop shop, Category category, DB db){
        super();
        this.context = context;
        this.controller = controller;
        this.shop = shop;
        this.category = category;
        this.db = db;
    }

    @Override
    public Loader<ArrayList<Sale>> onCreateLoader(int id, Bundle args) {
        Loader<ArrayList<Sale>> result = null;
        if (id == CategoryController.ID_FOR_CREATE_SHOPS_LOADER_FROM_NET) {
            result = new SalesInternetLoader(context, shop, category, db);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Sale>> loader, ArrayList<Sale> sales) {
        controller.updateSales(sales);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Sale>> loader) {

    }
}