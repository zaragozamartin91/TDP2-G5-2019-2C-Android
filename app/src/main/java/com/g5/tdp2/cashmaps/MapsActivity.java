package com.g5.tdp2.cashmaps;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Optional;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (canAccessLocation()) {
            launchMap();
        } else {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }
    }

    private void launchMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean canAccessLocation() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean hasPermission(String perm) {
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm);
    }

    private void notAccessLocation() {
        Toast.makeText(this, R.string.no_access_location, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("CHANGED", "LOCATION UPDATED");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST) {
            if (canAccessLocation()) {
                launchMap();
            } else {
                notAccessLocation();
            }
        }
    }

    private void centerAndMarkLocation(Location location) {
        Optional.ofNullable(mMap).ifPresent(m -> {
            LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
            m.addMarker(new MarkerOptions().position(latLngLocation).title("Your Location"));
            m.moveCamera(CameraUpdateFactory.newLatLng(latLngLocation));
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        setUpMap();
    }

    private void setUpMap() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            Optional<Location> location = Optional.ofNullable(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

            if (location.isPresent()) {
                Log.d("location-get", "NOT NULL");
                centerAndMarkLocation(location.get());
            } else {
                Log.d("location-get", "NULL");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        } catch (SecurityException se) {
            Log.d("location-get", "SE CAUGHT");
            se.printStackTrace();
        }
    }
}