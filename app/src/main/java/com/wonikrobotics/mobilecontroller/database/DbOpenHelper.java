package com.wonikrobotics.mobilecontroller.database;

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

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATEOPTION);
            db.execSQL(DataBases.CreateDB._CREATE);
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._OPTIONTABLE);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context) {
        this.mCtx = context;
        Log.e("helper",mCtx.toString());
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDB.close();
    }

    public Cursor getAllColumns(){
        return mDB.query(DataBases.CreateDB._TABLENAME, null, null, null, null, null, null);
    }
    public Cursor getAllColumnsFromOption(){
        return mDB.query(DataBases.CreateDB._OPTIONTABLE, null, null, null, null, null, null);
    }
    public boolean insertOption(String controller,String velocity,String angular){
        mDB.delete(DataBases.CreateDB._OPTIONTABLE, "*" , null);
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.CONTROLLER, controller);
        values.put(DataBases.CreateDB.VELOCITY, velocity);
        values.put(DataBases.CreateDB.ANGULAR, angular);
        Log.d("DataBase","insert "+controller+","+velocity+","+angular);
        return mDB.insert(DataBases.CreateDB._OPTIONTABLE, null, values)>0;
    }

    public boolean insertColumn(String name, String uri,String master){
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.URI, uri);
        values.put(DataBases.CreateDB.MASTER, master);
        Log.d("DataBase","insert "+name+","+uri);
        return mDB.insert(DataBases.CreateDB._TABLENAME, null, values)>0;
//         mDB.rawQuery("INSERT INTO "+DataBases.CreateDB._TABLENAME+" VALUES ('','"+name+"','"+uri+"');",null);
    }
    public boolean deleteColumn(String idx){
        Log.d("DataBase","delete index "+idx);
        return mDB.delete(DataBases.CreateDB._TABLENAME, "idx="+idx, null) > 0;
//        mDB.rawQuery("DELETE FROM "+DataBases.CreateDB._TABLENAME+" WHERE "+DataBases.CreateDB.IDX+"="+idx+";",null);

    }
    public boolean updateCulumn(String idx,String name,String uri){
//        mDB.rawQuery("UPDATE "+DataBases.CreateDB._TABLENAME+" SET "+DataBases.CreateDB.NAME+"="+name+", "+DataBases.CreateDB.URI+"="+uri+" WHERE "+DataBases.CreateDB.IDX+"="+idx+";",null);
        Log.d("DataBase","update the value"+name+","+uri);
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME, name);
        values.put(DataBases.CreateDB.URI, uri);
        return mDB.update(DataBases.CreateDB._TABLENAME, values, "idx="+idx, null) > 0;

    }
}
