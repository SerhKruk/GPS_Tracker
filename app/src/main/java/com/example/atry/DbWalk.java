package com.example.atry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DbWalk extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "tracker";
    private static final int DATABASE_VERSION = 1;


    private static final String CREATE_TABLE_WALKS = "CREATE TABLE "
            + DbWalkHelper.TABLE_WALKS + "(" + DbWalkHelper.WALK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbWalkHelper.TIME_START + " TEXT,"
            + DbWalkHelper.DURATION + " TEXT,"
            + DbWalkHelper.DISTANCE + " REAL,"
            + DbWalkHelper.SPEED + " REAL );";

    private static final String CREATE_TABLE_LOCATIONS = "CREATE TABLE "
            + DbWalkHelper.TABLE_LOCATIONS + "(" + DbWalkHelper.LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DbWalkHelper.LATITUDE + " REAL,"
            + DbWalkHelper.LONGITUDE + " REAL,"
            + DbWalkHelper.ORDER + " REAL, "
            + DbWalkHelper.WALK_ID + "  INTEGER,"
            + " FOREIGN KEY (" + DbWalkHelper.WALK_ID + ") REFERENCES " + DbWalkHelper.TABLE_WALKS + " (" + DbWalkHelper.WALK_ID + "));"
            ;


    public DbWalk(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WALKS);
        db.execSQL(CREATE_TABLE_LOCATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + DbWalkHelper.TABLE_WALKS + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + DbWalkHelper.TABLE_LOCATIONS + "'");
        onCreate(db);
    }

    public long insertWalk(String start_time, String duration, String distance, String speed) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbWalkHelper.TIME_START, start_time);
        values.put(DbWalkHelper.DURATION, duration);
        values.put(DbWalkHelper.DISTANCE, distance);
        values.put(DbWalkHelper.SPEED, speed);

        //insert row in clubs table
        long insert = db.insert(DbWalkHelper.TABLE_WALKS, null, values);

        return insert;
    }

    public long insertLocation(String latitude, String longitude, String order, String walk_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbWalkHelper.LATITUDE, latitude);
        values.put(DbWalkHelper.LONGITUDE, longitude);
        values.put(DbWalkHelper.ORDER, order);
        values.put(DbWalkHelper.WALK_ID, walk_id);

        //insert row in clubs table
        long insert = db.insert(DbWalkHelper.TABLE_LOCATIONS, null, values);
        return insert;
    }

    public ArrayList<Walk> getWalks()
    {
        String time_start = "", duration  = "";
        double distance, speed;
        int id;
        ArrayList<Walk> walks = new ArrayList<Walk>();
        String selectQuery = "SELECT  * FROM " + DbWalkHelper.TABLE_WALKS + " ORDER BY " + DbWalkHelper.TIME_START + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                time_start = c.getString(c.getColumnIndex(DbWalkHelper.TIME_START));
                duration = c.getString(c.getColumnIndex(DbWalkHelper.DURATION));
                distance = Double.parseDouble(c.getString(c.getColumnIndex(DbWalkHelper.DISTANCE)));
                id = Integer.parseInt(c.getString(c.getColumnIndex(DbWalkHelper.WALK_ID)));
                speed = Double.parseDouble(c.getString(c.getColumnIndex(DbWalkHelper.SPEED)));
                Walk temp = new Walk(id, time_start, duration, distance, speed);
                walks.add(temp);
            } while (c.moveToNext());
        }
        return walks;
    }

    public ArrayList<Location> getLocations(int walk_id) {
        ArrayList<Location> locations = new ArrayList<Location>();
        double latitude, longitude;
        String str_walk_id = String.valueOf(walk_id);
        String selectQuery = "SELECT  * FROM " + DbWalkHelper.TABLE_LOCATIONS + " WHERE " + DbWalkHelper.WALK_ID + " == ? ORDER BY " + DbWalkHelper.ORDER + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[] {str_walk_id});
        if (c.moveToFirst())
        {
            do {
                latitude = Double.parseDouble(c.getString(c.getColumnIndex(DbWalkHelper.LATITUDE)));
                longitude = Double.parseDouble(c.getString(c.getColumnIndex(DbWalkHelper.LONGITUDE)));
                Location loc = new Location(walk_id, latitude, longitude);
                locations.add(loc);
            } while (c.moveToNext());
        }
        return locations;
    }

    /// sort by and for period

}