package com.example.blooddonation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Donation.db";
    private static final int DATABASE_VERSION = 4; // Incremented version for schema update
    private static final String TABLE_NAME = "participants";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "NAME";
    private static final String COL_3 = "AGE";
    private static final String COL_4 = "ADDRESS";
    private static final String COL_5 = "PHONE_NUMBER";
    private static final String COL_6 = "BLOODGROUP";
    private static final String COL_7 = "UNITS"; // New column for units of blood

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT, " +
                COL_4 + " TEXT, " +
                COL_5 + " TEXT, " +
                COL_6 + " TEXT, " +
                COL_7 + " INTEGER)"); // Added column for units
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_7 + " INTEGER");
        }
    }

    public boolean insertData(String name, String age, String address, String phoneNumber, String bloodGroup, int units) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, age);
        contentValues.put(COL_4, address);
        contentValues.put(COL_5, phoneNumber);
        contentValues.put(COL_6, bloodGroup);
        contentValues.put(COL_7, units); // Insert units
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public ArrayList<String> getAllData() {
        ArrayList<String> participantsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (res.moveToFirst()) {
            do {
                String participantDetails = "Name: " + res.getString(res.getColumnIndex(COL_2)) +
                        "\nAge: " + res.getString(res.getColumnIndex(COL_3)) +
                        "\nAddress: " + res.getString(res.getColumnIndex(COL_4)) +
                        "\nPhone Number: " + res.getString(res.getColumnIndex(COL_5)) +
                        "\nBlood Group: " + res.getString(res.getColumnIndex(COL_6)) +
                        "\nUnits: " + res.getInt(res.getColumnIndex(COL_7)); // Units column
                participantsList.add(participantDetails);
            } while (res.moveToNext());
        }
        res.close();
        return participantsList;
    }


}
