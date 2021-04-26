package com.example.myexpenses.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myexpenses.model.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TransactionRepository extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 16;
    private static final String DATABASE_NAME = "transactions_database";
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TRANSACTION_ID = "id";
    private static final String TRANSACTION_TYPE = "type";
    private static final String TRANSACTION_NAME = "name";
    private static final String TRANSACTION_AMOUNT = "amount";
    private static final String TRANSACTION_CATEGORY = "category";
    private static final String TRANSACTION_DATE = "date";

    public TransactionRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_TRANSACTION = "CREATE TABLE " + TABLE_TRANSACTION + " ("
                + TRANSACTION_ID + " INTEGER PRIMARY KEY, " + TRANSACTION_TYPE + " INTEGER, " + TRANSACTION_NAME + " TEXT, "
                + TRANSACTION_AMOUNT + " REAL, " + TRANSACTION_CATEGORY + " TEXT, " + TRANSACTION_DATE + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE_TRANSACTION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        onCreate(db);
    }

    public void create(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TRANSACTION_TYPE, transaction.getType());
        values.put(TRANSACTION_NAME, transaction.getName());
        values.put(TRANSACTION_AMOUNT, transaction.getAmount());
        values.put(TRANSACTION_CATEGORY, transaction.getCategory());
        values.put(TRANSACTION_DATE, transaction.getDate().getTime());

        db.insert(TABLE_TRANSACTION, null, values);
        db.close();
    }

    public List<Transaction> findAll() {
        List<Transaction> transactionList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setType(cursor.getInt(1));
                transaction.setName(cursor.getString(2));
                transaction.setAmount(Float.parseFloat(cursor.getString(3)));
                transaction.setCategory(cursor.getString(4));
                transaction.setDate(new Date(cursor.getLong(5)));

                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        return transactionList;
    }

    public List<Transaction> findTransactionsInMonth(int month, int year) {
        List<Long> monthBoundaries = getMonthBoundaries(month, year);
        Long dateFrom = monthBoundaries.get(0);
        Long dateTo = monthBoundaries.get(1);
        List<Transaction> transactionList = new LinkedList<>();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_DATE + " >= " + dateFrom + " AND " + TRANSACTION_DATE + " < " + dateTo
                + " ORDER BY " + TRANSACTION_TYPE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setType(cursor.getInt(1));
                transaction.setName(cursor.getString(2));
                transaction.setAmount(Float.parseFloat(cursor.getString(3)));
                transaction.setCategory(cursor.getString(4));
                transaction.setDate(new Date(cursor.getLong(5)));

                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        return transactionList;
    }

    public List<Transaction> findExpensesInMonth(int month, int year) {
        List<Long> monthBoundaries = getMonthBoundaries(month, year);
        Long dateFrom = monthBoundaries.get(0);
        Long dateTo = monthBoundaries.get(1);
        List<Transaction> expensesList = new LinkedList<>();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_DATE + " >= " + dateFrom + " AND " + TRANSACTION_DATE + " <= " + dateTo + " AND type = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setType(cursor.getInt(1));
                transaction.setName(cursor.getString(2));
                transaction.setAmount(Float.parseFloat(cursor.getString(3)));
                transaction.setCategory(cursor.getString(4));
                transaction.setDate(new Date(cursor.getLong(5)));

                expensesList.add(transaction);
            } while (cursor.moveToNext());
        }
        return expensesList;
    }

    public List<Transaction> findIncomesInMonth(int month, int year) {
        List<Long> monthBoundaries = getMonthBoundaries(month, year);
        Long dateFrom = monthBoundaries.get(0);
        Long dateTo = monthBoundaries.get(1);
        List<Transaction> expensesList = new LinkedList<>();

        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_DATE + " >= " + dateFrom + " AND " + TRANSACTION_DATE + " <= " + dateTo + " AND type = 0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setType(cursor.getInt(1));
                transaction.setName(cursor.getString(2));
                transaction.setAmount(Float.parseFloat(cursor.getString(3)));
                transaction.setCategory(cursor.getString(4));
                transaction.setDate(new Date(cursor.getLong(5)));

                expensesList.add(transaction);
            } while (cursor.moveToNext());
        }
        return expensesList;
    }

    public Integer getSumOfMonthlyExpenses(int month, int year) {
        List<Long> monthBoundaries = getMonthBoundaries(month, year);
        Long dateFrom = monthBoundaries.get(0);
        Long dateTo = monthBoundaries.get(1);
        Integer sumOfTransactions = 0;

        String selectQuery = "SELECT SUM(" + TRANSACTION_AMOUNT + ") " + " FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_DATE + " >= " + dateFrom + " AND " + TRANSACTION_DATE + " <= " + dateTo + " AND type = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            sumOfTransactions = cursor.getInt(0);
        }
        return sumOfTransactions;
    }

    public Integer getSumOfDailyExpenses(int day, int month, int year) {
        Long dayAsUnix = getDay(day, month, year);
        Integer sumOfTransactions = 0;

        String selectQuery = "SELECT SUM(" + TRANSACTION_AMOUNT + ") " + " FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_DATE + " == " + dayAsUnix + " AND type = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            sumOfTransactions = cursor.getInt(0);
        }
        return sumOfTransactions;
    }

    public List<Transaction> search(String keyword, int month, int year) {
        List<Long> monthBoundaries = getMonthBoundaries(month, year);
        Long dateFrom = monthBoundaries.get(0);
        Long dateTo = monthBoundaries.get(1);
        List<Transaction> transactionList = new LinkedList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_DATE + " >= " + dateFrom + " AND " + TRANSACTION_DATE + " <= " + dateTo + " AND " + TRANSACTION_NAME + " LIKE ?", new String[]{"%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(0));
                transaction.setType(cursor.getInt(1));
                transaction.setName(cursor.getString(2));
                transaction.setAmount(Float.parseFloat(cursor.getString(3)));
                transaction.setCategory(cursor.getString(4));
                transaction.setDate(new Date(cursor.getLong(5)));

                transactionList.add(transaction);
            } while (cursor.moveToNext());
        }
        return transactionList;
    }

    public void update(Transaction updatedTransaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_TYPE, updatedTransaction.getType());
        values.put(TRANSACTION_NAME, updatedTransaction.getName());
        values.put(TRANSACTION_AMOUNT, updatedTransaction.getAmount());
        values.put(TRANSACTION_CATEGORY, updatedTransaction.getCategory());
        values.put(TRANSACTION_DATE, updatedTransaction.getDate().getTime());

        db.update(TABLE_TRANSACTION, values, TRANSACTION_ID + "=" + updatedTransaction.getId(), null);
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSACTION + " WHERE " + TRANSACTION_ID + " = " + id);
    }

    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_TRANSACTION);
    }

    private List<Long> getMonthBoundaries(int month, int year) {
        List<Long> monthBoundaries = new LinkedList<>();
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Long startOfMonthUnix = calendar.getTime().getTime();
            monthBoundaries.add(startOfMonthUnix);
        }

        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, 1); //problem of 29 march
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Long endOfMonthUnix = calendar.getTime().getTime();
            monthBoundaries.add(endOfMonthUnix);
        }
        return monthBoundaries;
    }

    private Long getDay(int day, int month, int year) {
        Long dayAsUnix;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dayAsUnix = calendar.getTime().getTime();

        return dayAsUnix;
    }
}
