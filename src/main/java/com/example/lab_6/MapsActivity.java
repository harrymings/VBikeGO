package com.example.lab_6;


import android.Manifest;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lab_6.sampledata.JSON;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONException;


public class MapsActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, GoogleMap.InfoWindowAdapter {
    private GoogleMap mMap;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


        @Override
        public void onRequestPermissionsResult ( int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults){
            locationPermissionGranted = false;
            switch (requestCode) {
                case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        locationPermissionGranted = true;
                    }
                }
            }
            updateLocationUI();

        }

        private void getLocationPermission () {
            /*
             * Request location permission, so that we can get the location of the
             * device. The result of the permission request is handled by a callback,
             * onRequestPermissionsResult.
             */
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        }
        private void updateLocationUI () {
            if (mMap == null) {
                return;
            }
            try {
                if (locationPermissionGranted) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                } else {
                    mMap.setMyLocationEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    lastKnownLocation = null;
                    getLocationPermission();
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }

        @Override
        public void onMapReady (GoogleMap map){
            mMap = map;
            updateLocationUI();
            mMap.setTrafficEnabled(true);

            // TODO: Before enabling the My Location layer, you must request
            // location permission from the user. This sample does not include
            // a request for location permission.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setBuildingsEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            UiSettings uiSettings = mMap.getUiSettings();
            uiSettings.setCompassEnabled(true);
            uiSettings.setIndoorLevelPickerEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
            LatLng latLng = new LatLng(22.302711, 114.177216);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            try {
                JSONArray obj = new JSONArray(JSON.CoOrdinates);
                for (int i = 0; i < obj.length(); i++) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(obj.getJSONObject(i).getString("Lat")), Double.parseDouble(obj.getJSONObject(i).getString("Lon"))))
                            .title(obj.getJSONObject(i).getString("Site"))
                            //.snippet((obj.getJSONObject(i).getString("District")))
                            .snippet("設施名稱: "+obj.getJSONObject(i).getString("設施名稱")+"\nTelephone: "+(obj.getJSONObject(i).getString("Telephone"))+"\nDistrict: "+(obj.getJSONObject(i).getString("District"))));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mMap.setInfoWindowAdapter(this);

        }


        @Override
        public void onMyLocationClick (@NonNull Location location){
            Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();


        }

        @Override
        public boolean onMyLocationButtonClick () {
            Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();

            return false;
        }


    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
         View view = getLayoutInflater().inflate(R.layout.custom_info_view,null);
        TextView title = view.findViewById(R.id.title);
        TextView snippet = view.findViewById(R.id.snippet);
        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());
        return view;
    }



}


