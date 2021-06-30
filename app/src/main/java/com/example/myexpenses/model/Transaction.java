package com.example.myexpenses.model;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.myexpenses.R;
import com.example.myexpenses.arrayAdapter.ItemAdapter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import static com.example.myexpenses.other.CurrencyConverter.getValueInCurrency;

public class Transaction implements Comparable<Transaction>, Item, Serializable {

    private int id;
    private int type;
    private String name;
    private Integer amount;
    private String category;
    private Date date;

    public Transaction() {
    }

    public Transaction(int type, String name, Integer amount, String category, Date date) {
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public Transaction(int id, int type, String name, Integer amount, String category, Date date) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Transaction) {
            Transaction otherTransaction = (Transaction) obj;
            return id == otherTransaction.id &&
                    type == otherTransaction.type &&
                    name.equals(otherTransaction.name) &&
                    amount.equals(otherTransaction.amount) &&
                    category.equals(otherTransaction.category) &&
                    date.equals(otherTransaction.date);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, name, amount, category, date);
    }

    @NonNull
    @Override
    public String toString() {
        return "Id = " + id + ", Type = " + type + ", Name = " + name + ", Amount = " + amount + ", Category = " + category + ", Date = " + date;
    }

    @Override
    public int compareTo(Transaction transaction) {
        return this.date.compareTo(transaction.date);
    }

    @Override
    public int getViewType() {
        return ItemAdapter.RowType.LIST_ITEM.ordinal();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(LayoutInflater inflater, View view) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.transaction_row, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        int transactionAmount = this.amount;
        if (transactionAmount >= 0) {
            viewHolder.transactionAmount.setText(String.format("+%.2f", getValueInCurrency(transactionAmount)));
            viewHolder.transactionAmount.setTextColor(ContextCompat.getColor(view.getContext(), R.color.sum_greater_than_zero));
        } else {
            viewHolder.transactionAmount.setText(String.format("%.2f", getValueInCurrency(transactionAmount)));
            viewHolder.transactionAmount.setTextColor(ContextCompat.getColor(view.getContext(), R.color.sum_lesser_than_zero));
        }

        String transactionName = this.name;
        int lengthOfTransactionName = transactionName.length();
        if (lengthOfTransactionName >= 24) {
            int lengthOfTransactionAmount = String.valueOf(transactionAmount).length();
            if (transactionAmount > 0) {
                lengthOfTransactionAmount = lengthOfTransactionAmount + 2; //situation when '+' is added
            }
            int additionalSpaceForText;
            if (lengthOfTransactionAmount <= 4) {
                additionalSpaceForText = lengthOfTransactionAmount;
            } else {
                additionalSpaceForText = 7 - lengthOfTransactionAmount;
            }
            viewHolder.transactionName.setText(transactionName.substring(0, 20 + additionalSpaceForText) + "...");
        } else {
            viewHolder.transactionName.setText(transactionName);
        }

        String transactionCategory = this.category.toLowerCase().replace(" ", "_"); //prepare R.drawable.name: toLowerCase() because Android restrict Drawable filenames to not use Capital letters in their names, and also simple replace
        int res = view.getContext().getResources().getIdentifier(transactionCategory, "drawable", view.getContext().getPackageName());
        viewHolder.transactionImage.setImageResource(res);

        return view;
    }

    private class ViewHolder {
        TextView transactionAmount;
        TextView transactionName;
        ImageView transactionImage;

        ViewHolder(View convertView) {
            transactionAmount = (TextView) convertView.findViewById(R.id.amountTransaction);
            transactionName = (TextView) convertView.findViewById(R.id.nameTransaction);
            transactionImage = (ImageView) convertView.findViewById(R.id.imageTransaction);
        }
    }
}
