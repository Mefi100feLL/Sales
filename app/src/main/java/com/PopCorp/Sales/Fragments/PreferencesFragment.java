package com.PopCorp.Sales.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.PopCorp.Sales.Activities.CitysActivity;
import com.PopCorp.Sales.Activities.LoginActivity;
import com.PopCorp.Sales.Activities.MainActivity;
import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.Controllers.PreferencesController;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SD;
import com.PopCorp.Sales.SalesApplication;
import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class PreferencesFragment extends PreferenceFragment {

    public static final String TAG = PreferencesFragment.class.getSimpleName();

    private AppCompatActivity context;
    private SharedPreferences sPref;
    private PreferencesController controller;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        addPreferencesFromResource(R.xml.prefs);

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) getActivity().getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
        context = (AppCompatActivity) getActivity();
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        controller = new PreferencesController(context);
        setHasOptionsMenu(true);
        initializePrefs();
        if (getPreferenceScreen() != null) {
            ArrayList<Preference> preferences = getAllPreferenceScreen(getPreferenceScreen(), new ArrayList<Preference>());
            for (Preference preference : preferences) {
                preferenceToMaterialPreference(preference);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private ArrayList<Preference> getAllPreferenceScreen(Preference p, ArrayList<Preference> list) {
        if (p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            list.add(p);
            for (int i = 0; i < pCount; i++) {
                getAllPreferenceScreen(pGroup.getPreference(i), list);
            }
        }
        return list;
    }

    private void preferenceToMaterialPreference(Preference preference) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (preference instanceof PreferenceScreen && preference.getLayoutResource()
                    != R.layout.mp_preference_material) {
                preference.setLayoutResource(R.layout.mp_preference_material);
            } else if (preference instanceof PreferenceCategory && preference.getLayoutResource() != R.layout.mp_preference_category) {
                preference.setLayoutResource(R.layout.mp_preference_category);

                PreferenceCategory category = (PreferenceCategory) preference;
                for (int j = 0; j < category.getPreferenceCount(); j++) {
                    Preference basicPreference = category.getPreference(j);
                    if (!(basicPreference instanceof PreferenceCategory || basicPreference instanceof PreferenceScreen)) {
                        if (basicPreference.getLayoutResource() != R.layout.mp_preference_material_widget) {
                            basicPreference.setLayoutResource(R.layout.mp_preference_material_widget);
                        }
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView != null) {
            listView = (ListView) rootView.findViewById(android.R.id.list);
            listView.setPadding((int) context.getResources().getDimension(R.dimen.listview_padding_left_right), 0, (int) context.getResources().getDimension(R.dimen.listview_padding_left_right), 0);
            listView.setClipToPadding(false);
            listView.setFooterDividersEnabled(false);
            if (Build.VERSION.SDK_INT < 21) {
                listView.setSelector(R.drawable.selector_for_normal_list);
            }
        }
        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) getActivity().getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        listView.setKeepScreenOn(sPref.getBoolean(SD.PREFS_DISPLAY_NO_OFF, true));
        selectCity();
    }

    private void initializePrefs() {
        Preference prefCity = findPreference(SD.PREFS_CITY);
        if (prefCity != null) {
            prefCity.setSummary(controller.getCity(sPref.getString(SD.PREFS_CITY, "1")));
            prefCity.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(getActivity(), CitysActivity.class), MainActivity.CITY_REQUEST_CODE);
                    return true;
                }
            });
        }

        Preference prefProfile = findPreference(SD.PREFS_PROFILE);
        if (prefProfile != null) {
            //prefProfile.setSummary(controller.getCity(sPref.getString(SD.PREFS_CITY, "1")));
            prefProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    return true;
                }
            });
        }

        Preference prefDisplayNoOff = findPreference(SD.PREFS_DISPLAY_NO_OFF);
        if (prefDisplayNoOff != null) {
            prefDisplayNoOff.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean value = (boolean) newValue;
                    listView.setKeepScreenOn(value);
                    return true;
                }
            });
        }

        Preference about = findPreference(SD.PREFS_ABOUT);
        if (about != null) {
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    controller.showDialogAbout();
                    return true;
                }
            });
        }
    }

    public void selectCity() {
        Preference prefCity = findPreference(SD.PREFS_CITY);
        if (prefCity != null) {
            prefCity.setSummary(controller.getCity(sPref.getString(SD.PREFS_CITY, "1")));
        }
    }
}
