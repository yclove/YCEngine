package com.ycengine.ontimealarm.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ycengine.ontimealarm.Constants;
import com.ycengine.yclibrary.util.LogUtil;

import java.util.ArrayList;

public class Databases {
    private final static String DB_NAME = "OnTime";
    private final static int DB_VERSION = 1;
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
                db.execSQL(CREATE_TABLE_ONTIME);
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
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_ONTIME);
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_ONTIME);
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

    private final static String CREATE_TABLE_ONTIME = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLE_ONTIME +
            "(" + DBInfo.ONTIME_IDX + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DBInfo.ONTIME_ENABLED + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_NAME + " NVARCHAR(" + Constants.DB_ONTIME_NAME_MAX_LENGTH+ "), "
            + DBInfo.ONTIME_MESSAGE + " NVARCHAR(" + Constants.DB_ONTIME_MESSAGE_MAX_LENGTH+ "), "
            + DBInfo.ONTIME_TYPE + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_RINGTONE + " NVARCHAR(100), "
            + DBInfo.ONTIME_VOLUME + " INTEGER NOT NULL DEFAULT 0, "
            + DBInfo.ONTIME_ETQ_ENABLED + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_ETQ_BEGIN_HOUR + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_ETQ_BEGIN_MINUTE + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_ETQ_END_HOUR + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_ETQ_END_MINUTE + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_DAY + " CHARACTER(7), "
            + DBInfo.ONTIME_HOUR + " CHARACTER(24), "
            + DBInfo.ONTIME_STATUS_BAR + " INTEGER NOT NULL, "
            + DBInfo.ONTIME_REG_DATE + " NVARCHAR(17)" + " )";

    public ArrayList<OnTimeItem> getOnTimeItemList() {
        ArrayList<OnTimeItem> arrOnTimeItem = new ArrayList<OnTimeItem>();

        Cursor cursor = mDb.query(DBInfo.TABLE_ONTIME, null, null, null, null, null, DBInfo.ONTIME_REG_DATE+ " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OnTimeItem item = new OnTimeItem();
                int idx = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_IDX));
                int enabled = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ENABLED));
                String name = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_NAME));
                String message = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_MESSAGE));
                int type = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_TYPE));
                String ringTone = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_RINGTONE));
                int volume = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_VOLUME));
                int etqEnabled = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_ENABLED));
                int beginHour = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_BEGIN_HOUR));
                int beginMinute = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_BEGIN_MINUTE));
                int endHour = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_END_HOUR));
                int endMinute = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_END_MINUTE));
                String day = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_DAY));
                String hour = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_HOUR));
                int statusBar = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_STATUS_BAR));
                String regDate = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_REG_DATE));

                item.setOnTimeIdx(idx);
                item.setOnTimeEnabled(enabled);
                item.setOnTimeName(name);
                item.setOnTimeMessage(message);
                item.setOnTimeType(type);
                item.setOnTimeRingTone(ringTone);
                item.setOnTimeVolume(volume);
                item.setOnTimeEtqEnabled(etqEnabled);
                item.setOnTimeBeginHour(beginHour);
                item.setOnTimeBeginMinute(beginMinute);
                item.setOnTimeEndHour(endHour);
                item.setOnTimeEndMinute(endMinute);
                item.setOnTimeDay(day);
                item.setOnTimeHour(hour);
                item.setOnTimeStatusBar(statusBar);
                item.setOnTimeRegDate(regDate);
                arrOnTimeItem.add(item);
            }
            cursor.close();

            if (com.ycengine.yclibrary.Constants.DEBUG_DATABASE) {
                cursor = mDb.rawQuery("SELECT * FROM " + DBInfo.TABLE_ONTIME + " ORDER BY " + DBInfo.ONTIME_REG_DATE + " ASC", null);
                String tableString = String.format("TABLE : %s", DBInfo.TABLE_ONTIME);
                LogUtil.e(tableString);

                if (cursor .moveToFirst()) {
                    String[] columnNames = cursor.getColumnNames();
                    do {
                        String columString = "";
                        for (String name: columnNames) {
                            columString += String.format("%s : %s\n ", name, cursor.getString(cursor.getColumnIndex(name)));
                        }

                        LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                    } while (cursor.moveToNext());
                }
            }
        }

        return arrOnTimeItem;
    }

    public OnTimeItem getOnTimeItem(int idx) {
        OnTimeItem item = null;

        Cursor cursor = mDb.query(DBInfo.TABLE_ONTIME, null, DBInfo.ONTIME_IDX + "=" + idx, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                item = new OnTimeItem();
                int enabled = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ENABLED));
                String name = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_NAME));
                String message = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_MESSAGE));
                int type = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_TYPE));
                String ringTone = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_RINGTONE));
                int volume = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_VOLUME));
                int etqEnabled = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_ENABLED));
                int beginHour = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_BEGIN_HOUR));
                int beginMinute = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_BEGIN_MINUTE));
                int endHour = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_END_HOUR));
                int endMinute = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_ETQ_END_MINUTE));
                String day = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_DAY));
                String hour = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_HOUR));
                int statusBar = cursor.getInt(cursor.getColumnIndex(DBInfo.ONTIME_STATUS_BAR));
                String regDate = cursor.getString(cursor.getColumnIndex(DBInfo.ONTIME_REG_DATE));

                item.setOnTimeIdx(idx);
                item.setOnTimeEnabled(enabled);
                item.setOnTimeName(name);
                item.setOnTimeMessage(message);
                item.setOnTimeType(type);
                item.setOnTimeRingTone(ringTone);
                item.setOnTimeVolume(volume);
                item.setOnTimeEtqEnabled(etqEnabled);
                item.setOnTimeBeginHour(beginHour);
                item.setOnTimeBeginMinute(beginMinute);
                item.setOnTimeEndHour(endHour);
                item.setOnTimeEndMinute(endMinute);
                item.setOnTimeDay(day);
                item.setOnTimeHour(hour);
                item.setOnTimeStatusBar(statusBar);
                item.setOnTimeRegDate(regDate);
            }
            cursor.close();
        }

        return item;
    }

    public void setOnTimeItemList(OnTimeItem item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBInfo.ONTIME_ENABLED, item.getOnTimeEnabled());
        contentValues.put(DBInfo.ONTIME_NAME, item.getOnTimeName());
        contentValues.put(DBInfo.ONTIME_MESSAGE, item.getOnTimeMessage());
        contentValues.put(DBInfo.ONTIME_TYPE, item.getOnTimeType());
        contentValues.put(DBInfo.ONTIME_RINGTONE, item.getOnTimeRingTone());
        contentValues.put(DBInfo.ONTIME_VOLUME, item.getOnTimeVolume());
        contentValues.put(DBInfo.ONTIME_ETQ_ENABLED, item.getOnTimeEtqEnabled());
        contentValues.put(DBInfo.ONTIME_ETQ_BEGIN_HOUR, item.getOnTimeBeginHour());
        contentValues.put(DBInfo.ONTIME_ETQ_BEGIN_MINUTE, item.getOnTimeBeginMinute());
        contentValues.put(DBInfo.ONTIME_ETQ_END_HOUR, item.getOnTimeEndHour());
        contentValues.put(DBInfo.ONTIME_ETQ_END_MINUTE, item.getOnTimeEndMinute());
        contentValues.put(DBInfo.ONTIME_DAY, item.getOnTimeDay());
        contentValues.put(DBInfo.ONTIME_HOUR, item.getOnTimeHour());
        contentValues.put(DBInfo.ONTIME_STATUS_BAR, item.getOnTimeStatusBar());
        contentValues.put(DBInfo.ONTIME_REG_DATE, item.getOnTimeRegDate());
        mDb.insert(DBInfo.TABLE_ONTIME, null, contentValues);
    }

    public void deleteOnTimeItem(int idx) {
        mDb.delete(DBInfo.TABLE_ONTIME, DBInfo.ONTIME_IDX + " = " + idx, null);
    }

    public void deleteOnTimeItemList() {
        mDb.delete(DBInfo.TABLE_ONTIME, null, null);
    }

    public void updateOnTimeItem(OnTimeItem item) {
        boolean isExsit = false;
        // 아이템이 있는지 검사!!
        Cursor cursor = mDb.query(DBInfo.TABLE_ONTIME, null, DBInfo.ONTIME_IDX + "=" + item.getOnTimeIdx(), null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) isExsit = true;
            cursor.close();
        }

        if (isExsit) {
            ContentValues val = new ContentValues();
            val.put(DBInfo.ONTIME_ENABLED, item.getOnTimeEnabled());
            val.put(DBInfo.ONTIME_NAME, item.getOnTimeName());
            val.put(DBInfo.ONTIME_MESSAGE, item.getOnTimeMessage());
            val.put(DBInfo.ONTIME_TYPE, item.getOnTimeType());
            val.put(DBInfo.ONTIME_RINGTONE, item.getOnTimeRingTone());
            val.put(DBInfo.ONTIME_VOLUME, item.getOnTimeVolume());
            val.put(DBInfo.ONTIME_ETQ_ENABLED, item.getOnTimeEtqEnabled());
            val.put(DBInfo.ONTIME_ETQ_BEGIN_HOUR, item.getOnTimeBeginHour());
            val.put(DBInfo.ONTIME_ETQ_BEGIN_MINUTE, item.getOnTimeBeginMinute());
            val.put(DBInfo.ONTIME_ETQ_END_HOUR, item.getOnTimeEndHour());
            val.put(DBInfo.ONTIME_ETQ_END_MINUTE, item.getOnTimeEndMinute());
            val.put(DBInfo.ONTIME_DAY, item.getOnTimeDay());
            val.put(DBInfo.ONTIME_HOUR, item.getOnTimeHour());
            val.put(DBInfo.ONTIME_STATUS_BAR, item.getOnTimeStatusBar());
            val.put(DBInfo.ONTIME_REG_DATE, item.getOnTimeRegDate());
            mDb.update(DBInfo.TABLE_ONTIME, val, DBInfo.ONTIME_IDX + "=" + item.getOnTimeIdx(), null);
        }
    }
}
