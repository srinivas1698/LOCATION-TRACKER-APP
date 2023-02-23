package edu.sjsu.android.project4srinivasraochavan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class LocationsDB extends SQLiteOpenHelper {
    protected static String DATABASE_NAME="locationsDB";
    protected static String TABLE_NAME="locations";
    protected static String ID="_id";
    protected static String LAT="latitude";
    protected static String LONG="longitude";
    protected static String ZOOM="zoom";
    public LocationsDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create=String.format("CREATE TABLE %s " +
                "(%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "%s DOUBLE NOT NULL, "+
                "%s DOUBLE NOT NULL, " +
                "%s FLOAT NOT NULL);", TABLE_NAME, ID, LAT, LONG, ZOOM);
        sqLiteDatabase.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME   );
        onCreate(sqLiteDatabase);
    }
    public long insert(ContentValues contentValues){
        SQLiteDatabase database = getWritableDatabase();
        return database.insert(TABLE_NAME, null, contentValues);
    }
    public int deleteAll()
    {
        SQLiteDatabase database=getWritableDatabase();
        return database.delete(TABLE_NAME,null,null);
    }
    public Cursor getAllLocations(){
        SQLiteDatabase database = getWritableDatabase();
        return database.query(TABLE_NAME,
                new String[]{ID, LAT, LONG, ZOOM},
                null, null, null, null,null);
    }
}
