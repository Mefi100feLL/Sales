package com.PopCorp.Sales.Adapters;

import android.graphics.Bitmap;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Sales.Controllers.CategoryController;
import com.PopCorp.Sales.Data.Shop;
import com.PopCorp.Sales.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> implements Filterable {

    public static final String FILTER_TYPE_FAVORITE = "FILTER_TYPE_FAVORITE";
    public static final String FILTER_TYPE_ALL = "FILTER_TYPE_ALL";

    private final CategoryController controller;
    private ArrayList<Shop> items;
    private SortedList<Shop> publishItems;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public CategoryAdapter(CategoryController controller, ArrayList<Shop> array) {
        this.controller = controller;
        this.items = array;
        imageLoader = ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.back_right)
                .imageScaleType(ImageScaleType.EXACTLY)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        publishItems = new SortedList<>(Shop.class, new SortedList.Callback<Shop>() {
            @Override
            public boolean areContentsTheSame(Shop oneItem, Shop twoItem) {
                return oneItem.equals(twoItem);
            }

            @Override
            public boolean areItemsTheSame(Shop oneItem, Shop twoItem) {
                return oneItem.equals(twoItem);
            }

            @Override
            public int compare(Shop oneItem, Shop twoItem) {
                return oneItem.getName().compareToIgnoreCase(twoItem.getName());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }
        });
    }

    public SortedList<Shop> getPublishItems() {
        return publishItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View view;
        public ImageView image;
        public TextView name;
        public ImageView favorite;
        private ClickListener clickListener;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            image = (ImageView) view.findViewById(R.id.item_shop_image);
            name = (TextView) view.findViewById(R.id.item_shop_name);
            favorite = (ImageView) view.findViewById(R.id.item_shop_image_favorite);
            view.setOnClickListener(this);
        }

        public interface ClickListener {
            void onClick(View v, int position);
        }

        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return publishItems.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Shop shop = publishItems.get(position);

        imageLoader.displayImage(shop.getImage(), holder.image, options);
        holder.name.setText(shop.getName());
        holder.favorite.setImageResource(shop.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_outline_yellow_24dp);

        holder.favorite.setTag(shop);
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shop shop = (Shop) v.getTag();
                shop.setFavorite(!shop.isFavorite());
                ((ImageView) v).setImageResource(shop.isFavorite() ? R.drawable.ic_star_yellow_24dp : R.drawable.ic_star_outline_yellow_24dp);
                publishItems.updateItemAt(publishItems.indexOf(shop), shop);
                controller.updateShopFavorite(shop);
            }
        });

        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position) {
                if (position < publishItems.size() && position > -1) {
                    controller.openShop(publishItems.get(position));
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<Shop> newItems = (ArrayList<Shop>) results.values;
                ArrayList<Shop> itemsForRemove = new ArrayList<>();
                for (int i = 0; i < publishItems.size(); i++) {
                    if (!newItems.contains(publishItems.get(i))) {
                        itemsForRemove.add(publishItems.get(i));
                    }
                }
                for (Shop item : itemsForRemove) {
                    publishItems.remove(item);
                }
                for (Shop item : newItems) {
                    if (publishItems.indexOf(item) == -1) {
                        publishItems.add(item);
                    }
                }
                if (publishItems.size()==0){
                    if (constraint.equals(FILTER_TYPE_ALL)) {
                        controller.getUiController().showEmpty(R.string.empty_no_shops, R.drawable.ic_empty_shop);
                    } else{
                        controller.getUiController().showEmpty(R.string.empty_no_favorite_shops, R.drawable.ic_empty_favorite);
                    }
                } else{
                    controller.getUiController().showListView();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Shop> FilteredArrayNames = new ArrayList<>();

                if (constraint.equals(FILTER_TYPE_ALL)) {
                    results.count = items.size();
                    results.values = items;
                    return results;
                }

                for (int i = 0; i < items.size(); i++) {
                    Shop item = items.get(i);
                    if (item.isFavorite()) {
                        FilteredArrayNames.add(item);
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;
                return results;
            }
        };

        return filter;
    }
}