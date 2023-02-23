package edu.sjsu.android.project4srinivasraochavan;

import static edu.sjsu.android.project4srinivasraochavan.LocationsProvider.CONTENT_URI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.sjsu.android.project4srinivasraochavan.databinding.ActivityMapsBinding;

public class    MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private final LatLng LOCATION_UNIV = new LatLng(37.335371, -121.881050);
    private final LatLng LOCATION_CS = new LatLng(37.333714, -121.881860);
    private double lat, lon;
    float z;
    int mt;
    private static SharedPreferences preferences;
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String ZOOM = "zoom";
    private static final String MAPTYPE = "maptype";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.location.setOnClickListener(view -> new GPSTracker(this).getLocation());
        binding.uninstall.setOnClickListener(view -> {
            Intent delete = new Intent(Intent.ACTION_DELETE,
                    Uri.parse("package:" + getPackageName()));
            startActivity(delete);
        });
        binding.city.setOnClickListener(this::switchView);
        binding.CS.setOnClickListener(this::switchView);
        binding.university.setOnClickListener(this::switchView);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        preferences = getPreferences(MODE_PRIVATE);
        lat = preferences.getFloat(LATITUDE, 0.0f);
        lon = preferences.getFloat(LONGITUDE, 0.0f);
        z = preferences.getFloat(ZOOM, 0.0f);
        mt = preferences.getInt(MAPTYPE, 2);
        LoaderManager.getInstance(this).restartLoader(0, null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(LATITUDE, (float) mMap.getCameraPosition().target.latitude);
        editor.putFloat(LONGITUDE, (float) mMap.getCameraPosition().target.longitude);
        editor.putFloat(ZOOM, mMap.getCameraPosition().zoom);
        editor.putInt(MAPTYPE, mMap.getMapType());

        editor.apply();
    }

    public void switchView(View view) {
        CameraUpdate update = null;
        if (view.getId() == R.id.city) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            System.out.println("SATELLITE : " + mMap.getMapType());
            update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 10f);
        } else if (view.getId() == R.id.university) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            System.out.println("NORMAL : " + mMap.getMapType());
            update = CameraUpdateFactory.newLatLngZoom(LOCATION_UNIV, 14f);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            System.out.println("TERRAIN : " + mMap.getMapType());
            update = CameraUpdateFactory.newLatLngZoom(LOCATION_CS, 18f);
        }
        mMap.animateCamera(update);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if(mt == 2)
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else if(mt == 1)
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),z));
        mMap.setOnMapClickListener(this::insert);
        mMap.setOnMapLongClickListener(latLng -> delete());

    }

    private void insert(LatLng point) {
        mMap.addMarker(new MarkerOptions().position(point));
        ContentValues values = new ContentValues();
        values.put(LocationsDB.LAT, point.latitude);
        values.put(LocationsDB.LONG, point.longitude);
        values.put(LocationsDB.ZOOM, mMap.getCameraPosition().zoom);
        new MyTask().execute(values);
    }

    private void delete() {
        mMap.clear();
        new MyTask().execute();
        Toast.makeText(this, "All Markers deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this, LocationsProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        double lat = 0.0, lng = 0.0;
        float zoom = 0.0f;
        if(cursor != null && cursor.moveToFirst()) {
            do {
                int latIndex = cursor.getColumnIndex(LocationsDB.LAT);
                int lngIndex = cursor.getColumnIndex(LocationsDB.LONG);
                int zoomIndex = cursor.getColumnIndex(LocationsDB.ZOOM);
                lat = cursor.getDouble(latIndex);
                lng = cursor.getDouble(lngIndex);
                zoom = cursor.getFloat(zoomIndex);
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
            }while (cursor.moveToNext());

        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lng), zoom));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }


    class MyTask extends AsyncTask<ContentValues, Void, Void> {

        @Override
        protected Void doInBackground(ContentValues... values) {
            if (values != null && values.length > 0) {
                getContentResolver().insert(CONTENT_URI, values[0]);
            } else {
                getContentResolver().delete(CONTENT_URI, null, null);
            }
            return null;
        }
    }

}