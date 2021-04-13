package com.example.myexpenses.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myexpenses.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseRepository extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "transaction_database";
    private static final String TABLE_EXPENSE = "expense";
    private static final String EXPENSE_ID = "id";
    private static final String EXPENSE_NAME = "name";
    private static final String EXPENSE_AMOUNT = "amount";
    private static final String EXPENSE_CATEGORY = "category";
    private static final String EXPENSE_DATE = "date_of_expense";

    public ExpenseRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_EXPENSE = "CREATE TABLE " + TABLE_EXPENSE + "("
                + EXPENSE_ID + " INTEGER PRIMARY KEY," + EXPENSE_NAME + " TEXT,"
                + EXPENSE_AMOUNT + " REAL," + EXPENSE_CATEGORY + " TEXT," + EXPENSE_DATE + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE_EXPENSE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        onCreate(db);
    }

    public void create(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(EXPENSE_NAME, expense.getName());
        values.put(EXPENSE_AMOUNT, expense.getAmount());
        values.put(EXPENSE_CATEGORY, expense.getCategory());
        values.put(EXPENSE_DATE, expense.getDate().getTime());

        db.insert(TABLE_EXPENSE, null, values);
        db.close();
    }

    public List<Expense> findAll() {
        List<Expense> expenseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EXPENSE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(0));
                expense.setName(cursor.getString(1));
                expense.setAmount(Float.parseFloat(cursor.getString(2)));
                expense.setCategory(cursor.getString(3));
                expense.setDate(new Date(cursor.getLong(4)));

                expenseList.add(expense);
            } while (cursor.moveToNext());
        }
        return expenseList;
    }

    public List<Expense> findAllBetweenTwoDates(Long dateFrom, Long dateTo) {
        List<Expense> expenseList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_EXPENSE + " WHERE " + EXPENSE_DATE + " >= " + dateFrom + " AND " + EXPENSE_DATE + " <= " + dateTo;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(0));
                expense.setName(cursor.getString(1));
                expense.setAmount(Float.parseFloat(cursor.getString(2)));
                expense.setCategory(cursor.getString(3));
                expense.setDate(new Date(cursor.getLong(4)));

                expenseList.add(expense);
            } while (cursor.moveToNext());
        }
        return expenseList;
    }

    public Integer returnSumOfExpenses(Long dateFrom, Long dateTo) {
        Integer sumOfExpenses = 0;

        String selectQuery = "SELECT SUM(" + EXPENSE_AMOUNT + ") " + " FROM " + TABLE_EXPENSE + " WHERE " + EXPENSE_DATE + " >= " + dateFrom + " AND " + EXPENSE_DATE + " <= " + dateTo;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            sumOfExpenses = cursor.getInt(0);

        }
        return sumOfExpenses;
    }

    public List<Expense> findLatest10Expenses() {
        List<Expense> expenseList = new ArrayList<>();

        String selectQuery = "SELECT * FROM expense ORDER BY date_of_expense DESC limit 10";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense();
                expense.setId(cursor.getInt(0));
                expense.setName(cursor.getString(1));
                expense.setAmount(Float.parseFloat(cursor.getString(2)));
                expense.setCategory(cursor.getString(3));
                expense.setDate(new Date(cursor.getLong(4)));

                expenseList.add(expense);
            } while (cursor.moveToNext());
        }
        return expenseList;
    }


    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EXPENSE);
    }
}
