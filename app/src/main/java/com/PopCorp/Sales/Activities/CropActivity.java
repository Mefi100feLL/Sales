package com.PopCorp.Sales.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.Requests.FileSpiceRequest;
import com.PopCorp.Sales.SalesApplication;
import com.edmodo.cropper.CropImageView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.UncachedSpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CropActivity extends AppCompatActivity implements PendingRequestListener<File> {

    public static final String CURRENT_SALE_TAG = "current_sale_tag";

    private static final String BOOLEAN_CROPPED = "cropped";
    private static final String BITMAP = "bitmap";
    private static final String FILE_NAME = "file_name";

    private SpiceManager spiceManager = new SpiceManager(UncachedSpiceService.class);

    private CropImageView cropImage;
    private ImageView image;

    private Menu menu;

    private Sale sale;
    private String fileName;
    private File file;
    private boolean cropped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        cropped = false;

        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) this.getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }

        sale = getIntent().getParcelableExtra(CURRENT_SALE_TAG);
        fileName = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", new Locale("ru")).format(Calendar.getInstance().getTime()) + ".png";

        Toolbar toolBar = (Toolbar) findViewById(R.id.activity_crop_toolbar);
        toolBar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        setSupportActionBar(toolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        cropImage = (CropImageView) findViewById(R.id.activity_crop_cropimage);
        image = (ImageView) findViewById(R.id.activity_crop_image);

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        imageLoader.loadImage(sale.getImageUrl(), options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                cropImage.setImageBitmap(bitmap);
                if (menu != null && menu.findItem(R.id.action_sale_crop_apply)!=null && !cropped) {
                    menu.findItem(R.id.action_sale_crop_apply).setVisible(true);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        if (savedInstanceState!=null) {
            cropped = savedInstanceState.getBoolean(BOOLEAN_CROPPED);
            if (cropped) {
                cropImage.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);
                Bitmap bitmap = savedInstanceState.getParcelable(BITMAP);
                fileName = savedInstanceState.getString(FILE_NAME);
                image.setImageBitmap(bitmap);
                spiceManager.execute(new FileSpiceRequest(getApplicationContext(), fileName, bitmap), this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_for_crop_sale, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        menu.findItem(R.id.action_sale_share).setVisible(cropped);
        menu.findItem(R.id.action_sale_crop_apply).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sale_crop_apply) {
            cropImage();
            cropped = true;
            return true;
        }
        if (item.getItemId() == R.id.action_sale_share) {
            shareSale();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void cropImage() {
        image.setImageBitmap(cropImage.getCroppedImage());
        cropImage.setVisibility(View.INVISIBLE);
        image.setVisibility(View.VISIBLE);
        if (menu!=null && menu.findItem(R.id.action_sale_crop_apply)!=null) {
            menu.findItem(R.id.action_sale_crop_apply).setVisible(false);
        }

        spiceManager.execute(new FileSpiceRequest(getApplicationContext(), fileName, cropImage.getCroppedImage()), this);
    }

    @Override
    public void onRequestNotFound() {

    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        Toast.makeText(CropActivity.this, R.string.error_imposible_mkdir, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestSuccess(File file) {
        if (file == null) {
            Toast.makeText(CropActivity.this, R.string.error_file_no_created, Toast.LENGTH_SHORT).show();
            return;
        }
        this.file = file;
        if (menu!=null && menu.findItem(R.id.action_sale_share)!=null) {
            menu.findItem(R.id.action_sale_share).setVisible(true);
        }
    }

    private void shareSale() {
        String[] split = sale.getShop().split("/");
        String shop = split[split.length - 1];
        DB db = ((SalesApplication) getApplication()).getDB();
        Cursor cursor = db.getData(DB.TABLE_SHOPS, DB.COLUMNS_SHOPS, DB.KEY_SHOP_URL + "='" + sale.getShop() + "'");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                shop = cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_NAME));
            }
            cursor.close();
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.string_for_share_sale).replaceAll("shop", shop).replace("period", sale.getPeriod()));

        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.string_chooser_send_in)));
    }

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BOOLEAN_CROPPED, cropped);
        if (cropped) {
            outState.putParcelable(BITMAP, cropImage.getCroppedImage());
            outState.putString(FILE_NAME, fileName);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        spiceManager.addListenerIfPending(File.class, null, this);
    }


}
