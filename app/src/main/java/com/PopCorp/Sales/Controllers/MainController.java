package com.PopCorp.Sales.Controllers;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.PopCorp.Sales.Callbacks.CallBackForCategoriesFromNet;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Loaders.CategoriesLoader;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SalesApplication;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import java.util.ArrayList;
import java.util.Iterator;

public class MainController implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int ID_FOR_CREATE_CATEGORIES_LOADER_FROM_DB = 1;
    public static final int ID_FOR_CREATE_CATEGORIES_LOADER_FROM_NET = 2;

    private final DB db;
    private AppCompatActivity context;
    private Drawer drawer;
    private ArrayList<Category> categories = new ArrayList<>();

    public MainController(AppCompatActivity context, Drawer drawer) {
        this.context = context;
        this.drawer = drawer;
        db = ((SalesApplication) context.getApplication()).getDB();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;
        if (id == ID_FOR_CREATE_CATEGORIES_LOADER_FROM_DB) {
            result = new CategoriesLoader(context, db);
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        categories.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                addCategoryFromCursor(cursor);
                while (cursor.moveToNext()) {
                    addCategoryFromCursor(cursor);
                }
            }
        }
        if (drawer.getDrawerItems().size() > 3) {
            int size = drawer.getDrawerItems().size()-3;
            for (int i = 0; i < size; i++) {
                drawer.removeItem(i);
            }
        }
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            drawer.addItem(new PrimaryDrawerItem().withName(category.getName()), i);
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int position = drawer.getCurrentSelection();
                if (position==-1){
                    drawer.setSelectionByIdentifier(R.string.drawer_item_my_notes);
                } else{
                    drawer.setSelection(drawer.getCurrentSelection());
                }
            }
        });
        startLoaderFromNet();
    }

    private void addCategoryFromCursor(Cursor cursor) {
        Category category = new Category(cursor);
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    public void startLoaderFromNet() {
        context.getSupportLoaderManager().restartLoader(ID_FOR_CREATE_CATEGORIES_LOADER_FROM_NET, new Bundle(), new CallBackForCategoriesFromNet(context, this));
        Loader<ArrayList<Category>> categoriesLoaderFromNET = context.getSupportLoaderManager().getLoader(ID_FOR_CREATE_CATEGORIES_LOADER_FROM_NET);
        categoriesLoaderFromNET.forceLoad();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateCategories(ArrayList<Category> newCategories) {
        if (newCategories.size()==0){
            return;
        }
        Iterator<Category> iterator = categories.listIterator();
        while (iterator.hasNext()){
            Category category = iterator.next();
            if (!newCategories.contains(category)) {
                drawer.removeItem(categories.indexOf(category));
                iterator.remove();
            }
        }
        for (Category category : newCategories) {
            if (!categories.contains(category)) {
                drawer.addItem(new PrimaryDrawerItem().withName(category.getName()), categories.size());
                categories.add(category);
                category.putInDB(db);
            }
        }
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }
}
