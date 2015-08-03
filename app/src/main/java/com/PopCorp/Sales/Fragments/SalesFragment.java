package com.PopCorp.Sales.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.AutoScrollHelper;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PopCorp.Sales.Activities.SaleActivity;
import com.PopCorp.Sales.Adapters.SalesAdapter;
import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Callbacks.SalesFilterListener;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.Controllers.SalesController;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Data.Sales;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.Requests.SalesSpiceRequest;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class SalesFragment extends Fragment implements SalesFilterListener, UIFragmentCallback {

    public static final String CURRENT_CATEGORY_TAG = "current_category";
    public static final String CURRENT_SHOP_TAG = "current_shop";

    public static final String TAG = SalesFragment.class.getSimpleName();
    public static final int REQUEST_CODE_FOR_VIEW_SALES = 12;

    private RecyclerView recyclerView;
    private SalesController controller;
    private SharedPreferences sPref;
    private Menu menu;
    private String[] arraySizesTable;
    private Shop shop;
    private TextView empty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;

    private SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);
    private Category category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sales, container, false);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) getActivity().getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
        setHasOptionsMenu(true);
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_sales_recyclerview);
        empty = (TextView) rootView.findViewById(R.id.fragment_sales_empty);
        progress = (ProgressBar) rootView.findViewById(R.id.fragment_sales_progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_sales_refresh);

        swipeRefresh.setColorSchemeResources(R.color.md_amber_500, R.color.md_red_500, R.color.md_deep_purple_500);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                getLoaderManager().restartLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);
                Loader<Cursor> citysLoaderFromDB = getLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB);
                citysLoaderFromDB.forceLoad();
            }
        });

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(sPref.getInt(SD.PREFS_SIZE_TABLE_SALES, getResources().getInteger(R.integer.default_size_table_sales)), StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        category = getArguments().getParcelable(CURRENT_CATEGORY_TAG);
        shop = getArguments().getParcelable(CURRENT_SHOP_TAG);
        controller = new SalesController((AppCompatActivity) getActivity(), category, shop, this, recyclerView);

        recyclerView.setAdapter(controller.getAdapter());
        return rootView;
    }

    @Override
    public void showListView() {
        empty.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showEmpty(int resEmptyString, int resEmptyDrawable) {
        progress.setVisibility(View.INVISIBLE);
        empty.setText(resEmptyString);
        empty.setCompoundDrawablesWithIntrinsicBounds(0, resEmptyDrawable, 0, 0);
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideRefresh(){
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_for_sales, menu);
        super.onCreateOptionsMenu(menu, inflater);
        startLoaderFromDB();

        int groupId = 12;
        MenuItem item = menu.findItem(R.id.action_size_table);
        item.getSubMenu().clear();
        arraySizesTable = getResources().getStringArray(R.array.sizes_table_sales);
        for (String filterItem : arraySizesTable) {
            MenuItem addedItem = item.getSubMenu().add(groupId, filterItem.hashCode(), Menu.NONE, filterItem);
            if (filterItem.equals(String.valueOf(sPref.getInt(SD.PREFS_SIZE_TABLE_SALES, getResources().getInteger(R.integer.default_size_table_sales))))) {
                addedItem.setChecked(true);
            }
        }
        item.getSubMenu().setGroupCheckable(groupId, true, true);
        item.getSubMenu().setGroupEnabled(groupId, true);
        item.setVisible(true);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (controller.onOptionsItemSelected(item)) {
            item.setChecked(true);
            return true;
        }
        if (item.getItemId() == R.id.action_sale_update){
            spiceManager.execute(new SalesSpiceRequest(getActivity(), new ArrayList<Shop>(){}, category, ((SalesApplication) getActivity().getApplication()).getDB()), controller);
        }
        for (String filterItem : arraySizesTable) {
            if (item.getItemId() == filterItem.hashCode()) {
                sPref.edit().putInt(SD.PREFS_SIZE_TABLE_SALES, Integer.parseInt(filterItem)).apply();
                item.setChecked(true);
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(Integer.parseInt(filterItem), StaggeredGridLayoutManager.VERTICAL);
                layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
                recyclerView.setLayoutManager(layoutManager);
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Toolbar) getActivity().findViewById(R.id.activity_main_toolbar)).setTitle(shop.getName());
        if (getView() != null) {
            getView().setKeepScreenOn(sPref.getBoolean(SD.PREFS_DISPLAY_NO_OFF, true));
        }
    }

    private void startLoaderFromDB() {
        getLoaderManager().initLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);
        Loader<Cursor> citysLoaderFromDB = getLoaderManager().getLoader(SalesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB);
        citysLoaderFromDB.forceLoad();
    }

    @Override
    public void onHideFilterMenuItem() {
        if (menu != null && menu.findItem(R.id.action_filter) != null) {
            menu.findItem(R.id.action_filter).setVisible(false);
        }
    }

    @Override
    public void onShowFilterMenuItem(TreeMap<String, String> filterItems, String selectedItem) {
        if (menu != null && menu.findItem(R.id.action_filter) != null) {
            int groupId = 12;
            MenuItem item = menu.findItem(R.id.action_filter);
            item.getSubMenu().clear();
            for (String filterItem : filterItems.keySet()) {
                MenuItem addedItem = item.getSubMenu().add(groupId, filterItem.hashCode(), Menu.NONE, filterItems.get(filterItem));
                if (filterItem.equals(selectedItem)) {
                    addedItem.setChecked(true);
                }
            }
            item.getSubMenu().setGroupCheckable(groupId, true, true);
            item.getSubMenu().setGroupEnabled(groupId, true);
            item.setVisible(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FOR_VIEW_SALES) {
            if (data != null) {
                ArrayList<Sale> newSales = data.getParcelableArrayListExtra(SaleActivity.CURRENT_ARRAY_SALES_TAG);
                controller.updateFavorites(newSales);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getActivity());
        spiceManager.addListenerIfPending(Sales.class, null, controller);
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        super.onStop();
    }
}
