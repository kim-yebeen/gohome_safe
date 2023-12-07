package com.example.mobileappfinal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBManager extends SQLiteOpenHelper {
    public DBManager(Context context){
        super(context, "my_DB", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table persons(name text, num text, password text, egnum text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}