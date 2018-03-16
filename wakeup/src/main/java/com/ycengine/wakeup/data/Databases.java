package com.ycengine.wakeup.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ycengine.wakeup.Constants;
import com.ycengine.yclibrary.util.LogUtil;

import java.util.ArrayList;

public class Databases {
    private final static String DB_NAME = "WakeUp";
    private final static int DB_VERSION = 6;
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
                db.execSQL(CREATE_TABLE_NOTIFY);
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
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_NOTIFY);
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_NOTIFY);
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

    private final static String CREATE_TABLE_NOTIFY = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLE_NOTIFY +
            "(" + DBInfo.NOTI_IDX + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DBInfo.NOTI_ENABLED + " INTEGER NOT NULL, "
            + DBInfo.NOTI_NAME + " NVARCHAR(" + Constants.DB_NOTIFY_NAME_MAX_LENGTH + "), "
            + DBInfo.NOTI_MESSAGE + " NVARCHAR(" + Constants.DB_NOTIFY_MESSAGE_MAX_LENGTH + "), "
            + DBInfo.NOTI_TYPE + " INTEGER NOT NULL, "
            + DBInfo.NOTI_RINGTONE + " NVARCHAR(100), "
            + DBInfo.NOTI_VOLUME + " INTEGER NOT NULL DEFAULT 0, "
            + DBInfo.NOTI_GAME + " INTEGER NOT NULL, "
            + DBInfo.NOTI_HOUR + " INTEGER NOT NULL, "
            + DBInfo.NOTI_MINUTE + " INTEGER NOT NULL, "
            + DBInfo.NOTI_DAY + " CHARACTER(7), "
            + DBInfo.NOTI_SPECIAL_DAY + " INTEGER NOT NULL, "
            + DBInfo.NOTI_REG_DATE + " NVARCHAR(17)" + " )";

    public ArrayList<NotifyItem> getNotifyItemList() {
        ArrayList<NotifyItem> arrNotifyItem = new ArrayList<NotifyItem>();

        Cursor cursor = mDb.query(DBInfo.TABLE_NOTIFY, null, null, null, null, null, DBInfo.NOTI_REG_DATE + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                NotifyItem item = new NotifyItem();
                int idx = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_IDX));
                int enabled = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_ENABLED));
                String name = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_NAME));
                String message = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_MESSAGE));
                int type = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_TYPE));
                String ringTone = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_RINGTONE));
                int volume = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_VOLUME));
                int game = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_GAME));
                int hour = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_HOUR));
                int minute = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_MINUTE));
                String day = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_DAY));
                int specialDay = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_SPECIAL_DAY));
                String regDate = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_REG_DATE));

                item.setNotiIdx(idx);
                item.setNotiEnabled(enabled);
                item.setNotiName(name);
                item.setNotiMessage(message);
                item.setNotiType(type);
                item.setNotiRingTone(ringTone);
                item.setNotiVolume(volume);
                item.setNotiGame(game);
                item.setNotiHour(hour);
                item.setNotiMinute(minute);
                item.setNotiDay(day);
                item.setNotiSpecialDay(specialDay);
                item.setNotiRegDate(regDate);
                arrNotifyItem.add(item);
            }
            cursor.close();

            if (com.ycengine.yclibrary.Constants.DEBUG_DATABASE) {
                cursor = mDb.rawQuery("SELECT * FROM " + DBInfo.TABLE_NOTIFY + " ORDER BY " + DBInfo.NOTI_REG_DATE + " ASC", null);
                String tableString = String.format("TABLE : %s", DBInfo.TABLE_NOTIFY);
                LogUtil.e(tableString);

                if (cursor .moveToFirst()) {
                    String[] columnNames = cursor.getColumnNames();
                    do {
                        String columString = "";
                        for (String name: columnNames) {
                            columString += String.format("%s : %s, ", name, cursor.getString(cursor.getColumnIndex(name)));
                        }

                        LogUtil.e(columString.substring(0, columString.length() - 1) + "\n");
                    } while (cursor.moveToNext());
                }
            }
        }

        return arrNotifyItem;
    }

    public NotifyItem getNotifyItem(int idx) {
        NotifyItem item = null;

        Cursor cursor = mDb.query(DBInfo.TABLE_NOTIFY, null, DBInfo.NOTI_IDX + "=" + idx, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                item = new NotifyItem();
                int enabled = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_ENABLED));
                String name = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_NAME));
                String message = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_MESSAGE));
                int type = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_TYPE));
                String ringTone = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_RINGTONE));
                int volume = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_VOLUME));
                int game = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_GAME));
                int hour = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_HOUR));
                int minute = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_MINUTE));
                String day = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_DAY));
                int specialDay = cursor.getInt(cursor.getColumnIndex(DBInfo.NOTI_SPECIAL_DAY));
                String regDate = cursor.getString(cursor.getColumnIndex(DBInfo.NOTI_REG_DATE));

                item.setNotiIdx(idx);
                item.setNotiEnabled(enabled);
                item.setNotiName(name);
                item.setNotiMessage(message);
                item.setNotiType(type);
                item.setNotiRingTone(ringTone);
                item.setNotiVolume(volume);
                item.setNotiGame(game);
                item.setNotiHour(hour);
                item.setNotiMinute(minute);
                item.setNotiDay(day);
                item.setNotiSpecialDay(specialDay);
                item.setNotiRegDate(regDate);
            }
            cursor.close();
        }

        return item;
    }

    public void setNotifyItemList(NotifyItem item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBInfo.NOTI_ENABLED, item.getNotiEnabled());
        contentValues.put(DBInfo.NOTI_NAME, item.getNotiName());
        contentValues.put(DBInfo.NOTI_MESSAGE, item.getNotiMessage());
        contentValues.put(DBInfo.NOTI_TYPE, item.getNotiType());
        contentValues.put(DBInfo.NOTI_RINGTONE, item.getNotiRingTone());
        contentValues.put(DBInfo.NOTI_VOLUME, item.getNotiVolume());
        contentValues.put(DBInfo.NOTI_GAME, item.getNotiGame());
        contentValues.put(DBInfo.NOTI_HOUR, item.getNotiHour());
        contentValues.put(DBInfo.NOTI_MINUTE, item.getNotiMinute());
        contentValues.put(DBInfo.NOTI_DAY, item.getNotiDay());
        contentValues.put(DBInfo.NOTI_SPECIAL_DAY, item.getNotiSpecialDay());
        contentValues.put(DBInfo.NOTI_REG_DATE, item.getNotiRegDate());
        mDb.insert(DBInfo.TABLE_NOTIFY, null, contentValues);
    }

    public void deleteNotifyItem(int notifyIdx) {
        mDb.delete(DBInfo.TABLE_NOTIFY, DBInfo.NOTI_IDX + " = " + notifyIdx, null);
    }

    public void deleteNotifyItemList() {
        mDb.delete(DBInfo.TABLE_NOTIFY, null, null);
    }

    public void updateNotifyItem(NotifyItem item) {
        boolean isExsit = false;
        // 아이템이 있는지 검사!!
        Cursor cursor = mDb.query(DBInfo.TABLE_NOTIFY, null, DBInfo.NOTI_IDX + "=" + item.getNotiIdx(), null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) isExsit = true;
            cursor.close();
        }

        if (isExsit) {
            ContentValues val = new ContentValues();
            val.put(DBInfo.NOTI_ENABLED, item.getNotiEnabled());
            val.put(DBInfo.NOTI_NAME, item.getNotiName());
            val.put(DBInfo.NOTI_MESSAGE, item.getNotiMessage());
            val.put(DBInfo.NOTI_TYPE, item.getNotiType());
            val.put(DBInfo.NOTI_RINGTONE, item.getNotiRingTone());
            val.put(DBInfo.NOTI_VOLUME, item.getNotiVolume());
            val.put(DBInfo.NOTI_GAME, item.getNotiGame());
            val.put(DBInfo.NOTI_HOUR, item.getNotiHour());
            val.put(DBInfo.NOTI_MINUTE, item.getNotiMinute());
            val.put(DBInfo.NOTI_DAY, item.getNotiDay());
            val.put(DBInfo.NOTI_SPECIAL_DAY, item.getNotiSpecialDay());
            val.put(DBInfo.NOTI_REG_DATE, item.getNotiRegDate());
            mDb.update(DBInfo.TABLE_NOTIFY, val, DBInfo.NOTI_IDX + "=" + item.getNotiIdx(), null);
        }
    }
}
