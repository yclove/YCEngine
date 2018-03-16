package com.ycengine.rainsound.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ycengine.rainsound.Constants;
import com.ycengine.rainsound.R;
import com.ycengine.yclibrary.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Databases {
    private final static String DB_NAME = "RainSound";
    private final static int DB_VERSION = 5;
    private Context mContext;
    private DBOpenHelper mDbHelper;
    private SQLiteDatabase mDb;

    public Databases(Context context) {
        mContext = context;
    }

    public class DBOpenHelper extends SQLiteOpenHelper {
        public DBOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL(CREATE_CORE_TABLE);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            LogUtil.e("DataBase Update !!!");
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.DB_TABLE_CORE_LIST);
            db.beginTransaction();
            try {
                db.execSQL(CREATE_CORE_TABLE);
                db.setTransactionSuccessful();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
    }

    public DBOpenHelper open() {
        mDbHelper = new DBOpenHelper(mContext);
        try {
            mDb = mDbHelper.getWritableDatabase();
        } catch (Exception ex) {
            mDb = mDbHelper.getReadableDatabase();
        }
        return mDbHelper;
    }

    public void close() {
        mDbHelper.close();
    }

    private final static String CREATE_CORE_TABLE = "CREATE TABLE IF NOT EXISTS " + DBInfo.DB_TABLE_CORE_LIST +
            "(" + DBInfo.COLUMN_CORE_LIST_POSITION + " INT, "
            + DBInfo.COLUMN_CORE_LIST_STATUS + " CHARACTER(1), "
            + DBInfo.COLUMN_CORE_LIST_TITLE + " INT, "
            + DBInfo.COLUMN_CORE_LIST_PHOTO + " INT, "
            + DBInfo.COLUMN_CORE_LIST_PHOTOGRAPHER + " NVARCHAR(100), "
            + DBInfo.COLUMN_CORE_LIST_PHOTO_REFERRER + " NVARCHAR(100), "
            + DBInfo.COLUMN_CORE_LIST_MUSIC + " INT, "
            + DBInfo.COLUMN_CORE_LIST_MUSICIAN + " NVARCHAR(100), "
            + DBInfo.COLUMN_CORE_LIST_MUSIC_REFERRER + " NVARCHAR(100), "
            + DBInfo.COLUMN_CORE_LIST_REGDATE + " NVARCHAR(17)" + " )";

    public ArrayList<CoreItem> getCoreItemList() {
        ArrayList<CoreItem> arrCoreItem = new ArrayList<CoreItem>();

        Cursor cursor = mDb.query(DBInfo.DB_TABLE_CORE_LIST, null, null, null, null, null, DBInfo.COLUMN_CORE_LIST_POSITION + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CoreItem item = new CoreItem();
                int position = cursor.getInt(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_POSITION));
                String status = cursor.getString(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_STATUS));
                int title = cursor.getInt(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_TITLE));
                int photo = cursor.getInt(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_PHOTO));
                String photographer = cursor.getString(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_PHOTOGRAPHER));
                String photoreferrer = cursor.getString(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_PHOTO_REFERRER));
                int music = cursor.getInt(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_MUSIC));
                String musician = cursor.getString(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_MUSICIAN));
                String musicreferrer = cursor.getString(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_MUSIC_REFERRER));
                String regdate = cursor.getString(cursor.getColumnIndex(DBInfo.COLUMN_CORE_LIST_REGDATE));

                item.setCorePosition(position);
                item.setCoreStatus(status);
                item.setCoreTitle(title);
                item.setCorePhoto(photo);
                item.setCorePhotographer(photographer);
                item.setCorePhotoReferrer(photoreferrer);
                item.setCoreMusic(music);
                item.setCoreMusician(musician);
                item.setCoreMusicReferrer(musicreferrer);
                item.setCoreRegDate(regdate);
                arrCoreItem.add(item);
            }
            cursor.close();
        }

        if (arrCoreItem.size() == 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
            String currentDate = sdf.format(new Date());
            long lngCurrentDate = Long.parseLong(currentDate);

            arrCoreItem.add(new CoreItem(0, Constants.CORE_STATUS_ENABLED, R.string.core_bungalow_downpour, R.drawable.core_bungalow_downpour, "Erik Cleves Kristensen", "Flickr", R.raw.core_bungalow_downpour, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate)));
            arrCoreItem.add(new CoreItem(1, Constants.CORE_STATUS_ENABLED, R.string.core_dripping_water, R.drawable.core_dripping_water, "Maxime Bober", "Flickr", R.raw.core_dripping_water, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 2)));
            arrCoreItem.add(new CoreItem(2, Constants.CORE_STATUS_ENABLED, R.string.core_hailstorm, R.drawable.core_hailstorm, "Jason Thibault", "Flickr", R.raw.core_hailstorm, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 3)));
            arrCoreItem.add(new CoreItem(3, Constants.CORE_STATUS_ENABLED, R.string.core_inside_car, R.drawable.core_inside_car, "Luca Sartoni", "Flickr", R.raw.core_inside_car, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 5)));
            arrCoreItem.add(new CoreItem(4, Constants.CORE_STATUS_ENABLED, R.string.core_inside_motorhome, R.drawable.core_inside_motorhome, "Angeli Laura De", "Flickr", R.raw.core_inside_motorhome, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 7)));
            arrCoreItem.add(new CoreItem(5, Constants.CORE_STATUS_ENABLED, R.string.core_light_rain_backyard, R.drawable.core_light_rain_backyard, "Chiara", "Flickr", R.raw.core_light_rain_backyard, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 8)));
            arrCoreItem.add(new CoreItem(6, Constants.CORE_STATUS_ENABLED, R.string.core_light_rain_night, R.drawable.core_light_rain_night, "Allison Meier", "Flickr", R.raw.core_light_rain_night, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 9)));
            arrCoreItem.add(new CoreItem(7, Constants.CORE_STATUS_ENABLED, R.string.core_lodge_rainforest3, R.drawable.core_eiffel_tower, "JH", "YC", R.raw.core_lodge_rainforest2, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 11)));
            arrCoreItem.add(new CoreItem(8, Constants.CORE_STATUS_ENABLED, R.string.core_morning_rain, R.drawable.core_morning_rain, "Max Stolbinsky", "Flickr", R.raw.core_morning_rain, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 12)));
            arrCoreItem.add(new CoreItem(9, Constants.CORE_STATUS_ENABLED, R.string.core_puddles, R.drawable.core_puddles, "Christoph Zurnieden", "Flickr", R.raw.core_puddles, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 13)));
            arrCoreItem.add(new CoreItem(10, Constants.CORE_STATUS_ENABLED, R.string.core_rain_backyard, R.drawable.core_rain_backyard, "Su-May", "Flickr", R.raw.core_rain_backyard, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 14)));
            arrCoreItem.add(new CoreItem(11, Constants.CORE_STATUS_ENABLED, R.string.core_rain_city, R.drawable.core_rain_city, "Livio Burtscher", "Flickr", R.raw.core_rain_city, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 16)));
            arrCoreItem.add(new CoreItem(12, Constants.CORE_STATUS_ENABLED, R.string.core_rain_forest, R.drawable.core_rain_forest, "anasararojas", "Flickr", R.raw.core_rain_forest, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 17)));
            arrCoreItem.add(new CoreItem(13, Constants.CORE_STATUS_ENABLED, R.string.core_rain_gutter, R.drawable.core_rain_gutter, "Sean Thornton", "Flickr", R.raw.core_rain_gutter, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 18)));
            arrCoreItem.add(new CoreItem(14, Constants.CORE_STATUS_ENABLED, R.string.core_rain_park, R.drawable.core_rain_park, "jar []", "Flickr", R.raw.core_rain_park, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 20)));
            arrCoreItem.add(new CoreItem(15, Constants.CORE_STATUS_ENABLED, R.string.core_rain_skylight, R.drawable.core_rain_skylight, "Hide", "Flickr", R.raw.core_rain_skylight, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 21)));
            arrCoreItem.add(new CoreItem(16, Constants.CORE_STATUS_ENABLED, R.string.core_rain_street, R.drawable.core_rain_street, "Terry Madeley", "Flickr", R.raw.core_rain_street, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 22)));
            arrCoreItem.add(new CoreItem(17, Constants.CORE_STATUS_ENABLED, R.string.core_rain_tin_roof, R.drawable.core_rain_tin_roof, "Ruth Hartnup", "Flickr", R.raw.core_rain_tin_roof, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 23)));
            arrCoreItem.add(new CoreItem(18, Constants.CORE_STATUS_ENABLED, R.string.core_rain_umbrella, R.drawable.core_rain_umbrella, "craig Cloutier", "Flickr", R.raw.core_rain_umbrella, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 24)));
            arrCoreItem.add(new CoreItem(19, Constants.CORE_STATUS_ENABLED, R.string.core_rain_under_tree, R.drawable.core_rain_under_tree, "Martin Gräter", "Flickr", R.raw.core_rain_under_tree, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 25)));
            arrCoreItem.add(new CoreItem(20, Constants.CORE_STATUS_ENABLED, R.string.core_rain_window, R.drawable.core_rain_window, "Benjamin Balázs", "Flickr", R.raw.core_rain_window, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 26)));
            arrCoreItem.add(new CoreItem(21, Constants.CORE_STATUS_ENABLED, R.string.core_rainy_night_crickets, R.drawable.core_rainy_night_crickets, "Vikramdeep Sidhu", "Flickr", R.raw.core_rainy_night_crickets, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 27)));
            arrCoreItem.add(new CoreItem(22, Constants.CORE_STATUS_ENABLED, R.string.core_strong_thunderstorm, R.drawable.core_strong_thunderstorm, "Nicholas LabyrinthX", "Flickr", R.raw.core_strong_thunderstorm, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 28)));
            arrCoreItem.add(new CoreItem(23, Constants.CORE_STATUS_ENABLED, R.string.core_tent_rain, R.drawable.core_tent_rain, "Stefan", "Flickr", R.raw.core_tent_rain, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 29)));
            arrCoreItem.add(new CoreItem(24, Constants.CORE_STATUS_ENABLED, R.string.core_thunderstorm_countryside, R.drawable.core_thunderstorm_countryside, "Adam Świątkowski", "Flickr", R.raw.core_thunderstorm_countryside, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 32)));
            arrCoreItem.add(new CoreItem(25, Constants.CORE_STATUS_ENABLED, R.string.core_thunderstorm, R.drawable.core_thunderstorm, "Ron Reiring", "Flickr", R.raw.core_thunderstorm, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 33)));
            arrCoreItem.add(new CoreItem(26, Constants.CORE_STATUS_ENABLED, R.string.core_tropical_storm, R.drawable.core_tropical_storm, "jimd2007", "Flickr", R.raw.core_tropical_storm, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 34)));
            arrCoreItem.add(new CoreItem(27, Constants.CORE_STATUS_ENABLED, R.string.core_wind_rain, R.drawable.core_wind_rain, "brx0", "Flickr", R.raw.core_wind_rain, "pioggia", "purple-planet.com", String.valueOf(lngCurrentDate + 35)));
        }
        return arrCoreItem;
    }

    public void setCoreItem(CoreItem item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBInfo.COLUMN_CORE_LIST_POSITION, item.getCorePosition());
        contentValues.put(DBInfo.COLUMN_CORE_LIST_STATUS, item.getCoreStatus());
        contentValues.put(DBInfo.COLUMN_CORE_LIST_PHOTO, item.getCorePhoto());
        contentValues.put(DBInfo.COLUMN_CORE_LIST_PHOTOGRAPHER, item.getCorePhotographer());
        contentValues.put(DBInfo.COLUMN_CORE_LIST_MUSIC, item.getCoreMusic());
        contentValues.put(DBInfo.COLUMN_CORE_LIST_MUSICIAN, item.getCoreMusician());
        contentValues.put(DBInfo.COLUMN_CORE_LIST_REGDATE, item.getCoreRegDate());
        mDb.insert(DBInfo.DB_TABLE_CORE_LIST, null, contentValues);
    }

    public void deleteAllCoreItem() {
        mDb.delete(DBInfo.DB_TABLE_CORE_LIST, null, null);
    }

    public void updateCoreItem(String regdate, String status) {
        boolean isExsit = false;
        // 아이템이 있는지 검사!!
        Cursor cursor = mDb.query(DBInfo.DB_TABLE_CORE_LIST, null, DBInfo.COLUMN_CORE_LIST_REGDATE + "=?", new String[]{regdate}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) isExsit = true;
            cursor.close();
        }

        if (isExsit) {
            ContentValues val = new ContentValues();
            val.put(DBInfo.COLUMN_CORE_LIST_STATUS, status);
            mDb.update(DBInfo.DB_TABLE_CORE_LIST, val, DBInfo.COLUMN_CORE_LIST_REGDATE + "=?", new String[]{regdate});
        }
    }
}
