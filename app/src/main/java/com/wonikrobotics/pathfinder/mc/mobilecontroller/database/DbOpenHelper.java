package com.wonikrobotics.pathfinder.mc.mobilecontroller.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Notebook on 2016-07-05.
 */
public class DbOpenHelper {

    private static final String DATABASE_NAME = "RobotList";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    public DbOpenHelper(Context context) {
        this.mCtx = context;
        Log.e("helper", mCtx.toString());
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDB.close();
    }

    /**
     * Return all rows
     *
     * @return
     */
    public Cursor getAllColumns() {
        return mDB.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, null);
    }

    /**
     * Insert new row to Database
     *
     * @param name
     * @param uri
     * @param master
     * @param controller
     * @param velocity
     * @param angular
     * @return
     */
    public boolean insertColumn(String name, String uri, String master, String controller, String velocity, String angular) {
        if (getIdx(uri) == -1)
            return false;
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.URI, uri);
        values.put(DataBases.CreateDB.MASTER, master);
        values.put(DataBases.CreateDB.CONTROLLER, controller);
        values.put(DataBases.CreateDB.VELOCITY, velocity);
        values.put(DataBases.CreateDB.ANGULAR, angular);
        Log.d("DataBase", "insert " + name + "," + uri);
        return mDB.insert(DataBases.CreateDB._TABLENAME, null, values) > 0;
    }

    public int getIdx(String uri) {
        Cursor c = mDB.rawQuery("SELECT " + DataBases.CreateDB.IDX + " FROM " + DataBases.CreateDB._TABLENAME
                + " WHERE " + DataBases.CreateDB.URI + "='" + uri + "';", null);
        if (c.moveToNext()) {
            return c.getInt(c.getColumnIndex(DataBases.CreateDB.IDX));
        }
        return -1;
    }

    /**
     * Delete row
     *
     * @param idx
     * @return
     */
    public boolean deleteColumn(String idx) {
        Log.d("DataBase", "delete index " + idx);
        return mDB.delete(DataBases.CreateDB._TABLENAME, "idx=" + idx, null) > 0;
    }

    /**
     * Update robot information
     *
     * @param idx
     * @param name
     * @param uri
     * @param master
     * @return
     */
    public boolean updateColumn(String idx, String name, String uri, String master) {
        Log.d("DataBase", "update the value" + name + "," + uri);
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.URI, uri);
        values.put(DataBases.CreateDB.MASTER, master);
        return mDB.update(DataBases.CreateDB._TABLENAME, values, "idx=" + idx, null) > 0;

    }

    /**
     * Update robot controller option
     *
     * @param idx
     * @param controller
     * @param velocity
     * @param angular
     * @return
     */
    public boolean updateOption(String idx, String controller, String velocity, String angular) {
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.CONTROLLER, controller);
        values.put(DataBases.CreateDB.VELOCITY, velocity);
        values.put(DataBases.CreateDB.ANGULAR, angular);
        return mDB.update(DataBases.CreateDB._TABLENAME, values, "idx=" + idx, null) > 0;

    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }
}
