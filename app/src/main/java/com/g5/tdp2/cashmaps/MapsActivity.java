package com.g5.tdp2.cashmaps;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.domain.AtmDist;
import com.g5.tdp2.cashmaps.domain.AtmNet;
import com.g5.tdp2.cashmaps.gateway.AtmGateway;
import com.g5.tdp2.cashmaps.gateway.AtmRequest;
import com.g5.tdp2.cashmaps.gateway.BankGateway;
import com.g5.tdp2.cashmaps.gateway.impl.CacheAtmGateway;
import com.g5.tdp2.cashmaps.gateway.impl.CacheBankGateway;
import com.g5.tdp2.cashmaps.gateway.impl.WebBankGateway;
import com.g5.tdp2.cashmaps.view.CustomInfoWindowAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Spinner spinnerBanks, spinnerNets, spinnerRadio;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] INTERNET_PERMS = {
            Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE
    };
    private static final int LOCATION_REQUEST = 1307;
    private static final int INTERNET_REQUEST = 1319;

    private AtmGateway atmGateway = new CacheAtmGateway();
    private AtomicReference<List<Atm>> atmsRef = new AtomicReference<>(); // contiene a los cajeros cargados
    private List<Marker> bank_markers = new ArrayList<>();

    private String filterBank = null;
    private AtmNet filterNet = null;
    private double filterRadio = AtmDist.R_500.radius;
    private Location currentLocation = null;

    private BankGateway bankGateway = new CacheBankGateway(new WebBankGateway());
    private AtomicReference<List<String>> linkBanks = new AtomicReference<>(Collections.emptyList());
    private AtomicReference<List<String>> banelcoBanks = new AtomicReference<>(Collections.emptyList());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (canAccessInternet()) {
            monitorConnection();
            loadBanks();
        } else {
            requestPermissions(INTERNET_PERMS, INTERNET_REQUEST);
        }

        if (canAccessLocation()) {
            launchMap();
        } else {
            requestPermissions(INITIAL_PERMS, LOCATION_REQUEST);
        }

        loadSpinnerBanks(null);
        loadSpinnerNet();
        loadSpinnerRadio();
        loadAtms(new AtmRequest());
    }

    /**
     * Carga los bancos disponibles por red de cajeros de forma asincronica en los campos linkBanks y banelcoBanks respectivamente
     */
    private void loadBanks() {
        new BankFetchTask(bankGateway, linkBanks::set).execute(AtmNet.LINK);
        new BankFetchTask(bankGateway, banelcoBanks::set).execute(AtmNet.BANELCO);
        new BankFetchTask(bankGateway, this::loadSpinnerBanks).execute(AtmNet.values());
    }

    private void monitorConnection() {
        registerReceiver(new ConnectionTask(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Carga los cajeros en el campo atmsRef
     *
     * @param atmRequest Filtros a aplicar. new AtmRequest() implica 'ningun filtro'
     */
    private void loadAtms(AtmRequest atmRequest) {
        new AtmFetchTask(atmGateway, this::filterAndSetAtms)
                .execute(atmRequest);
    }

    /**
     * Aplica los filtros sobre los cajeros obtenidos y los setea en {@link MapsActivity#atmsRef}
     *
     * @param atms Atms obtenidos
     */
    private void filterAndSetAtms(List<Atm> atms) {
        if (atms == null || atms.isEmpty()) return;
        clearActualMarkers();
        atmsRef.set(atms);
        List<Atm> filteredAtms = Atm.filter(atms,filterNet, filterBank, currentLocation.getLatitude(), currentLocation.getLongitude(), filterRadio);
        for (Atm atm : filteredAtms) {
            Log.d("atm-list", atm.toString());
            addAtmToMap(atm);
        }
    }

    private void clearActualMarkers() {
        for (Marker marker : bank_markers) {
            marker.remove();
        }
        bank_markers.clear();
    }

    private void addAtmToMap(Atm atm) {
        Optional.ofNullable(mMap).ifPresent(m -> {
            LatLng latLngLocation = new LatLng(atm.getLat(), atm.getLon());
            Marker newMarker = m.addMarker(new MarkerOptions().position(latLngLocation).title(atm.getBank() + "&" + atm.getAddress() + "&" + atm.getNet() + "&Terminales: " + atm.getTerms()));
            bank_markers.add(newMarker);
        });
    }

    private void launchMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void loadSpinnerBanks(List<String> banks) {
        ArrayList<String> spinnerOptions = new ArrayList<>();
        spinnerOptions.add(0, "Cualquier banco");
        if (banks != null){
            spinnerOptions.addAll(banks);
        }

        spinnerBanks = (Spinner) findViewById(R.id.select_bank);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerOptions);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanks.setAdapter(dataAdapter);

        spinnerBanks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedFilter = adapterView.getItemAtPosition(i).toString();
                if (!selectedFilter.equals("Cualquier banco")) {
                    filterBank = adapterView.getItemAtPosition(i).toString();
                } else {
                    filterBank = null;
                }
                filterAndSetAtms(atmsRef.get());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadSpinnerNet() {
        spinnerNets = (Spinner) findViewById(R.id.select_net);
        List<String> list = new ArrayList<>();
        list.add("Cualquier red");
        list.add(AtmNet.BANELCO.toString());
        list.add(AtmNet.LINK.toString());

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNets.setAdapter(dataAdapter);
        spinnerNets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFilter = adapterView.getItemAtPosition(i).toString();
                List<String> banksByNet = new ArrayList<>();

                if (!selectedFilter.equals("Cualquier red")) {
                    filterNet = AtmNet.fromString(adapterView.getItemAtPosition(i).toString());

                    if (selectedFilter.equals(AtmNet.BANELCO.toString())) {
                        banksByNet.addAll(banelcoBanks.get());
                    } else if (selectedFilter.equals(AtmNet.LINK.toString())) {
                        banksByNet.addAll(linkBanks.get());
                    }
                } else {
                    filterNet = null;
                    banksByNet.addAll(banelcoBanks.get());
                    banksByNet.addAll(linkBanks.get());
                }
                java.util.Collections.sort(banksByNet);
                loadSpinnerBanks(banksByNet);
                filterAndSetAtms(atmsRef.get());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadSpinnerRadio() {
        spinnerRadio = (Spinner) findViewById(R.id.select_radio);
        List<String> list = new ArrayList<String>();
        list.add("" + AtmDist.R_100.radius);
        list.add("" + AtmDist.R_200.radius);
        list.add("" + AtmDist.R_500.radius);
        list.add("" + AtmDist.R_1000.radius);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRadio.setAdapter(dataAdapter);
        spinnerRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("radio-selected", adapterView.getItemAtPosition(i).toString());
                filterRadio = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                filterAndSetAtms(atmsRef.get());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerRadio.setSelection(2);
    }

    private boolean canAccessLocation() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean canAccessInternet() {
        return hasPermission(Manifest.permission.INTERNET) && hasPermission(Manifest.permission.ACCESS_NETWORK_STATE);
    }

    private boolean hasPermission(String perm) {
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm);
    }

    private void notAccessLocation() {
        Toast.makeText(this, R.string.no_access_location, Toast.LENGTH_LONG).show();
        finish();
    }

    private void notAccessInternet() {
        Toast.makeText(this, R.string.no_access_internet, Toast.LENGTH_LONG).show();
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
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (canAccessLocation()) {
                    launchMap();
                } else {
                    notAccessLocation();
                }
                break;
            case INTERNET_REQUEST:
                if (canAccessInternet()) {
                    monitorConnection();
                    loadBanks();
                } else {
                    notAccessInternet();
                }
                break;
        }
    }

    private void centerAndMarkLocation(Location location) {
        Optional.ofNullable(mMap).ifPresent(m -> {
            LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
            int height = 75;
            int width = 75;
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.curr_loc);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            m.addMarker(
                    new MarkerOptions()
                            .position(latLngLocation)
                            .title("current-location")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
            );
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLngLocation, 15);
            m.animateCamera(yourLocation);
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
        mMap.setInfoWindowAdapter(
                new CustomInfoWindowAdapter(LayoutInflater.from(this))
        );

        setUpMap();
    }

    private void setUpMap() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            Optional<Location> location = Optional.ofNullable(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

            if (location.isPresent()) {
                Log.d("location-get", "NOT NULL");
                currentLocation = location.get();
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
