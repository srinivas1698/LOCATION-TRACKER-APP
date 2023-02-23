package edu.sjsu.android.project4srinivasraochavan;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class LocationsProvider extends ContentProvider {
    private LocationsDB database;
    protected static final Uri CONTENT_URI = Uri.parse("content://edu.sjsu.android.project4srinivasraochavan");

    @Override
    public boolean onCreate() {
        database = new LocationsDB(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID=database.insert(values);
        if(rowID == -1)
            throw new SQLException("Failed to add a record into "+uri);
        Uri inserted= ContentUris.withAppendedId(uri,rowID);
        getContext().getContentResolver().notifyChange(inserted,null);
        return inserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        return database.deleteAll();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return database.getAllLocations();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}