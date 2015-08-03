package com.PopCorp.Sales.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.PopCorp.Sales.Activities.CropActivity;
import com.PopCorp.Sales.BuildConfig;
import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.R;
import com.PopCorp.Sales.SalesApplication;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.edmodo.cropper.CropImageView;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;

public class SaleFragment extends Fragment {

    public static final String CURRENT_SALE_TAG = "current_sale_tag";

    private DB db;
    private Sale sale;
    private SubsamplingScaleImageView imageView;
    private CircularProgressView progressBar;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sale, container, false);

        db = ((SalesApplication) getActivity().getApplication()).getDB();
        if (!BuildConfig.DEBUG) {
            Tracker t = ((SalesApplication) getActivity().getApplication()).getTracker(SalesApplication.TrackerName.APP_TRACKER);
            t.setScreenName(this.getClass().getSimpleName());
            t.send(new HitBuilders.AppViewBuilder().build());
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);

        sale = getArguments().getParcelable(CURRENT_SALE_TAG);

        progressBar = (CircularProgressView) rootView.findViewById(R.id.fragment_sale_progressbar);
        imageView = (SubsamplingScaleImageView) rootView.findViewById(R.id.fragment_sale_imageview);
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
        imageView.setMaxScale(getResources().getDimension(R.dimen.image_maximum_scale));
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        File smallFile = imageLoader.getDiskCache().get(sale.getSmallImageUrl());
        if (smallFile != null) {
            imageView.setImage(ImageSource.uri(smallFile.getAbsolutePath()));
            loadBigImage();
        } else {
            imageLoader.loadImage(sale.getSmallImageUrl(), null, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    File smallFile = imageLoader.getDiskCache().get(sale.getSmallImageUrl());
                    if (smallFile != null) {
                        imageView.setImage(ImageSource.uri(smallFile.getAbsolutePath()));
                    }
                    bitmap.recycle();
                    loadBigImage();
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String s, View view, int progress, int size) {
                    progressBar.setProgress(progress * 500 / size);
                }
            });
        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void loadBigImage() {
        imageLoader.loadImage(sale.getImageUrl(), null, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                File file = imageLoader.getDiskCache().get(sale.getImageUrl());
                if (file != null) {
                    imageView.setImage(ImageSource.uri(file.getAbsolutePath()));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String s, View view, int progress, int size) {
                progressBar.setProgress(500 + progress * 500 / size);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_for_sale, menu);

        MenuItem item = menu.findItem(R.id.action_sale_favorite);
        if (sale.isFavorite()) {
            item.setIcon(R.drawable.ic_star_white_24dp);
            item.setTitle(R.string.action_sale_from_favorite);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sale_favorite) {
            sale.setFavorite(!sale.isFavorite());
            sale.updateInDB(db);
            if (sale.isFavorite()) {
                item.setIcon(R.drawable.ic_star_white_24dp);
                item.setTitle(R.string.action_sale_from_favorite);
            } else {
                item.setIcon(R.drawable.ic_star_outline_white_24dp);
                item.setTitle(R.string.action_sale_in_favorite);
            }
            return true;
        }
        if (item.getItemId() == R.id.action_sale_share) {
            shareSale();
            return true;
        }
        if (item.getItemId() == R.id.action_sale_crop) {
            Intent intent = new Intent(getActivity(), CropActivity.class);
            intent.putExtra(CropActivity.CURRENT_SALE_TAG, sale);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareSale() {
        File image = imageLoader.getDiskCache().get(sale.getImageUrl());
        if (image == null) {
            image = imageLoader.getDiskCache().get(sale.getSmallImageUrl());
        }
        if (image == null) {
            Toast.makeText(getActivity(), R.string.error_no_cached_image, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] split = sale.getShop().split("/");
        String shop = split[split.length - 1];
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

        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.string_chooser_send_in)));
    }
}
