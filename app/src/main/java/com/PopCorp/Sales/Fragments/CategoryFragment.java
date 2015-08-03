package com.PopCorp.Sales.Fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Callbacks.ShopsFilterListener;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.Controllers.CategoryController;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class CategoryFragment extends Fragment implements ShopsFilterListener, UIFragmentCallback {

    public static final String CURRENT_CATEGORY_TAG = "current_category";

    private RecyclerView recyclerView;
    private CategoryController controller;
    private SharedPreferences sPref;
    private Menu menu;
    private String[] arraySizesTable;
    private Category category;
    private TextView empty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_category, container, false);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) getActivity().getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        setHasOptionsMenu(true);
        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_category_recyclerview);
        empty = (TextView) rootView.findViewById(R.id.fragment_category_empty);
        progress = (ProgressBar) rootView.findViewById(R.id.fragment_category_progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_category_refresh);

        swipeRefresh.setColorSchemeResources(R.color.md_amber_500, R.color.md_red_500, R.color.md_deep_purple_500);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                getLoaderManager().restartLoader(CategoryController.ID_FOR_CREATE_SHOPS_LOADER_FROM_DB, new Bundle(), controller);
                Loader<Cursor> citysLoaderFromDB = getLoaderManager().getLoader(CategoryController.ID_FOR_CREATE_SHOPS_LOADER_FROM_DB);
                citysLoaderFromDB.forceLoad();
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), sPref.getInt(SD.PREFS_SIZE_TABLE_SHOPS, getResources().getInteger(R.integer.default_size_table_lists)));
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        category = getArguments().getParcelable(CURRENT_CATEGORY_TAG);
        controller = new CategoryController((AppCompatActivity) getActivity(), category, this);

        recyclerView.setAdapter(controller.getAdapter());
        return rootView;
    }

    @Override
    public void showListView(){
        empty.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showEmpty(int resEmptyString, int resEmptyDrawable){
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
        inflater.inflate(R.menu.menu_for_shopes, menu);
        super.onCreateOptionsMenu(menu, inflater);
        startLoaderFromDB();

        int groupId = 12;
        MenuItem item = menu.findItem(R.id.action_size_table);
        item.getSubMenu().clear();
        arraySizesTable = getResources().getStringArray(R.array.sizes_table_lists);
        for (String filterItem : arraySizesTable) {
            MenuItem addedItem = item.getSubMenu().add(groupId, filterItem.hashCode(), Menu.NONE, filterItem);
            if (filterItem.equals(String.valueOf(sPref.getInt(SD.PREFS_SIZE_TABLE_SHOPS, getResources().getInteger(R.integer.default_size_table_lists))))) {
                addedItem.setChecked(true);
            }
        }
        item.getSubMenu().setGroupCheckable(groupId, true, true);
        item.getSubMenu().setGroupEnabled(groupId, true);
        item.setVisible(true);
        this.menu = menu;
    }

    @Override
    public void onResume(){
        super.onResume();
        ((Toolbar) getActivity().findViewById(R.id.activity_main_toolbar)).setTitle(category.getName());
        if (getView() != null) {
            getView().setKeepScreenOn(sPref.getBoolean(SD.PREFS_DISPLAY_NO_OFF, true));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filter_all || item.getItemId() == R.id.action_filter_favorite){
            item.setChecked(true);
            controller.filter(item.getItemId());
        }
        for (String filterItem : arraySizesTable) {
            if (item.getItemId() == filterItem.hashCode()){
                sPref.edit().putInt(SD.PREFS_SIZE_TABLE_SHOPS, Integer.parseInt(filterItem)).commit();
                item.setChecked(true);
                GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), Integer.parseInt(filterItem));
                recyclerView.setLayoutManager(layoutManager);
            }
        }
        return true;
    }

    private void startLoaderFromDB(){
        getLoaderManager().initLoader(CategoryController.ID_FOR_CREATE_SHOPS_LOADER_FROM_DB, new Bundle(), controller);
        Loader<Cursor> citysLoaderFromDB = getLoaderManager().getLoader(CategoryController.ID_FOR_CREATE_SHOPS_LOADER_FROM_DB);
        citysLoaderFromDB.forceLoad();
    }

    @Override
    public void onHideMenu() {
        if (menu != null && menu.findItem(R.id.action_shopes_filter) != null) {
            menu.findItem(R.id.action_shopes_filter).setVisible(false);
        }
    }

    @Override
    public void onShowMenu() {
        if (menu != null && menu.findItem(R.id.action_shopes_filter) != null) {
            menu.findItem(R.id.action_shopes_filter).setVisible(true);
        }
    }

    @Override
    public void onCheckMenuItem(int menuId) {
        if (menu != null && menu.findItem(R.id.action_shopes_filter) != null) {
            menu.findItem(R.id.action_shopes_filter).getSubMenu().findItem(menuId).setChecked(true);
        }
    }
}
