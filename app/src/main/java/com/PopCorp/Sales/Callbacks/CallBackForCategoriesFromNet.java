package com.PopCorp.Sales.Callbacks;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.PopCorp.Sales.Controllers.MainController;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Loaders.CategoriesInternetLoader;

import java.util.ArrayList;

public class CallBackForCategoriesFromNet implements LoaderManager.LoaderCallbacks<ArrayList<Category>> {

    private Context context;
    private MainController controller;

    public CallBackForCategoriesFromNet(Context context, MainController controller){
        super();
        this.context = context;
        this.controller = controller;
    }

    @Override
    public Loader<ArrayList<Category>> onCreateLoader(int id, Bundle args) {
        Loader<ArrayList<Category>> result = null;
        if (id == MainController.ID_FOR_CREATE_CATEGORIES_LOADER_FROM_NET) {
            result = new CategoriesInternetLoader(context);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Category>> loader, ArrayList<Category> categories) {
        controller.updateCategories(categories);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Category>> loader) {

    }
}
