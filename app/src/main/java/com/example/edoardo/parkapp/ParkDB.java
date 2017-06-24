package com.example.edoardo.parkapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Niko on 20/06/2017.
 */

public class ParkDB {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public static final String DB_NAME="history.db";
    public static final int DB_VERSION =1;

    public static final String HISTORY_TABLE = "history";

    public static final String HISTORY_ID= "_id";
    public static final int HISTORY_ID_COL= 0;

    public static final String HISTORY_INDIRIZZO = "indirizzo";
    public static final int HISTORY_INDIRIZZO_COL = 1;

    public static final String HISTORY_PARKTYPE= "park_type";
    public static final int HISTORY_PARKTYPE_COL= 2;

    public static final String HISTORY_DATE=  "date";
    public static final int HISTORY_DATE_COL= 3;

    public static final String HISTORY_ORAINIZIO= "ora_inizio";
    public static final int HISTORY_ORAINIZIO_COL= 4;

    public static final String HISTORY_ORAFINE= "ora_fine";
    public static final int HISTORY_ORAFINE_COL= 5;



    //CREATE AND DROP TABLE STATEMENTS

    public static final String CREATE_HISTORY_TABLE =
            "CREATE TABLE " + HISTORY_TABLE + " (" +
                    HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    HISTORY_INDIRIZZO + " STRING, " +
                    HISTORY_PARKTYPE + " STRING, " +
                    HISTORY_DATE + " STRING, " +
                    HISTORY_ORAINIZIO + " STRING, " +
                    HISTORY_ORAFINE + " STRING);";
    public static final String DROP_HISTORY_TABLE = "DROP TABLE IF EXISTS " + HISTORY_TABLE;

    private static class DBHelper extends SQLiteOpenHelper{

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context,name,factory,version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_HISTORY_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("History list", "Upgrading db from version " + oldVersion + " to " + newVersion);

            db.execSQL(ParkDB.DROP_HISTORY_TABLE);
            onCreate(db);
        }

    }
    public ParkDB(Context context){
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    private void openReadableDB(){
        db=dbHelper.getReadableDatabase();
    }
    private void openWriteable(){
        db=dbHelper.getWritableDatabase();
    }
    private void closeDB(){
        if(db!=null)
            db.close();
    }
    public ArrayList<Park> getParks(){
        this.openReadableDB();
        //where
        //whereargs
        Cursor cursor = db.query(HISTORY_TABLE,null,null,null,null,null,null);
        ArrayList<Park> parks = new ArrayList<Park>();
        while (cursor.moveToNext()){
            parks.add(getParkFromCursor(cursor));
        }
        if(cursor!=null)
            cursor.close();
        this.closeDB();
        return parks;
    }
    private static Park getParkFromCursor(Cursor cursor){
        if(cursor == null || cursor.getCount() == 0){
            return null;
        }
        else {
            try {
                Park park = new Park(
                        cursor.getString(HISTORY_INDIRIZZO_COL),
                        cursor.getInt(HISTORY_ID_COL),
                        cursor.getString(HISTORY_PARKTYPE_COL),
                        cursor.getString(HISTORY_DATE_COL),
                        cursor.getString(HISTORY_ORAINIZIO_COL),
                        cursor.getString(HISTORY_ORAFINE_COL));
                return park;
            }
            catch (Exception e) {
                return null;
            }
        }
    }
    public long insertPark(Park park){
        ContentValues cv = new ContentValues();
        cv.put(HISTORY_INDIRIZZO, park.getIndirizzo());
        cv.put(HISTORY_PARKTYPE, park.getPark_type());
        cv.put(HISTORY_DATE, park.getDate());
        cv.put(HISTORY_ORAINIZIO, park.getOra_inizio());
        cv.put(HISTORY_ORAFINE, park.getOra_fine());
        this.openWriteable();
        long rowID = db.insert(HISTORY_TABLE, null, cv);
        this.closeDB();
        return rowID;
    }
    public int updatePark(Park park){
        ContentValues cv = new ContentValues();
        cv.put(HISTORY_ID, park.getPark_id());
        cv.put(HISTORY_INDIRIZZO, park.getIndirizzo());
        cv.put(HISTORY_PARKTYPE, park.getPark_type());
        cv.put(HISTORY_DATE, park.getDate());
        cv.put(HISTORY_ORAINIZIO, park.getOra_inizio());
        cv.put(HISTORY_ORAFINE, park.getOra_fine());

        String where = HISTORY_ID + "= ?";
        String[] whereArgs = { String.valueOf(park.getPark_id())};

        this.openWriteable();
        int rowCount = db.update(HISTORY_TABLE, cv, where, whereArgs);
        this.closeDB();

        return rowCount;
    }
    public int deleteTask(long id){
        String where = HISTORY_ID + "= ?";
        String[] whereArgs = { String.valueOf(id)};

        this.openWriteable();
        int rowCount= db.delete(HISTORY_TABLE, where,whereArgs);
        this.closeDB();

        return rowCount;
    }
    public int deleteAll(){

        this.openWriteable();
        int rowCount= db.delete(HISTORY_TABLE, null,null);
        this.closeDB();

        return rowCount;
    }
}
