package com.g5.tdp2.cashmaps;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
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
import android.widget.ImageButton;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    public static final List<String> NO_BANKS = Collections.emptyList();
    public static final List<Atm> NO_ATMS = Collections.emptyList();

    private GoogleMap mMap;
    private Spinner spinnerBanks, spinnerNets, spinnerRadio;
    private ImageButton searchButton;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] INTERNET_PERMS = {
            Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE
    };
    private static final int LOCATION_REQUEST = 1307;
    private static final int INTERNET_REQUEST = 1319;

    private AtmGateway atmGateway = new CacheAtmGateway();
    private AtomicReference<List<Atm>> atmsRef = new AtomicReference<>(NO_ATMS); // contiene a los cajeros cargados
    private List<Marker> bank_markers = new ArrayList<>();

    private String filterBank = null;
    private AtmNet filterNet = null;
    private double filterRadio = AtmDist.R_500.radius;
    private AtomicReference<Location> currentLocation = new AtomicReference<>();

    private BankGateway bankGateway = new CacheBankGateway(new WebBankGateway());
    private AtomicReference<List<String>> linkBanks = new AtomicReference<>(NO_BANKS);
    private AtomicReference<List<String>> banelcoBanks = new AtomicReference<>(NO_BANKS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (canAccessInternet()) monitorConnection();
        else requestPermissions(INTERNET_PERMS, INTERNET_REQUEST);

        if (canAccessLocation()) launchMap();
        else requestPermissions(INITIAL_PERMS, LOCATION_REQUEST);

        findViewById(R.id.select_bank).setEnabled(false);
        loadSpinnerRadio();
        loadSpinnerNet();
        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(view -> refreshAtms());
    }

    /**
     * Carga los bancos disponibles por red de cajeros de forma asincronica en los campos linkBanks y banelcoBanks respectivamente
     */
    private void loadBanks() {
        new BankFetchTask(bankGateway, b -> linkBanks.compareAndSet(NO_BANKS, b)).execute(AtmNet.LINK);
        new BankFetchTask(bankGateway, b -> banelcoBanks.compareAndSet(NO_BANKS, b)).execute(AtmNet.BANELCO);
    }

    private void monitorConnection() {
        registerReceiver(
                new ConnectionTask(() -> {
                    loadBanks();
                    loadAtms(new AtmRequest());
                }),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    /**
     * Carga los cajeros en el campo atmsRef
     *
     * @param atmRequest Filtros a aplicar. new AtmRequest() implica 'ningun filtro'
     */
    private void loadAtms(AtmRequest atmRequest) {
        new AtmFetchTask(atmGateway, atms -> {
            atmsRef.compareAndSet(NO_ATMS, atms);
            if (atms.isEmpty()) {
                Toast.makeText(this, "Error al cargar cajeros", Toast.LENGTH_SHORT).show();
//            } else {
                //Toast.makeText(this, "Cajeros cargados", Toast.LENGTH_SHORT).show();
            }
        }).execute(atmRequest);
    }


    private void refreshAtms() {
        clearActualMarkers();
        if (currentLocation.get() == null) return; // current location not ready
        List<Atm> atms = Optional.ofNullable(atmsRef.get()).orElse(new ArrayList<>());
        List<Atm> filteredAtms = Atm.filter(atms, filterNet, filterBank, currentLocation.get().getLatitude(), currentLocation.get().getLongitude(), filterRadio);
        filteredAtms.forEach(this::addAtmToMap);
        if (filteredAtms.size() == 0) {
            if (filterRadio < AtmDist.R_1000.radius) {
                Toast.makeText(this, R.string.no_results, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.no_results_max_radio, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void clearActualMarkers() {
        bank_markers.forEach(Marker::remove);
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
        spinnerOptions.add("Cualquier banco");
        spinnerOptions.addAll(Optional.ofNullable(banks).orElse(new ArrayList<>()));

        spinnerBanks = findViewById(R.id.select_bank);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerOptions);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanks.setAdapter(dataAdapter);

        spinnerBanks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFilter = adapterView.getItemAtPosition(i).toString();
                filterBank = selectedFilter.equals("Cualquier banco") ? null : adapterView.getItemAtPosition(i).toString();
                // refreshAtms();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadSpinnerNet() {
        spinnerNets = findViewById(R.id.select_net);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Cualquier red", AtmNet.BANELCO.toString(), AtmNet.LINK.toString())
        );
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNets.setAdapter(dataAdapter);
        spinnerNets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedFilter = adapterView.getItemAtPosition(i).toString();

                if (selectedFilter.equals("Cualquier red")) {
                    findViewById(R.id.select_bank).setEnabled(false);
                    filterNet = null;
                    List<String> banks = new ArrayList<>(banelcoBanks.get() == null ? new ArrayList<>() : banelcoBanks.get());
                    banks.addAll(linkBanks.get() == null ? new ArrayList<>() : linkBanks.get());
                    loadSpinnerBanks(banks);
                } else {
                    findViewById(R.id.select_bank).setEnabled(true);
                    filterNet = AtmNet.fromString(adapterView.getItemAtPosition(i).toString());
                    switch (filterNet) {
                        case BANELCO:
                            loadSpinnerBanks(banelcoBanks.get());
                            break;
                        case LINK:
                            loadSpinnerBanks(linkBanks.get());
                            break;
                    }
                }
                // refreshAtms();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadSpinnerRadio() {
        spinnerRadio = findViewById(R.id.select_radio);
        List<String> list = new ArrayList<>();
        list.add("" + AtmDist.R_100.radius);
        list.add("" + AtmDist.R_200.radius);
        list.add("" + AtmDist.R_500.radius);
        list.add("" + AtmDist.R_1000.radius);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRadio.setAdapter(dataAdapter);
        spinnerRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("radio-selected", adapterView.getItemAtPosition(i).toString());
                filterRadio = Integer.parseInt(adapterView.getItemAtPosition(i).toString());
                // refreshAtms();
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
        currentLocation.set(location);
        Optional.ofNullable(location).ifPresent(this::centerAndMarkLocation);
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
                } else {
                    notAccessInternet();
                }
                break;
        }
    }

    private void centerAndMarkLocation(Location location) {
        Optional.ofNullable(mMap).ifPresent(m -> {
            LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());
//            int height = 75;
//            int width = 75;
//            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.curr_loc);
//            Bitmap b = bitmapdraw.getBitmap();
//            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
//            m.addMarker(
//                    new MarkerOptions()
//                            .position(latLngLocation)
//                            .title("current-location")
//                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
//            );
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
            Location location =
                    Optional.ofNullable(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                            .orElseGet(() -> locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

            if (location != null) {
                Log.d("location-get", "NOT NULL");
                currentLocation.set(location);
                centerAndMarkLocation(location);
            } else {
                Toast.makeText(this, R.string.no_location, Toast.LENGTH_LONG).show();
                Log.d("location-get", "NULL");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5f, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5f, this);
            }
        } catch (SecurityException se) {
            Log.d("location-get", "SE CAUGHT");
            se.printStackTrace();
        }
    }
}
