package com.PopCorp.Sales.Controllers;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SalesApplication;
import com.afollestad.materialdialogs.MaterialDialog;

public class PreferencesController {

    private final AppCompatActivity context;
    private final DB db;

    public PreferencesController(AppCompatActivity context) {
        this.context = context;
        db = ((SalesApplication) context.getApplication()).getDB();
    }

    public void showDialogAbout() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_about_application, null);

        final Dialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.prefs_about)
                .positiveText(android.R.string.ok)
                .autoDismiss(false)
                .customView(customView, true)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }
                })
                .build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public String getCity(String city) {
        String result = city;
        Cursor cursor = db.getData(DB.TABLE_CITYS, DB.COLUMNS_CITYS, DB.KEY_CITY_URL + "='" + city + "'");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(DB.KEY_CITY_NAME));
            }
            cursor.close();
        }
        return result;
    }
}
