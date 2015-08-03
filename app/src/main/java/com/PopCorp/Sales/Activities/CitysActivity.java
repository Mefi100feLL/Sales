package com.PopCorp.Sales.Activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Controllers.CitysController;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.software.shell.fab.ActionButton;

public class CitysActivity extends AppCompatActivity implements CitysController.UICallback {

    private CitysController controller;
    private ActionButton fab;
    private SharedPreferences sPref;
    private ListView listView;
    private ProgressBar progress;
    private TextView empty;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citys);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) this.getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        Toolbar toolBar = (Toolbar) findViewById(R.id.activity_citys_toolbar);
        setSupportActionBar(toolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listView = (ListView) findViewById(R.id.activity_citys_listview);
        fab = (ActionButton) findViewById(R.id.activity_citys_fab);
        progress = (ProgressBar) findViewById(R.id.activity_citys_progress);
        empty = (TextView) findViewById(R.id.activity_citys_empty);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.activity_citys_refresh);

        swipeRefresh.setColorSchemeResources(R.color.md_amber_500, R.color.md_red_500, R.color.md_deep_purple_500);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                getSupportLoaderManager().initLoader(CitysController.ID_FOR_CREATE_CITYS_LOADER_FROM_DB, new Bundle(), controller);
                Loader<Cursor> citysLoaderFromDB = getSupportLoaderManager().getLoader(CitysController.ID_FOR_CREATE_CITYS_LOADER_FROM_DB);
                citysLoaderFromDB.forceLoad();
            }
        });

        controller = new CitysController(this);

        listView.setAdapter(controller.getAdapter());
        listView.setKeepScreenOn(sPref.getBoolean(SD.PREFS_DISPLAY_NO_OFF, true));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                controller.getAdapter().setSelectedCity(position);
                showFab();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fab.getAnimation() != null) {
                    if (!fab.getAnimation().hasEnded()) {
                        return;
             }
                }
                fab.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
                fab.getHideAnimation().setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        sPref.edit().putString(SD.PREFS_CITY, controller.getAdapter().getSelectedCity().getUrl()).apply();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fab.hide();
            }
        });

        String city = sPref.getString(SD.PREFS_CITY, "");
        if (city != null && !city.isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showFab();
                }
            }, 200);
        }

        getSupportLoaderManager().initLoader(CitysController.ID_FOR_CREATE_CITYS_LOADER_FROM_DB, new Bundle(), controller);
        Loader<Cursor> citysLoaderFromDB = getSupportLoaderManager().getLoader(CitysController.ID_FOR_CREATE_CITYS_LOADER_FROM_DB);
        citysLoaderFromDB.forceLoad();
    }

    @Override
    public void showListView() {
        listView.setFastScrollAlwaysVisible(true);
        listView.setFastScrollEnabled(true);
        progress.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideRefresh(){
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void showEmpty(int resEmptyString, int resEmptyDrawable) {
        listView.setFastScrollEnabled(false);
        progress.setVisibility(View.INVISIBLE);
        empty.setVisibility(View.VISIBLE);
        empty.setCompoundDrawablesWithIntrinsicBounds(0, resEmptyDrawable, 0, 0);
        empty.setText(resEmptyString);
    }

    @Override
    public void setSelection(int position) {
        listView.setSelection(position);
    }

    private void showFab() {
        if (fab.isHidden()) {
            fab.setShowAnimation(ActionButton.Animations.SCALE_UP);
            fab.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fab.getAnimation() != null) {
            if (!fab.getAnimation().hasEnded()) {
                return;
            }
        }
        if (!fab.isHidden()) {
            fab.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
            fab.getHideAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            fab.hide();
        } else {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}