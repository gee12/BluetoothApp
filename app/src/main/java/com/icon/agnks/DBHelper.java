package com.icon.agnks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 06.10.2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "agnks.db";
    private static final int DB_VERSION = 1;
    private static final String DB_DEVICES_TABLE = "devices";

    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_DEVICE_NAME = "device_name";
    public static final String COLUMN_CUSTOM_NAME = "custom_name";
    public static final String COLUMN_MAC_ADDRESS = "mac_address";
//    public static final String COLUMN_STATE = "state";

    private static final String DB_CREATE_SCRIPT = "CREATE TABLE "
            + DB_DEVICES_TABLE + " (" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DEVICE_NAME + " text not null, "
            + COLUMN_CUSTOM_NAME + " text, "
            + COLUMN_MAC_ADDRESS + " text not null);";
//            + COLUMN_STATE + " integer);";
    private static final String DB_DROP_SCRIPT = "DROP TABLE IF EXISTS " + DB_DEVICES_TABLE;

    public static List<Device> DbDevices = new ArrayList<>();

    public static void init(Context context) {
        DBHelper db = new DBHelper(context);
        DbDevices = db.getAllToList();
    }

    /**
     *
     * @param context
     */
    public DBHelper(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
    }

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.add("Create/open database [" + DB_NAME + "] from path: [" + db.getPath() + "]", Log.DEBUG);
        db.execSQL(DB_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.add("Update database [" + DB_NAME + "] from version " + oldVersion + " to " + newVersion, Log.DEBUG);
        db.execSQL(DB_DROP_SCRIPT);
        onCreate(db);
    }

    public static Device toDevice(Cursor cursor) {
        Device res = new Device();
        res.Id = cursor.getInt(0);
        res.DeviceName = cursor.getString(1);
        res.CustomName = cursor.getString(2);
        res.MacAddress = cursor.getString(3);
        res.IsSaved = true;
        //...

        return res;
    }

    public List<Device> getAllToList() {
        List<Device> res = new ArrayList<>();
        Cursor cursor = getAllToCursor();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Device device = toDevice(cursor);
            res.add(device);
            cursor.moveToNext();
        }
        cursor.close();
        return res;
    }

    public Cursor getAllToCursor() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(DB_DEVICES_TABLE, new String[] {
                COLUMN_ID, COLUMN_DEVICE_NAME, COLUMN_CUSTOM_NAME, COLUMN_MAC_ADDRESS},
                null, null, null, null, null);
    }

    public long insert(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_NAME, device.DeviceName);
        values.put(COLUMN_CUSTOM_NAME, device.CustomName);
        values.put(COLUMN_MAC_ADDRESS, device.MacAddress);
        long res = db.insert(DB_DEVICES_TABLE, null, values);
        db.close();
        Logger.add("Insert device [" + device.DeviceName + "] into database [" + DB_NAME + "]. Result: " + res, Log.DEBUG);
        return res;
    }

    public int update(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_NAME, device.DeviceName);
        values.put(COLUMN_CUSTOM_NAME, device.CustomName);
        values.put(COLUMN_MAC_ADDRESS, device.MacAddress);
        int res = db.update(DB_DEVICES_TABLE, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(device.Id)});
        db.close();
        Logger.add("Update device [" + device.DeviceName + "] into database [" + DB_NAME + "]. Result: " + res, Log.DEBUG);
        return res;
    }

    public int delete(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete(DB_DEVICES_TABLE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(device.Id)});
        db.close();
        Logger.add("Delete device [" + device.DeviceName + "] from database [" + DB_NAME + "]. Result: " + res, Log.DEBUG);
        return res;
    }

    public static DBHelper getInstance(Context context) {
        return new DBHelper(context);
    }
}
