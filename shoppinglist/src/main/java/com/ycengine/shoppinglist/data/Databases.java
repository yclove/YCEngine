package com.ycengine.shoppinglist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ycengine.shoppinglist.Constants;
import com.ycengine.yclibrary.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Databases {
    private final static String DB_NAME = "ShoppingList";
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
                db.execSQL(CREATE_TABLE_GOODS);
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
            db.execSQL("DROP TABLE IF EXISTS " + DBInfo.TABLE_GOODS);
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_GOODS);
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

    private final static String CREATE_TABLE_GOODS = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLE_GOODS +
            "("+ DBInfo.GOODS_NAME + " NVARCHAR(" + Constants.DB_GOODS_MAX_LENGTH + "), "
            + DBInfo.GOODS_REG_DATE + " NVARCHAR(17), "
            + DBInfo.GOODS_EVENT_TYPE + " CHARACTER(1), "
            + DBInfo.GOODS_EVENT_DATE + " NVARCHAR(17)" +" )";

    public ArrayList<GoodsItem> getGoodsItemList(){
        ArrayList<GoodsItem> arrGoodsItems =  new ArrayList<GoodsItem>();

        //Cursor cursor =  mDb.query(DBInfo.TABLE_GOODS, null, null, null, null, null, DBInfo.GOODS_REG_DATE + " ASC");
        Cursor cursor =  mDb.query(DBInfo.TABLE_GOODS, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                GoodsItem item = new GoodsItem();
                String name = cursor.getString(cursor.getColumnIndex(DBInfo.GOODS_NAME));
                String regDate = cursor.getString(cursor.getColumnIndex(DBInfo.GOODS_REG_DATE));
                String eventType = cursor.getString(cursor.getColumnIndex(DBInfo.GOODS_EVENT_TYPE));
                String eventDate = cursor.getString(cursor.getColumnIndex(DBInfo.GOODS_EVENT_DATE));

                item.setGoodsName(name);
                item.setGoodsRegDate(regDate);
                item.setGoodsEventType(eventType);
                item.setGoodsEventDate(eventDate);
                arrGoodsItems.add(item);
            }
            cursor.close();
        }

        return arrGoodsItems;
    }

    public void setGoodsItemList(GoodsItem item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBInfo.GOODS_NAME, item.getGoodsName());
        contentValues.put(DBInfo.GOODS_REG_DATE, item.getGoodsRegDate());
        contentValues.put(DBInfo.GOODS_EVENT_TYPE, item.getGoodsEventType());
        contentValues.put(DBInfo.GOODS_EVENT_DATE, item.getGoodsEventDate());
        mDb.insert(DBInfo.TABLE_GOODS, null, contentValues);
    }

    public void setGoodsItemList(ArrayList<GoodsItem> items) {
        for (GoodsItem item : items) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBInfo.GOODS_NAME, item.getGoodsName());
            contentValues.put(DBInfo.GOODS_REG_DATE, item.getGoodsRegDate());
            contentValues.put(DBInfo.GOODS_EVENT_TYPE, item.getGoodsEventType());
            contentValues.put(DBInfo.GOODS_EVENT_DATE, item.getGoodsEventDate());
            mDb.insert(DBInfo.TABLE_GOODS, null, contentValues);
        }
    }

    public void deleteGoodsItem(String goodsRegDate){
        mDb.delete(DBInfo.TABLE_GOODS, DBInfo.GOODS_REG_DATE + " = '" + goodsRegDate + "'", null);
    }

    public void deleteGoodsItemList() {
        mDb.delete(DBInfo.TABLE_GOODS, null, null);
    }

    public void updateGoodsItem(String goodsRegDate, String updateEventType){
        boolean isExsit = false;
        // 아이템이 있는지 검사!!
        Cursor cursor =  mDb.query(DBInfo.TABLE_GOODS, null, DBInfo.GOODS_REG_DATE + "=?", new String[]{goodsRegDate}, null, null, null);
        if(cursor != null){
            if(cursor.getCount() > 0)isExsit = true;
            cursor.close();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US);
        String date = sdf.format(new Date());

        if(isExsit){
            ContentValues val = new ContentValues();
            val.put(DBInfo.GOODS_EVENT_TYPE, updateEventType);
            val.put(DBInfo.GOODS_EVENT_DATE, date);
            mDb.update(DBInfo.TABLE_GOODS, val, DBInfo.GOODS_REG_DATE+"=?", new String[]{goodsRegDate});
        }
    }

    public void updateGoodsItemName(String goodsRegDate, String reName){
        boolean isExsit = false;
        // 아이템이 있는지 검사!!
        Cursor cursor =  mDb.query(DBInfo.TABLE_GOODS, null, DBInfo.GOODS_REG_DATE + "=?", new String[]{goodsRegDate}, null, null, null);
        if(cursor != null){
            if(cursor.getCount() > 0)isExsit = true;
            cursor.close();
        }

        if(isExsit){
            ContentValues val = new ContentValues();
            val.put(DBInfo.GOODS_NAME, reName);
            mDb.update(DBInfo.TABLE_GOODS, val, DBInfo.GOODS_REG_DATE+"=?", new String[]{goodsRegDate});
        }
    }
}
