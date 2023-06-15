package com.ai.excelsheetdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class Database {

    public static final String DATABASE_NAME = "ExcelDemoDB";
    public static final int DATABSE_VERSION = 1;

    public static final String TABLE_NAME_USERLIST = "userlist";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private static final String CREATE_TABLE_USERLIST = "create table userlist(uid INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT,password TEXT);";


    private Context context;
    SQLiteDatabase db;
    DatabaseHelper dbhelper;

    public Database(Context context) {
        this.context = context;
        dbhelper =new DatabaseHelper(context);
    }

    class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context)
        {
            super(context,DATABASE_NAME,null,DATABSE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_USERLIST);

            Log.d("Table","Created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS");
            onCreate(db);
        }
    }

    public Database open()
    {
        db=dbhelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbhelper.close();
    }


    public long insert_user(String username, String password) {

        ContentValues initialvalues = new ContentValues();

        initialvalues.put(USERNAME, username);
        initialvalues.put(PASSWORD, password);

        return db.insert(TABLE_NAME_USERLIST, null, initialvalues);

    }

    public Cursor get_userlist() {
        String query = "SELECT * FROM " + TABLE_NAME_USERLIST;
        Cursor cur = db.rawQuery(query, null);
        return cur;
    }

    public void delete_All_user() {
        // TODO Auto-generated method stub
        db.delete(TABLE_NAME_USERLIST, null, null);
        Log.d("Deleted All", "userlist.....");

    }
}
