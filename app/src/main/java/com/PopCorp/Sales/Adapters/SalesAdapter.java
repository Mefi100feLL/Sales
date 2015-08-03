package com.PopCorp.Sales.Adapters;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.PopCorp.Sales.Callbacks.SaleClickListener;
import com.PopCorp.Sales.Data.Group;
import com.PopCorp.Sales.Data.Sale;
import com.PopCorp.Sales.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.ViewHolder> implements Filterable {

    private ArrayList<Sale> items;
    private SortedList<Sale> publishItems;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private SaleClickListener controller;
    private ArrayList<Group> groups;

    public SalesAdapter(SaleClickListener controller, ArrayList<Sale> array, ArrayList<Group> groups) {
        this.controller = controller;
        this.items = array;
        this.groups = groups;

        imageLoader = ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.back_right)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        publishItems = new SortedList<>(Sale.class, new SortedList.Callback<Sale>() {
            @Override
            public boolean areContentsTheSame(Sale oneItem, Sale twoItem) {
                return oneItem.equals(twoItem);
            }

            @Override
            public boolean areItemsTheSame(Sale oneItem, Sale twoItem) {
                return oneItem.equals(twoItem);
            }

            @Override
            public int compare(Sale oneItem, Sale twoItem) {
                return oneItem.compareTo(twoItem);
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
        update(items);
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    public void update(ArrayList<Sale> items) {
        publishItems.clear();
        Collections.sort(items);
        for (int i = 0; i < items.size(); i++) {
            Sale sale = items.get(i);
            if ((i - 1 >= 0 && (!items.get(i - 1).getGroup().equals(sale.getGroup()))) || (i - 1 < 0)) {
                Sale header = new Sale();
                for (Group group : groups) {
                    if (group.getUrl().equals(sale.getGroup())) {
                        String title = group.getName();
                        if (group.getPeriod().matches("(.*)[0-9]+ [^[0-9]]+[0-9]+(.*)")){
                            title += " (" + group.getPeriod() + ")";
                        }
                        header.setTitle(title);
                        break;
                    }
                }
                if (!header.getTitle().isEmpty()) {
                    header.setGroup(sale.getGroup());
                    publishItems.add(header);
                }
            }
            publishItems.add(sale);
        }
        notifyDataSetChanged();
    }

    public SortedList<Sale> getPublishItems() {
        return publishItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View view;
        public ImageView image;
        public ImageView favorite;
        public ImageView share;
        public ImageView buy;
        public TextView header;
        private ClickListener clickListener;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            image = (ImageView) view.findViewById(R.id.item_sale_image);
            favorite = (ImageView) view.findViewById(R.id.item_sale_favorite);
            share = (ImageView) view.findViewById(R.id.item_sale_share);
            buy = (ImageView) view.findViewById(R.id.item_sale_buy);
            header = (TextView) view.findViewById(R.id.item_sale_header_textview);
            if (image!=null) {
                view.setOnClickListener(this);
            }
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
        Sale sale = publishItems.get(position);

        if (!sale.isHeader()) {
            if (holder.image != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    holder.image.setTransitionName(sale.getId());
                }
                imageLoader.displayImage(sale.getSmallImageUrl(), holder.image, options);

                holder.setClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        if (position < publishItems.size() && position > -1) {
                            controller.onSaleClicked(v.findViewById(R.id.item_sale_image), publishItems.get(position));
                        }
                    }
                });
            }
            holder.favorite.setTag(sale);
            if (sale.isFavorite()){
                holder.favorite.setImageResource(R.drawable.ic_star_grey600_24dp);
            } else{
                holder.favorite.setImageResource(R.drawable.ic_star_outline_grey600_24dp);
            }
            holder.share.setTag(sale);
            holder.buy.setTag(sale);
            holder.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Sale sale = (Sale) v.getTag();
                    controller.changeFavorite(sale);
                }
            });
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Sale sale = (Sale) v.getTag();
                    controller.shareSale(sale);
                }
            });
            holder.buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Sale sale = (Sale) v.getTag();
                    controller.buySale(sale);
                }
            });
        } else {
            if (holder.header != null) {
                holder.header.setText(sale.getTitle());
            }
        }

        StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.view.getLayoutParams();
        if (params == null) {
            params = new StaggeredGridLayoutManager.LayoutParams(StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT, StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
        }
        params.setFullSpan(sale.isHeader());
        holder.view.setLayoutParams(params);
    }

    @Override
    public int getItemViewType(int position) {
        if (publishItems.get(position).isHeader()) {
            return 1;
        }
        return 2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v;
        if (position == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_header, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale, parent, false);
        }

        return new ViewHolder(v);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<Sale> newItems = (ArrayList<Sale>) results.values;
                update(newItems);
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Sale> FilteredArrayNames = new ArrayList<>();

                if (constraint.equals("")) {
                    results.count = items.size();
                    results.values = items;
                    return results;
                }

                for (Sale sale : items) {
                    if (sale.getGroup().equals(constraint)) {
                        FilteredArrayNames.add(sale);
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