package com.example.myexpenses.customAdapter;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.myexpenses.R;
import com.example.myexpenses.model.Transaction;

import java.util.ArrayList;
import java.util.Date;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private LayoutInflater inflater;

    public TransactionAdapter(Activity context, ArrayList transactionList) {
        super(context, 0, transactionList);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View cellView;
        Transaction cell = getItem(position);

        //if the cell is a section header inflate the header layout
        if (cell.isSectionHeader()) {
            cellView = inflater.inflate(R.layout.transaction_header_newday, null);
            TextView headerName = cellView.findViewById(R.id.headerName);

            Date fullDate = cell.getDate();
            String dayOfMonth = (String) DateFormat.format("dd", fullDate);
            String month = (String) DateFormat.format("MM", fullDate);
            String year = (String) DateFormat.format("yyyy", fullDate);
            String dayOfWeek = (String) DateFormat.format("EEEE", fullDate);
            String formattedDate = dayOfMonth + '/' + month + "/" + year + ", " + dayOfWeek;

            headerName.setText(formattedDate);
            //turn off clickable if not I show empty modify record when click on it
            cellView.setEnabled(false);
            cellView.setClickable(false);
            cellView.setOnClickListener(null);
        }

        //the cell is another row from database
        else {
            cellView = inflater.inflate(R.layout.transaction_row, null);

            TextView transactionAmount = cellView.findViewById(R.id.amountTransaction);
            double transactionAmountAsDouble = cell.getAmount();
            transactionAmountAsDouble = Math.round(transactionAmountAsDouble * 100);
            transactionAmountAsDouble = transactionAmountAsDouble / 100; //round to two decimal digits, for example 66.9899000 -> 66.99
            if (transactionAmountAsDouble >= 0) {
                transactionAmount.setText(String.format("+%.2f", transactionAmountAsDouble));
                transactionAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.ColorPrimary));
            } else {
                transactionAmount.setText(String.format("%.2f", transactionAmountAsDouble));
                transactionAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.bacgroundColorPopup));
            }

            TextView transactionName = cellView.findViewById(R.id.nameTransaction);
            Integer lengthOfTransactionName = cell.getName().length();
            if (lengthOfTransactionName >= 24) {
                int lengthOfTransactionAmount = Double.toString(transactionAmountAsDouble).length();
                if (transactionAmountAsDouble > 0) {
                    lengthOfTransactionAmount = lengthOfTransactionAmount + 2; //situation when '+' is added
                }
                int additionalSpaceForText;
                if (lengthOfTransactionAmount <= 4) {
                    additionalSpaceForText = lengthOfTransactionAmount;
                } else {
                    additionalSpaceForText = 7 - lengthOfTransactionAmount;
                }
                transactionName.setText(cell.getName().substring(0, 20 + additionalSpaceForText) + "...");
            } else if (lengthOfTransactionName == 0) { //if name of transaction is empty we set it as category name
                transactionName.setText(cell.getCategory());
            }
            else {
                transactionName.setText(cell.getName());
            }

            ImageView transactionImage = cellView.findViewById(R.id.imageTransaction);
            String categoryName = cell.getCategory().toLowerCase().replace(" ", "_"); //prepare R.drawable.name: toLowerCase() because Android restrict Drawable filenames to not use Capital letters in their names, and also simple replace
            int res = getContext().getResources().getIdentifier(categoryName, "drawable", getContext().getPackageName());
            transactionImage.setImageResource(res);
        }
        return cellView;
    }
}
