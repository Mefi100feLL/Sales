package com.PopCorp.Sales.Controllers;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.PopCorp.Sales.Activities.SaleActivity;
import com.PopCorp.Sales.Adapters.SalesAdapter;
import com.PopCorp.Sales.Callbacks.SaleClickListener;
import com.PopCorp.Sales.Callbacks.SalesFilterListener;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Group;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.Fragments.NotesFragment;
import com.PopCorp.Sales.Fragments.SalesFragment;
import com.PopCorp.Sales.Loaders.SalesLoader;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;

import java.util.ArrayList;
import java.util.Collections;

public class NotesController implements LoaderManager.LoaderCallbacks<Cursor>, SaleClickListener {

    public static final int ID_FOR_CREATE_SALES_LOADER_FROM_DB = 1;
    private final String city;
    private final DB db;
    private final AppCompatActivity context;
    private final Fragment fragment;
    private final ArrayList<Sale> sales = new ArrayList<>();
    private final SalesAdapter adapter;
    private final UIFragmentCallback uiController;
    private ArrayList<Group> groups = new ArrayList<>();

    public NotesController(AppCompatActivity context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        uiController = (UIFragmentCallback) fragment;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
        city = sPref.getString(SD.PREFS_CITY, "");
        db = ((SalesApplication) context.getApplication()).getDB();

        adapter = new SalesAdapter(this, sales, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;
        if (id == ID_FOR_CREATE_SALES_LOADER_FROM_DB) {
            result = new SalesLoader(context, db, DB.KEY_CITY + "='" + city + "' AND " + DB.KEY_SALE_FAVORITE + "='true'");
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
        uiController.hideRefresh();
        if (sales.size() == 0) {
            uiController.showEmpty(R.string.empty_no_notes, R.drawable.ic_empty_note);
        } else {
            uiController.showListView();
        }
        getGroups();
        adapter.setGroups(groups);
        adapter.getFilter().filter("");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void addSaleFromCursor(Cursor cursor) {
        Sale sale = new Sale(cursor);
        if (!sales.contains(sale)) {
            sales.add(sale);
        }
    }

    @Override
    public void onSaleClicked(View v, Sale sale) {
        Bundle args = new Bundle();
        args.putParcelable(SaleActivity.CURRENT_SALE_TAG, sale);
        ArrayList<Sale> array = new ArrayList<>();
        for (int i = 0; i < adapter.getPublishItems().size(); i++) {
            if (!adapter.getPublishItems().get(i).isHeader()) {
                array.add(adapter.getPublishItems().get(i));
            }
        }
        args.putParcelableArrayList(SaleActivity.CURRENT_ARRAY_SALES_TAG, array);
        Intent intent = new Intent(context, SaleActivity.class);
        intent.putExtra(SaleActivity.CURRENT_SALE_TAG, sale);
        intent.putParcelableArrayListExtra(SaleActivity.CURRENT_ARRAY_SALES_TAG, array);

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(context, v, sale.getId());
            ArrayList<Pair<View, String>> pairs = new ArrayList<>();
            /*for (Sale item : array){
                int pos = adapter.getPublishItems().indexOf(item);
                SalesAdapter.ViewHolder holder = (SalesAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(pos);
                if (holder!=null){
                    if (holder.image!=null){
                        pairs.add(new Pair<View, String>(holder.image, item.getId()));
                    }
                }
            }*/
            ActivityOptions.makeSceneTransitionAnimation(context, pairs.toArray(new Pair[pairs.size()]));
            context.startActivityForResult(intent, NotesFragment.REQUEST_CODE_FOR_VIEW_SALES, transitionActivityOptions.toBundle());
            return;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
            Bundle bundle = ActivityOptions.makeThumbnailScaleUpAnimation(v, bitmap, 0, 0).toBundle();
            context.startActivityForResult(intent, NotesFragment.REQUEST_CODE_FOR_VIEW_SALES, bundle);
            return;
        }

        context.startActivityForResult(intent, NotesFragment.REQUEST_CODE_FOR_VIEW_SALES);
    }

    @Override
    public void changeFavorite(Sale sale) {
        sale.setFavorite(!sale.isFavorite());
        sale.updateInDB(db);
        if (!sale.isFavorite()) {
            sales.remove(sale);
            adapter.getPublishItems().remove(sale);
        }
        if (sales.size() == 0) {
            uiController.showEmpty(R.string.empty_no_notes, R.drawable.ic_empty_note);
        }
    }

    @Override
    public void shareSale(Sale sale) {
        sale.share(context, db);
    }

    @Override
    public void buySale(Sale sale) {

    }

    public SalesAdapter getAdapter() {
        return adapter;
    }

    private void getGroups() {
        Cursor cursor = db.getData(DB.TABLE_GROUPS, DB.COLUMNS_GROUPS, DB.KEY_CITY + "='" + city + "'");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                addGroupFromCursor(cursor);
                while (cursor.moveToNext()) {
                    addGroupFromCursor(cursor);
                }
            }
            cursor.close();
        }
    }


    private void addGroupFromCursor(Cursor cursor) {
        Group group = new Group(cursor);
        if (!groups.contains(group)) {
            groups.add(group);
        }
    }

    public void updateFavorites(ArrayList<Sale> newSales) {
        int count = 0;
        for (Sale sale : newSales) {
            if (!sale.isFavorite()) {
                sales.remove(sale);
                count++;
            }
        }
        if (count != 0) {
            if (sales.size() == 0) {
                uiController.showEmpty(R.string.empty_no_notes, R.drawable.ic_empty_note);
            } else {
                uiController.showListView();
            }
            adapter.getFilter().filter("");
        }
    }
}
