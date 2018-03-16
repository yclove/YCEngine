package com.ycengine.tourist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ycengine.tourist.model.CodeItem;
import com.ycengine.yclibrary.Constants;
import com.ycengine.yclibrary.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Databases {
    private final static String DB_NAME = "Tourist";
    private final static int DB_VERSION = 19;
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
                db.execSQL(CREATE_TABLE_FILTER);
                db.execSQL(CREATE_TABLE_CODE);
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
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_FILTER);
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_CODE);
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_FILTER);
                db.execSQL(CREATE_TABLE_CODE);
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

    private final static String CREATE_TABLE_FILTER = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLE_FILTER +
            "("+ DBInfo.FILTER_TYPE + " varchar(255), "
            + DBInfo.FILTER_AREA_CODE + " varchar(255), "
            + DBInfo.FILTER_SIGUNGU_CODE + " varchar(255), "
            + DBInfo.FILTER_CATEGORY_1 + " varchar(255), "
            + DBInfo.FILTER_CATEGORY_2 + " varchar(255), "
            + DBInfo.FILTER_CATEGORY_3 + " varchar(255), "
            + DBInfo.FILTER_DATE + " varchar(255)" +" )";

    private final static String CREATE_TABLE_CODE = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLE_CODE +
            "(" + DBInfo.TYPE + " NVARCHAR(100), "
            + DBInfo.CODE + " NVARCHAR(100), "
            + DBInfo.NAME + " NVARCHAR(100), "
            + DBInfo.RNUM + " INTEGER NOT NULL, "
            + "PRIMARY KEY (" + DBInfo.TYPE + "," + DBInfo.CODE + "))";

    public List<CodeItem> getCodeList(String _type) {
        List<CodeItem> areaCodeList = new ArrayList<>();

        Cursor cursor = mDb.query(DBInfo.TABLE_CODE, null, DBInfo.TYPE + " = ?", new String[]{_type}, null, null, DBInfo.RNUM + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CodeItem item = new CodeItem();
                String type = cursor.getString(cursor.getColumnIndex(DBInfo.TYPE));
                String code = cursor.getString(cursor.getColumnIndex(DBInfo.CODE));
                String name = cursor.getString(cursor.getColumnIndex(DBInfo.NAME));
                int rnum = cursor.getInt(cursor.getColumnIndex(DBInfo.RNUM));

                item.setType(type);
                item.setCode(code);
                item.setName(name);
                item.setRnum(rnum);
                areaCodeList.add(item);
            }
            cursor.close();

            if (com.ycengine.yclibrary.Constants.DEBUG_DATABASE) {
                cursor = mDb.rawQuery("SELECT * FROM " + DBInfo.TABLE_CODE + " WHERE " + DBInfo.TYPE + " = '" + _type + "' ORDER BY " + DBInfo.RNUM + " ASC", null);
                String tableString = String.format("TABLE : %s", DBInfo.TABLE_CODE);
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

        return areaCodeList;
    }

    public void setCodeList(List<CodeItem> items) {
        for (CodeItem item : items) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBInfo.TYPE, item.getType());
            contentValues.put(DBInfo.CODE, item.getCode());
            contentValues.put(DBInfo.NAME, item.getName());
            contentValues.put(DBInfo.RNUM, item.getRnum());
            mDb.insert(DBInfo.TABLE_CODE, null, contentValues);
        }
    }

    public void delAreaCodeList() {
        mDb.delete(DBInfo.TABLE_CODE, null, null);
    }

    public HashMap<String, String> getFilterData(String sortType) {
        HashMap<String, String> result =  new HashMap<>();

        Cursor cursor =  mDb.query(DBInfo.TABLE_FILTER, null, DBInfo.FILTER_TYPE + " = ?", new String[]{sortType}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()) {
                    String areaCode = cursor.getString(cursor.getColumnIndex(DBInfo.FILTER_AREA_CODE));
                    String sigunguCode = cursor.getString(cursor.getColumnIndex(DBInfo.FILTER_SIGUNGU_CODE));
                    String category1 = cursor.getString(cursor.getColumnIndex(DBInfo.FILTER_CATEGORY_1));
                    String category2 = cursor.getString(cursor.getColumnIndex(DBInfo.FILTER_CATEGORY_2));
                    String category3 = cursor.getString(cursor.getColumnIndex(DBInfo.FILTER_CATEGORY_3));

                    if (areaCode == null) areaCode = "";
                    if (sigunguCode == null) sigunguCode = "";
                    if (category1 == null) category1 = "";
                    if (category2 == null) category2 = "";
                    if (category3 == null) category3 = "";

                    result.put(com.ycengine.tourist.Constants.FILTER_AREA_CODE, areaCode);
                    result.put(com.ycengine.tourist.Constants.FILTER_SIGUNGU_CODE, sigunguCode);
                    result.put(com.ycengine.tourist.Constants.FILTER_CATEGORY_1_CODE, category1);
                    result.put(com.ycengine.tourist.Constants.FILTER_CATEGORY_2_CODE, category2);
                    result.put(com.ycengine.tourist.Constants.FILTER_CATEGORY_3_CODE, category3);
                }
            } else {
                result.put(com.ycengine.tourist.Constants.FILTER_AREA_CODE, "");
                result.put(com.ycengine.tourist.Constants.FILTER_SIGUNGU_CODE, "");
                result.put(com.ycengine.tourist.Constants.FILTER_CATEGORY_1_CODE, "");
                result.put(com.ycengine.tourist.Constants.FILTER_CATEGORY_2_CODE, "");
                result.put(com.ycengine.tourist.Constants.FILTER_CATEGORY_3_CODE, "");
            }
            cursor.close();
        }

        return result;
    }

    public void setFilterData(String sortType, HashMap<String, String> sortData) {
        boolean isExsit = false;

        Cursor cursor =  mDb.query(DBInfo.TABLE_FILTER, null, DBInfo.FILTER_TYPE + " = ?", new String[]{sortType}, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0)
                isExsit = true;
            cursor.close();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        String sortDate = simpleDateFormat.format(new Date(now));

        if (isExsit) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBInfo.FILTER_AREA_CODE, sortData.get(com.ycengine.tourist.Constants.FILTER_AREA_CODE));
            contentValues.put(DBInfo.FILTER_SIGUNGU_CODE, sortData.get(com.ycengine.tourist.Constants.FILTER_SIGUNGU_CODE));
            contentValues.put(DBInfo.FILTER_CATEGORY_1, sortData.get(com.ycengine.tourist.Constants.FILTER_CATEGORY_1_CODE));
            contentValues.put(DBInfo.FILTER_CATEGORY_2, sortData.get(com.ycengine.tourist.Constants.FILTER_CATEGORY_2_CODE));
            contentValues.put(DBInfo.FILTER_CATEGORY_3, sortData.get(com.ycengine.tourist.Constants.FILTER_CATEGORY_3_CODE));
            contentValues.put(DBInfo.FILTER_DATE, sortDate);
            mDb.update(DBInfo.TABLE_FILTER, contentValues, DBInfo.FILTER_TYPE + " = ?", new String[]{sortType});
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBInfo.FILTER_TYPE, sortType);
            contentValues.put(DBInfo.FILTER_AREA_CODE, sortData.get(com.ycengine.tourist.Constants.FILTER_AREA_CODE));
            contentValues.put(DBInfo.FILTER_SIGUNGU_CODE, sortData.get(com.ycengine.tourist.Constants.FILTER_SIGUNGU_CODE));
            contentValues.put(DBInfo.FILTER_CATEGORY_1, sortData.get(com.ycengine.tourist.Constants.FILTER_CATEGORY_1_CODE));
            contentValues.put(DBInfo.FILTER_CATEGORY_2, sortData.get(com.ycengine.tourist.Constants.FILTER_CATEGORY_2_CODE));
            contentValues.put(DBInfo.FILTER_CATEGORY_3, sortData.get(com.ycengine.tourist.Constants.FILTER_CATEGORY_3_CODE));
            contentValues.put(DBInfo.FILTER_DATE, sortDate);
            mDb.insert(DBInfo.TABLE_FILTER, null, contentValues);
        }

        if (Constants.DEBUG_DATABASE) {
            cursor = mDb.rawQuery("SELECT * FROM " + DBInfo.TABLE_FILTER + " ORDER BY " + DBInfo.FILTER_DATE + " DESC", null);
            String tableString = String.format("TABLE : %s", DBInfo.TABLE_FILTER);
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

}
