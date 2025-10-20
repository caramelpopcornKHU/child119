package com.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "location_log";

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "longitude REAL," +           // 0
                "latitude REAL," +            // 1
                "bearing REAL," +             // 2
                "speed REAL," +               // 3
                "time_sec TEXT," +            // 4
                "satellite_count INTEGER," +  // 5
                "method TEXT," +              // 6
                "accuracy REAL," +            // 7
                "map_match_code TEXT," +      // 8
                "map_match_lng TEXT," +       // 9
                "map_match_lat TEXT," +       //10
                "map_match_bearing TEXT," +   //11
                "real_time TEXT," +           //12
                "altitude REAL," +            //13
                "vertical_angle INTEGER," +   //14
                "provider TEXT," +            //15
                "activity_type INTEGER," +    //16
                "time_ms INTEGER" +           //17
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // insert 메서드
    public void insertLocation(
            Location location,
            int satelliteCount,
            String provider
    ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("longitude", location.getLongitude());
        values.put("latitude", location.getLatitude());
        values.put("bearing", location.getBearing());
        values.put("speed", location.getSpeed());
        values.put("time_sec", "");
        values.put("satellite_count", satelliteCount);
        values.put("method", "");
        values.put("accuracy", location.getAccuracy());
        values.put("map_match_code", "");
        values.put("map_match_lng", "");
        values.put("map_match_lat", "");
        values.put("map_match_bearing", "");
        values.put("real_time", "");
        values.put("altitude", location.getAltitude());
        values.put("vertical_angle", 0);
        values.put("provider", provider);
        values.put("activity_type", 0);
        values.put("time_ms", location.getTime());

        db.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllRows() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, "id ASC");
    }

    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);  // 조건 없이 전체 삭제
    }
}
