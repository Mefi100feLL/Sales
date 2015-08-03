package com.PopCorp.Sales.Controllers;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.PopCorp.Sales.Activities.MainActivity;
import com.PopCorp.Sales.Activities.SaleActivity;
import com.PopCorp.Sales.Adapters.SalesAdapter;
import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Callbacks.CallBackForSalesFromNet;
import com.PopCorp.Sales.Callbacks.SaleClickListener;
import com.PopCorp.Sales.Callbacks.SalesFilterListener;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.City;
import com.PopCorp.Sales.Data.Group;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Data.Sales;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.Fragments.SalesFragment;
import com.PopCorp.Sales.Loaders.SalesLoader;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class SalesController implements LoaderManager.LoaderCallbacks<Cursor>, SaleClickListener, PendingRequestListener<Sales> {

    public static final int ID_FOR_CREATE_SALES_LOADER_FROM_DB = 1;
    public static final int ID_FOR_CREATE_SALES_LOADER_FROM_NET = 2;

    private final DB db;
    private final Category currentCategory;
    private final Shop currentShop;
    private final UIFragmentCallback uiController;
    private final RecyclerView recyclerView;
    private Fragment fragment;
    private final SalesAdapter adapter;
    private ArrayList<Sale> sales = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();
    private String city;
    private AppCompatActivity context;
    private SalesFilterListener menuListener;

    private String filterItem = "";
    private TreeMap<String, String> itemsForFilter = new TreeMap<>();

    public SalesController(AppCompatActivity context, Category category, Shop shop, Fragment fragment, RecyclerView recyclerView) {
        this.context = context;
        this.fragment = fragment;
        this.menuListener = (SalesFilterListener) fragment;
        this.recyclerView = recyclerView;
        uiController = (UIFragmentCallback) fragment;
        currentCategory = category;
        currentShop = shop;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        city = sPref.getString(SD.PREFS_CITY, "");
        db = ((SalesApplication) context.getApplication()).getDB();

        adapter = new SalesAdapter(this, sales, groups);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;
        if (id == ID_FOR_CREATE_SALES_LOADER_FROM_DB) {
            result = new SalesLoader(context, db, DB.KEY_CITY + "='" + city + "' AND " + DB.KEY_SALE_SHOP + "='" + currentShop.getUrl() + "'");
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                addSaleFromCursor(cursor);
                while (cursor.moveToNext()) {
                    addSaleFromCursor(cursor);
                }
            }
        }
        Collections.sort(sales);
        if (sales.size() > 0) {
            adapter.getFilter().filter(filterItem);
            uiController.showListView();
        }
        getGroups();
        refreshFilterItems();
        startLoaderFromNet();
    }

    private void addSaleFromCursor(Cursor cursor) {
        Sale sale = new Sale(cursor);
        if (!sales.contains(sale) && sale.isActual()) {
            sales.add(sale);
        }
    }

    private void refreshFilterItems() {
        itemsForFilter.clear();
        for (Sale sale : sales) {
            if (!itemsForFilter.containsKey(sale.getGroup())) {
                String nameOfGroup = "";
                for (Group group : groups) {
                    if (String.valueOf(group.getUrl()).equals(sale.getGroup())) {
                        nameOfGroup = group.getName();
                        break;
                    }
                }
                itemsForFilter.put(sale.getGroup(), nameOfGroup);
            }
        }
        if (!filterItem.isEmpty()) {
            if (!itemsForFilter.containsKey(filterItem)) {
                filterItem = "";
            }
        }
        if (itemsForFilter.size() < 2) {
            menuListener.onHideFilterMenuItem();
        } else {
            itemsForFilter.put("", context.getString(R.string.string_all));
            menuListener.onShowFilterMenuItem(itemsForFilter, filterItem);
        }
        adapter.getFilter().filter(filterItem);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        for (String key : itemsForFilter.keySet()) {
            if (itemId == key.hashCode()) {
                if (!key.equals(filterItem)) {
                    filterItem = key;
                    adapter.getFilter().filter(filterItem);
                    return true;
                }
            }
        }
        return false;
    }

    public void startLoaderFromNet() {
        fragment.getLoaderManager().restartLoader(ID_FOR_CREATE_SALES_LOADER_FROM_NET, new Bundle(), new CallBackForSalesFromNet(context, this, currentShop, currentCategory, db));
        Loader<ArrayList<City>> shopsLoaderFromNET = fragment.getLoaderManager().getLoader(ID_FOR_CREATE_SALES_LOADER_FROM_NET);
        shopsLoaderFromNET.forceLoad();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void updateSales(ArrayList<Sale> newSales) {
        uiController.hideRefresh();
        getGroups();
        adapter.setGroups(groups);
        if (newSales!=null) {
            for (Sale sale : newSales) {
                if (!sales.contains(sale)) {
                    sales.add(sale);
                    sale.putInDB(db);
                } else {
                    Sale containedSale = sales.get(sales.indexOf(sale));
                    if (!containedSale.equalsContent(sale)) {
                        containedSale.update(sale);
                        containedSale.updateInDB(db);
                    }
                }
            }
        }
        if (sales.size()!=0){
            uiController.showListView();
        } else{
            if (newSales==null){
                uiController.showEmpty(R.string.empty_no_internet, R.drawable.ic_empty_wifi);
            } else{
                uiController.showEmpty(R.string.empty_no_sales, R.drawable.ic_empty_sale);
            }
        }
        Collections.sort(sales);
        refreshFilterItems();
    }

    public SalesAdapter getAdapter() {
        return adapter;
    }


    private void getGroups() {
        Cursor cursor = db.getData(DB.TABLE_GROUPS, DB.COLUMNS_GROUPS, DB.KEY_CITY + "='" + city + "'");
        if (cursor!=null){
            if (cursor.moveToFirst()){
                addGroupFromCursor(cursor);
                while (cursor.moveToNext()){
                    addGroupFromCursor(cursor);
                }
            }
            cursor.close();
        }
    }

    private void addGroupFromCursor(Cursor cursor) {
        Group group = new Group(cursor);
        if (!groups.contains(group)){
            groups.add(group);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSaleClicked(View v, Sale sale) {
        Bundle args = new Bundle();
        args.putParcelable(SaleActivity.CURRENT_SALE_TAG, sale);
        ArrayList<Sale> array = new ArrayList<>();
        for (int i=0; i<adapter.getPublishItems().size(); i++){
            if (!adapter.getPublishItems().get(i).isHeader()){
                array.add(adapter.getPublishItems().get(i));
            }
        }
        args.putParcelableArrayList(SaleActivity.CURRENT_ARRAY_SALES_TAG, array);
        Intent intent = new Intent(context, SaleActivity.class);
        intent.putExtra(SaleActivity.CURRENT_SALE_TAG, sale);
        intent.putParcelableArrayListExtra(SaleActivity.CURRENT_ARRAY_SALES_TAG, array);

        if (Build.VERSION.SDK_INT >= 21) {
            ArrayList<Pair<View, String>> pairs = new ArrayList<>();
            for (Sale item : array){
                int pos = adapter.getPublishItems().indexOf(item);
                SalesAdapter.ViewHolder holder = (SalesAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
                if (holder!=null){
                    if (holder.image!=null){
                        pairs.add(new Pair<View, String>(holder.image, item.getId()));
                    }
                }
            }
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(context, pairs.toArray(new Pair[pairs.size()]));
            context.startActivityForResult(intent, SalesFragment.REQUEST_CODE_FOR_VIEW_SALES, transitionActivityOptions.toBundle());
            return;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            Bundle bundle = null;
            try {
                Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                bundle = ActivityOptions.makeThumbnailScaleUpAnimation(v, bitmap, 0, 0).toBundle();
            } catch(Exception ignored){
                context.startActivityForResult(intent, SalesFragment.REQUEST_CODE_FOR_VIEW_SALES);
            }
            context.startActivityForResult(intent, SalesFragment.REQUEST_CODE_FOR_VIEW_SALES, bundle);
            return;
        }

        context.startActivityForResult(intent, SalesFragment.REQUEST_CODE_FOR_VIEW_SALES);
    }

    @Override
    public void changeFavorite(Sale sale) {
        sale.setFavorite(!sale.isFavorite());
        sale.updateInDB(db);
        adapter.getPublishItems().updateItemAt(adapter.getPublishItems().indexOf(sale), sale);
    }

    @Override
    public void shareSale(Sale sale) {
        sale.share(context, db);
    }

    @Override
    public void buySale(Sale sale) {

    }

    public void updateFavorites(ArrayList<Sale> newSales) {
        for (Sale sale : newSales){
            sales.get(sales.indexOf(sale)).setFavorite(sale.isFavorite());
        }
    }

    @Override
    public void onRequestNotFound() {

    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {

    }

    @Override
    public void onRequestSuccess(Sales allSales) {
        updateSales(allSales);
    }
}