package com.PopCorp.Sales.Fragments;

import android.content.Intent;
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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PopCorp.Sales.Activities.SaleActivity;
import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Callbacks.UIFragmentCallback;
import com.PopCorp.Sales.Controllers.NotesController;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class NotesFragment extends Fragment implements UIFragmentCallback {

    public static final String TAG = NotesFragment.class.getSimpleName();
    public static final int REQUEST_CODE_FOR_VIEW_SALES = 13;

    private SharedPreferences sPref;
    private RecyclerView recyclerView;
    private NotesController controller;
    private TextView empty;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_notes, container, false);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) getActivity().getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
        setHasOptionsMenu(true);

        sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_notes_recyclerview);
        empty = (TextView) rootView.findViewById(R.id.fragment_notes_empty);
        progress = (ProgressBar) rootView.findViewById(R.id.fragment_notes_progress);
        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_notes_refresh);

        swipeRefresh.setColorSchemeResources(R.color.md_amber_500, R.color.md_red_500, R.color.md_deep_purple_500);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                getLoaderManager().restartLoader(NotesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);
                Loader<Cursor> notesLoaderFromDB = getLoaderManager().getLoader(NotesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB);
                notesLoaderFromDB.forceLoad();
            }
        });

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(sPref.getInt(SD.PREFS_SIZE_TABLE_SALES, getResources().getInteger(R.integer.default_size_table_sales)), StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);

        controller = new NotesController((AppCompatActivity) getActivity(), this);

        recyclerView.setAdapter(controller.getAdapter());
        startLoaderFromDB();
        return rootView;
    }

    private void startLoaderFromDB() {
        getLoaderManager().initLoader(NotesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB, new Bundle(), controller);
        Loader<Cursor> notesLoaderFromDB = getLoaderManager().getLoader(NotesController.ID_FOR_CREATE_SALES_LOADER_FROM_DB);
        notesLoaderFromDB.forceLoad();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FOR_VIEW_SALES) {
            if (data != null) {
                ArrayList<Sale> newSales = data.getParcelableArrayListExtra(SaleActivity.CURRENT_ARRAY_SALES_TAG);
                controller.updateFavorites(newSales);
                Sale currentSale = data.getParcelableExtra(SaleActivity.CURRENT_SALE_TAG);
                recyclerView.scrollToPosition(controller.getAdapter().getPublishItems().indexOf(currentSale));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            getView().setKeepScreenOn(sPref.getBoolean(SD.PREFS_DISPLAY_NO_OFF, true));
        }
    }

    @Override
    public void showEmpty(int resEmptyString, int resEmptyDrawable) {
        progress.setVisibility(View.INVISIBLE);
        empty.setText(resEmptyString);
        empty.setCompoundDrawablesWithIntrinsicBounds(0, resEmptyDrawable, 0, 0);
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void showListView() {
        empty.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideRefresh() {
        swipeRefresh.setRefreshing(false);
    }
}
