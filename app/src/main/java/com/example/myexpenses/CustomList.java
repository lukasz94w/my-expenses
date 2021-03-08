package com.example.myexpenses;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] categoryName;
    private final Integer[] categoryImageId;

    public CustomList(Activity context, String[] categoryName, Integer[] categoryImageId) {
        super(context, R.layout.my_list, categoryName);
        this.context = context;
        this.categoryName = categoryName;
        this.categoryImageId = categoryImageId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.my_list, null, true);

        TextView txtTitle = rowView.findViewById(R.id.txt);
        ImageView imageView = rowView.findViewById(R.id.img);

        txtTitle.setText(categoryName[position]);
        imageView.setImageResource(categoryImageId[position]);
        return rowView;
    }
}
