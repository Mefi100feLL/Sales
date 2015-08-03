package com.PopCorp.Sales.Data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.PopCorp.Sales.DB.DB;
import com.PopCorp.Sales.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Sale implements Comparable<Sale>, Parcelable {

    private String id;
    private String shop;
    private String group;
    private String period;
    private boolean favorite = false;
    private String city;
    private String imageUrl;
    private String smallImageUrl;
    private String width;
    private String height;
    /////////////////////////////////////////// attributes for header
    private boolean header = false;
    private String title;

    public Sale(String id, String shop, String group, String period, String favorite, String city, String imageUrl, String smallImageUrl, String width, String height) {
        setId(id);
        setShop(shop);
        setGroup(group);
        setPeriod(period);
        setFavorite(favorite);
        setCity(city);
        setImageUrl(imageUrl);
        setSmallImageUrl(smallImageUrl);
        setWidth(width);
        setHeight(height);
    }

    public Sale(Cursor cursor) {
        this(
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_ID)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_SHOP)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_GROUP)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_PERIOD)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_FAVORITE)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_CITY)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_IMAGE)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_SMALL_IMAGE)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_WIDTH)),
                cursor.getString(cursor.getColumnIndex(DB.KEY_SALE_HEIGHT))
        );
    }

    public Sale() {
        header = true;
        title = "";
    }

    public void putInDB(DB db) {
        db.addRec(DB.TABLE_SALES, DB.COLUMNS_SALES, getFields());
    }

    private String[] getFields() {
        return new String[]{
                getId(),
                getShop(),
                getGroup(),
                getPeriod(),
                getCity(),
                getImageUrl(),
                getSmallImageUrl(),
                getWidth(),
                getHeight(),
                getFavorite()
        };
    }

    public void update(Sale sale) {
        setShop(sale.getShop());
        setGroup(sale.getGroup());
        setPeriod(sale.getPeriod());
        setCity(sale.getCity());
        setImageUrl(sale.getImageUrl());
        setSmallImageUrl(sale.getSmallImageUrl());
        setWidth(sale.getWidth());
        setHeight(sale.getHeight());
    }

    public int updateInDB(DB db) {
        return db.update(DB.TABLE_SALES, DB.COLUMNS_SALES, DB.KEY_CITY + "='" + getCity() + "' AND " + DB.KEY_SALE_ID + "='" + getId() + "'", getFields());
    }

    public void remove(DB db) {
        db.deleteRows(DB.TABLE_SALES, DB.KEY_CITY + "='" + getCity() + "' AND " + DB.KEY_SALE_ID + "='" + getId() + "'");
        ImageLoader imageLoader = ImageLoader.getInstance();
        File smallImage = imageLoader.getDiskCache().get(getSmallImageUrl());
        if (smallImage != null) {
            smallImage.delete();
        }
        File image = imageLoader.getDiskCache().get(getImageUrl());
        if (image != null) {
            image.delete();
        }
    }

    public boolean equalsContent(Sale sale) {
        for (int i = 0; i < getFields().length - 1; i++) {
            if (!getFields()[i].equals(sale.getFields()[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        Sale sale = (Sale) object;
        if (getCity().equals(sale.getCity())) {
            if (getId().equals(sale.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getShop());
        dest.writeString(getGroup());
        dest.writeString(getPeriod());
        dest.writeString(getCity());
        dest.writeString(getImageUrl());
        dest.writeString(getSmallImageUrl());
        dest.writeString(getWidth());
        dest.writeString(getHeight());
        dest.writeString(getFavorite());
    }

    public static final Parcelable.Creator<Sale> CREATOR = new Parcelable.Creator<Sale>() {
        public Sale createFromParcel(Parcel in) {
            return new Sale(in);
        }

        public Sale[] newArray(int size) {
            return new Sale[size];
        }
    };

    private Sale(Parcel parcel) {
        setId(parcel.readString());
        setShop(parcel.readString());
        setGroup(parcel.readString());
        setPeriod(parcel.readString());
        setCity(parcel.readString());
        setImageUrl(parcel.readString());
        setSmallImageUrl(parcel.readString());
        setWidth(parcel.readString());
        setHeight(parcel.readString());
        setFavorite(parcel.readString());
    }

    @Override
    public int compareTo(Sale another) {
        int result = 0;
        if (getGroup() != null && another.getGroup() != null) {
            result = getGroup().compareToIgnoreCase(another.getGroup());
        }
        if (result == 0) {
            if (!isHeader() && !another.isHeader()) {
                result = getId().compareToIgnoreCase(another.getId());
            } else if (isHeader()) {
                return -1;
            } else if (another.isHeader()) {
                return 1;
            }
        }
        return result;
    }

    public boolean isActual() {
        if (period != null && !period.isEmpty()) {
            String[] periods = period.split(" \\- ");
            if (periods.length > 1) {
                try {
                    String end = periods[1];
                    SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(formatter.parse(end));
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    if (Calendar.getInstance().before(calendar)) {
                        return true;
                    }
                } catch (ParseException e) {
                    return false;
                }
            }
        }
        return false;
    }

    public void share(Context context, DB db){
        ImageLoader imageLoader = ImageLoader.getInstance();
        File image = imageLoader.getDiskCache().get(getImageUrl());
        if (image == null) {
            image = imageLoader.getDiskCache().get(getSmallImageUrl());
        }
        if (image == null) {
            Toast.makeText(context, R.string.error_no_cached_image, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] split = getShop().split("/");
        String shop = split[split.length - 1];
        Cursor cursor = db.getData(DB.TABLE_SHOPS, DB.COLUMNS_SHOPS, DB.KEY_SHOP_URL + "='" + getShop() + "'");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                shop = cursor.getString(cursor.getColumnIndex(DB.KEY_SHOP_NAME));
            }
            cursor.close();
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.string_for_share_sale).replaceAll("shop", shop).replace("period", getPeriod()));

        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.string_chooser_send_in)));
    }

    private String getFavorite() {
        return String.valueOf(favorite);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setFavorite(String favorite) {
        this.favorite = Boolean.parseBoolean(favorite);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSmallImageUrl() {
        return smallImageUrl;
    }

    public void setSmallImageUrl(String smallImageUrl) {
        this.smallImageUrl = smallImageUrl;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }
}
