package com.PopCorp.Sales.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;

import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Controllers.MainController;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Category;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.Data.User;
import com.PopCorp.Sales.Fragments.CategoryFragment;
import com.PopCorp.Sales.Fragments.ListsFragment;
import com.PopCorp.Sales.Fragments.NotesFragment;
import com.PopCorp.Sales.Fragments.PreferencesFragment;
import com.PopCorp.Sales.Fragments.SalesFragment;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static final int CITY_REQUEST_CODE = 1;
    public static final int USER_REQUEST_CODE = 2;

    private static final String CURRENT_DRAWER_ITEM = "current_drawer_item";

    private Toolbar toolBar;
    private Drawer drawer;
    private MainController controller;
    private SharedPreferences sPref;
    private ArrayList<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) this.getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        toolBar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolBar);

        createNewDrawer(savedInstanceState);
        controller = new MainController(this, drawer);
        String city = sPref.getString(SD.PREFS_CITY, "");

        if ((city == null || city.isEmpty()) && savedInstanceState == null) {
            startActivityForResult(new Intent(this, CitysActivity.class), CITY_REQUEST_CODE);
            return;
        }
        loadCategs();
        Thread removerSales = new Thread(remover, "REMOVERSALES");
        removerSales.setDaemon(false);
        removerSales.setPriority(Thread.NORM_PRIORITY);
        removerSales.start();
    }

    Runnable remover = new Runnable() {
        @Override
        public void run() {
            if (getExternalCacheDir()!=null) {
                File dir = new File(getExternalCacheDir().getAbsolutePath() + "/tmp");
                File[] files = dir.listFiles();
                if (files!=null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
            File dir = new File(getCacheDir().getAbsolutePath() + "/tmp");
            File[] files = dir.listFiles();
            if (files!=null) {
                for (File file : files) {
                    file.delete();
                }
            }
            DB db = ((SalesApplication) getApplication()).getDB();
            Cursor cursor = null;
            try {
                db.open();
                cursor = db.getAllData(DB.TABLE_SALES);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        removeSale(db, cursor);
                        while (cursor.moveToNext()) {
                            removeSale(db, cursor);
                        }
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private void removeSale(DB db, Cursor cursor) {
            Sale sale = new Sale(cursor);
            if (!sale.isActual() && !sale.isFavorite()) {
                sale.remove(db);
            }
        }
    };

    private void loadCategs() {
        getSupportLoaderManager().restartLoader(MainController.ID_FOR_CREATE_CATEGORIES_LOADER_FROM_DB, new Bundle(), controller);
        Loader<Cursor> citysLoaderFromDB = getSupportLoaderManager().getLoader(MainController.ID_FOR_CREATE_CATEGORIES_LOADER_FROM_DB);
        citysLoaderFromDB.forceLoad();
    }

    private void createNewDrawer(Bundle savedInstanceState) {
        Cursor cursor = ((SalesApplication) getApplication()).getDB().getAllData(DB.TABLE_USERS);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                users.add(new User(cursor));
                while (cursor.moveToNext()) {
                    users.add(new User(cursor));
                }
            }
            cursor.close();
        }

        ArrayList<IProfile> profiles = new ArrayList<>();
        for (User user : users) {
            profiles.add(new ProfileDrawerItem().withName(user.getName()).withEmail(user.getEmail()));
        }
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.indigo)
                .withProfiles(profiles)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        ArrayList<IDrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(new DividerDrawerItem());
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.drawer_item_my_notes).withIdentifier(R.string.drawer_item_my_notes).withIcon(R.drawable.ic_star_grey600_24dp).withIconTintingEnabled(true));
        drawerItems.add(new PrimaryDrawerItem().withName(R.string.drawer_item_settings).withIdentifier(R.string.drawer_item_settings).withIcon(R.drawable.ic_settings_grey600_24dp).withIconTintingEnabled(true));
        DrawerBuilder builder = new DrawerBuilder();
        builder.withActivity(this);
        builder.withAccountHeader(headerResult);
        builder.withToolbar(toolBar);
        builder.withSavedInstance(savedInstanceState);
        if (savedInstanceState == null) {
            builder.withSelectedItem(-1);
        }
        builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(AdapterView<?> adapterView, View view, int position, long l, IDrawerItem iDrawerItem) {
                if (iDrawerItem == null) {
                    return true;
                }
                FragmentManager manager = getSupportFragmentManager();
                manager.popBackStackImmediate();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment fragment = null;
                String title = "";
                String tag = "";
                if (iDrawerItem.getIdentifier() == R.string.drawer_item_my_notes) {
                    fragment = manager.findFragmentByTag(NotesFragment.TAG);
                    if (fragment == null) {
                        fragment = new NotesFragment();
                    }
                    title = getString(R.string.drawer_item_my_notes);
                    tag = NotesFragment.TAG;
                } else if (iDrawerItem.getIdentifier() == R.string.drawer_item_lists) {
                    fragment = manager.findFragmentByTag(ListsFragment.TAG);
                    if (fragment == null) {
                        fragment = new ListsFragment();
                    }
                    title = getString(R.string.drawer_item_lists);
                    tag = ListsFragment.TAG;
                } else if (iDrawerItem.getIdentifier() == R.string.drawer_item_settings) {
                    fragment = manager.findFragmentByTag(PreferencesFragment.TAG);
                    if (fragment == null) {
                        fragment = new PreferencesFragment();
                    }
                    title = getString(R.string.drawer_item_settings);
                    tag = PreferencesFragment.TAG;
                } else {
                    if (controller.getCategories().size() != 0) {
                        for (int i = 0; i < controller.getCategories().size(); i++) {
                            if (position == i) {
                                Category category = controller.getCategories().get(i);
                                fragment = manager.findFragmentByTag(category.getUrl());
                                if (fragment == null) {
                                    fragment = new CategoryFragment();
                                    Bundle arguments = new Bundle();
                                    arguments.putParcelable(CategoryFragment.CURRENT_CATEGORY_TAG, category);
                                    fragment.setArguments(arguments);
                                }
                                title = category.getName();
                                tag = category.getUrl();
                            }
                        }
                    }
                }
                if (fragment != null) {
                    transaction.replace(R.id.activity_main_content_frame, fragment, tag).commit();
                    toolBar.setTitle(title);
                }
                return false;
            }
        });
        builder.withDrawerItems(drawerItems);
        drawer = builder.build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SalesFragment.REQUEST_CODE_FOR_VIEW_SALES) {
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(SalesFragment.TAG);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == NotesFragment.REQUEST_CODE_FOR_VIEW_SALES) {
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(NotesFragment.TAG);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == CITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                loadCategs();
            } else {
                String city = sPref.getString(SD.PREFS_CITY, "");
                if (city == null || city.isEmpty()) {
                    finish();
                }
            }
        } else if (requestCode == USER_REQUEST_CODE) {
            Cursor cursor = ((SalesApplication) getApplication()).getDB().getAllData(DB.TABLE_USERS);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    addUserFromCursor(cursor);
                    while (cursor.moveToNext()) {
                        addUserFromCursor(cursor);
                    }
                }
                cursor.close();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addUserFromCursor(Cursor cursor) {
        User user = new User(cursor);
        if (!users.contains(user)) {
            users.add(new User(cursor));
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            return;
        }
        if (!drawer.isDrawerOpen()) {
            drawer.openDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_DRAWER_ITEM, drawer.getCurrentSelection());
        drawer.saveInstanceState(outState);
    }
}
