package com.example.myexpenses.model;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.myexpenses.R;
import com.example.myexpenses.customAdapter.ItemAdapter;

import java.util.Date;

public class Header implements Item {
    private final Date date;

    public Header(Date date) {
        this.date = date;
    }

    @Override
    public int getViewType() {
        return ItemAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View view) {

        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.transaction_header_newday, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Date fullDate = this.date;
        String dayOfMonth = (String) DateFormat.format("dd", fullDate);
        String month = (String) DateFormat.format("MM", fullDate);
        String year = (String) DateFormat.format("yyyy", fullDate);
        String dayOfWeek = (String) DateFormat.format("EEEE", fullDate);
        String formattedDate = dayOfMonth + '/' + month + "/" + year + ", " + dayOfWeek;

        viewHolder.headerName.setText(formattedDate);

        //turn off clickable if not I show empty modify record when click on it
        view.setEnabled(false);
        view.setClickable(false);
        view.setOnClickListener(null);

        return view;
    }

    private class ViewHolder {
        TextView headerName;

        ViewHolder(View convertView) {
            headerName = (TextView) convertView.findViewById(R.id.headerName);
        }
    }
}
