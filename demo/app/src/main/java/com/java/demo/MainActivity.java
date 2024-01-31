package com.java.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private GoogleMap myMap;
    private Toolbar toolbar;
    private Marker marker;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private static final int REQUEST_CODE_PERMISSION = 100;
    private SearchView searchView;
    private ImageView btnGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.my_search_view);

        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        btnGPS = findViewById(R.id.my_gps);
        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        getCurrentLocation();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.my_map);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        Log.e(TAG, "onQueryTextSubmit: Location: " + address);
                        if (marker != null) {
                            marker.remove();
                        }
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                            marker = myMap.addMarker(new MarkerOptions().position(latLng).title(location));

                        if (address.getAdminArea() == null && address.getSubAdminArea() == null) {
                            animateCamera(latLng, 5, address.getAddressLine(0));
                        } else {
                            animateCamera(latLng, 12, address.getAddressLine(0));
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onQueryTextSubmit: Location not found");
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.e(TAG, "onMapReady: Map is ready");
        myMap = googleMap;
        myMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.map_none) {
            myMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        } else if (item.getItemId() == R.id.map_normal) {
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        } else if (item.getItemId() == R.id.map_satellite) {
            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        } else if (item.getItemId() == R.id.map_terrain) {
            myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        } else if (item.getItemId() == R.id.map_hybrid) {
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        return super.onOptionsItemSelected(item);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE_PERMISSION);
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task<Location> location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.e(TAG, "onComplete: Found location!");
                    currentLocation = (Location) task.getResult();

                    animateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15, "My Location");
                    Log.e(TAG, "onComplete: Current location (Latitude: " + currentLocation.getLatitude() + ", Longitude: " + currentLocation.getLongitude() + ")");
                } else {
                    Log.e(TAG, "onComplete: Current location is null");
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getCurrentLocation: " + e.getMessage());
        }
    }

    private void animateCamera(LatLng latLng, float zoom, String title) {
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equals("My Location")) {
            marker = myMap.addMarker(new MarkerOptions().position(latLng).title(title));
        } else {
            if (marker != null) {
                marker.remove();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}