package com.PopCorp.Sales.Controllers;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.PopCorp.Sales.Adapters.CategoryAdapter;
import com.PopCorp.Sales.Callbacks.CallBackForShopsFromNet;
import com.PopCorp.Sales.Callbacks.ShopsFilterListener;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.City;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.Fragments.SalesFragment;
import com.PopCorp.Sales.Loaders.ShopsLoader;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;

import java.util.ArrayList;

public class CategoryController implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int ID_FOR_CREATE_SHOPS_LOADER_FROM_DB = 1;
    public static final int ID_FOR_CREATE_SHOPS_LOADER_FROM_NET = 2;

    private final DB db;
    private final Category currentCategory;
    private Fragment fragment;
    private final CategoryAdapter adapter;
    private ArrayList<Shop> shops = new ArrayList<>();
    private String city;
    private AppCompatActivity context;
    private String currentFilter = CategoryAdapter.FILTER_TYPE_ALL;
    private ShopsFilterListener menuListener;
    private int checkedId = R.id.action_filter_all;
    private UIFragmentCallback uiController;

    public void openShop(Shop shop) {
        Fragment fragment = new SalesFragment();
        Bundle args = new Bundle();
        args.putParcelable(SalesFragment.CURRENT_CATEGORY_TAG, currentCategory);
        args.putParcelable(SalesFragment.CURRENT_SHOP_TAG, shop);
        fragment.setArguments(args);
        FragmentManager fragmentManager = context.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        try {
            transaction.replace(R.id.activity_main_content_frame, fragment, SalesFragment.TAG).addToBackStack(SalesFragment.TAG).commit();
        } catch (IllegalStateException ignored) {
        }
    }

    public CategoryController(AppCompatActivity context, Category category, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        uiController = (UIFragmentCallback) fragment;
        this.menuListener = (ShopsFilterListener) fragment;
        currentCategory = category;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        city = sPref.getString(SD.PREFS_CITY, "");
        db = ((SalesApplication) context.getApplication()).getDB();
        adapter = new CategoryAdapter(this, shops);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;
        if (id == ID_FOR_CREATE_SHOPS_LOADER_FROM_DB) {
            result = new ShopsLoader(context, db, DB.KEY_CITY + "='" + city + "' AND " + DB.KEY_SHOP_CATEGORY + "='" + currentCategory.getUrl() + "'");
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                addShopFromCursor(cursor);
                while (cursor.moveToNext()) {
                    addShopFromCursor(cursor);
                }
            }
        }
        if (shops.size() > 0) {
            uiController.showListView();
            for (Shop shop : shops) {
                if (shop.isFavorite()) {
                    checkedId = R.id.action_filter_favorite;
                }
            }
            menuListener.onShowMenu();
            filter(checkedId);
            menuListener.onCheckMenuItem(checkedId);
        } else {
            menuListener.onHideMenu();
        }
        startLoaderFromNet();
    }

    public void filter(int itemId) {
        checkedId = itemId;
        switch (itemId) {
            case R.id.action_filter_favorite: {
                adapter.getFilter().filter(CategoryAdapter.FILTER_TYPE_FAVORITE);
                currentFilter = CategoryAdapter.FILTER_TYPE_FAVORITE;
                break;
            }
            case R.id.action_filter_all: {
                adapter.getFilter().filter(CategoryAdapter.FILTER_TYPE_ALL);
                currentFilter = CategoryAdapter.FILTER_TYPE_ALL;
                break;
            }
        }
    }

    private void addShopFromCursor(Cursor cursor) {
        Shop shop = new Shop(cursor);
        if (!shops.contains(shop)) {
            shops.add(shop);
        }
    }

    public void startLoaderFromNet() {
        fragment.getLoaderManager().restartLoader(ID_FOR_CREATE_SHOPS_LOADER_FROM_NET, new Bundle(), new CallBackForShopsFromNet(context, this, currentCategory));
        Loader<ArrayList<City>> shopsLoaderFromNET = fragment.getLoaderManager().getLoader(ID_FOR_CREATE_SHOPS_LOADER_FROM_NET);
        shopsLoaderFromNET.forceLoad();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateShops(ArrayList<Shop> newShops) {
        uiController.hideRefresh();
        if (newShops != null) {
            for (Shop shop : newShops) {
                if (!shops.contains(shop)) {
                    shops.add(shop);
                    shop.putInDB(db);
                } else {
                    Shop containedShop = shops.get(shops.indexOf(shop));
                    if (!containedShop.equalsContent(shop)) {
                        containedShop.update(shop);
                        containedShop.updateInDB(db);
                    }
                }
            }
        }
        if (shops.size() > 0) {
            uiController.showListView();
            menuListener.onShowMenu();
            menuListener.onCheckMenuItem(checkedId);
            adapter.getFilter().filter(currentFilter);
        } else {
            menuListener.onHideMenu();
            if (currentFilter.equals(CategoryAdapter.FILTER_TYPE_ALL)) {
                if (newShops == null) {
                    uiController.showEmpty(R.string.empty_no_internet, R.drawable.ic_empty_wifi);
                } else {
                    uiController.showEmpty(R.string.empty_no_shops, R.drawable.ic_empty_shop);
                }
            } else {
                uiController.showEmpty(R.string.empty_no_favorite_shops, R.drawable.ic_empty_favorite);
            }
        }
    }

    public CategoryAdapter getAdapter() {
        return adapter;
    }

    public void updateShopFavorite(Shop shop) {
        shop.updateInDB(db);
        adapter.getFilter().filter(currentFilter);
    }

    public UIFragmentCallback getUiController(){
        return uiController;
    }
}