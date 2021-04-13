
package com.example.myexpenses.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myexpenses.model.Expense;
import com.example.myexpenses.model.Income;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IncomeRepository extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;
    private static final String DATABASE_NAME = "transaction_database";
    private static final String TABLE_INCOME = "income";
    private static final String INCOME_ID = "id";
    private static final String INCOME_NAME = "name";
    private static final String INCOME_AMOUNT = "amount";
    private static final String INCOME_DATE = "date_of_income";

    public IncomeRepository(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_INCOME = "CREATE TABLE " + TABLE_INCOME + "("
                + INCOME_ID + " INTEGER PRIMARY KEY," + INCOME_NAME + " TEXT,"
                + INCOME_AMOUNT + " REAL," + INCOME_DATE + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE_INCOME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCOME);
        onCreate(db);
    }

    public void create(Income income) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        values.put(INCOME_NAME, income.getName());
        values.put(INCOME_AMOUNT, income.getAmount());
        values.put(INCOME_DATE, income.getDate().getTime());

        db.insert(TABLE_INCOME, null, values);
        db.close();
    }

    public List<Income> findAll() {
        List<Income> incomeList = new ArrayList<Income>();
        String selectQuery = "SELECT * FROM " + TABLE_INCOME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Income income = new Income();
                income.setId(cursor.getInt(0));
                income.setName(cursor.getString(1));
                income.setAmount(Float.parseFloat(cursor.getString(2)));
                income.setDate(new Date(cursor.getLong(3)));

                incomeList.add(income);
            } while (cursor.moveToNext());
        }
        return incomeList;
    }

    public List<Income> findAllBetweenTwoDates(Long dateFrom, Long dateTo) {
        List<Income> incomeList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_INCOME + " WHERE " + INCOME_DATE + " >= " + dateFrom + " AND " + INCOME_DATE + " <= " + dateTo;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Income income = new Income();
                income.setId(cursor.getInt(0));
                income.setName(cursor.getString(1));
                income.setAmount(Float.parseFloat(cursor.getString(2)));
                income.setDate(new Date(cursor.getLong(3)));

                incomeList.add(income);
            } while (cursor.moveToNext());
        }
        return incomeList;
    }

    public Integer returnSumOfIncomes(Long dateFrom, Long dateTo) {
        Integer sumOfIncomes = 0;

        String selectQuery = "SELECT SUM(" + INCOME_AMOUNT + ") " + " FROM " + TABLE_INCOME + " WHERE " + INCOME_DATE + " >= " + dateFrom + " AND " + INCOME_DATE + " <= " + dateTo;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            sumOfIncomes = cursor.getInt(0);

        }
        return sumOfIncomes;
    }

    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_INCOME);
    }
}
