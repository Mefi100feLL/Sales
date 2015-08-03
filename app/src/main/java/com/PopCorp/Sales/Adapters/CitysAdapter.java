package com.PopCorp.Sales.Adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.PopCorp.Sales.Data.City;
import com.PopCorp.Sales.R;

import java.util.ArrayList;

public class CitysAdapter extends BaseAdapter implements SectionIndexer {

    private final int[] colors;
    private final Context context;
    private ArrayList<City> objects;
    private City selectedCity;
    private final ArrayList<Section> sections = new ArrayList<>();

    public CitysAdapter(Context context, ArrayList<City> array) {
        super();
        this.context = context;
        this.objects = array;
        for (City city : objects) {
            Section section = new Section(city.getName().substring(0, 1), objects.indexOf(city));
            if (!sections.contains(section)) {
                sections.add(section);
            }
        }
        final TypedArray ta = context.getResources().obtainTypedArray(R.array.colors);
        colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();
    }

    public void setObjects(ArrayList<City> array) {
        this.objects = array;
        for (City city : objects) {
            Section section = new Section(city.getName().substring(0, 1), objects.indexOf(city));
            if (!sections.contains(section)) {
                sections.add(section);
            }
        }
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_list_city, parent, false);
        }
        City city = objects.get(position);
        if (selectedCity != null && city.equals(selectedCity)) {
            view.setBackgroundResource(R.drawable.selector_for_normal_list);
            view.setActivated(true);
        } else {
            view.setActivated(false);
        }

        TextView name = (TextView) view.findViewById(R.id.item_city_name);
        TextView region = (TextView) view.findViewById(R.id.item_city_region);

        name.setText(city.getName());
        if (city.getRegion() != null && !city.getRegion().isEmpty()) {
            region.setVisibility(View.VISIBLE);
            region.setText(city.getRegion());
        } else {
            region.setVisibility(View.GONE);
        }

        Drawable drawable = createSelector(colors[position % colors.length]);

        ImageView image = (ImageView) view.findViewById(R.id.item_city_image);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            image.setBackground(drawable);
        } else {
            image.setBackgroundDrawable(drawable);
        }

        TextView word = (TextView) view.findViewById(R.id.item_city_word);
        word.setText(city.getName().substring(0, 1).toUpperCase());
        return view;
    }

    public City getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(int position) {
        this.selectedCity = objects.get(position);
        notifyDataSetChanged();
    }

    private Drawable createSelector(int color) {
        ShapeDrawable coloredCircle = new ShapeDrawable(new OvalShape());
        coloredCircle.getPaint().setColor(color);
        return coloredCircle;
    }

    @Override
    public Object[] getSections() {
        return sections.toArray(new Section[sections.size()]);
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return sections.get(sectionIndex).getPosition();
    }

    @Override
    public int getSectionForPosition(int position) {
        String word = objects.get(position).getName().substring(0, 1);
        for (Section section : sections) {
            if (section.getSection().equals(word)) {
                return sections.indexOf(section);
            }
        }
        return 0;
    }

    private class Section {

        private String section;
        private int position;

        public Section(String section, int position) {
            setSection(section);
            setPosition(position);
        }

        @Override
        public boolean equals(Object object) {
            Section section = (Section) object;
            return section.getSection().equals(getSection());
        }

        @Override
        public String toString() {
            return getSection();
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
