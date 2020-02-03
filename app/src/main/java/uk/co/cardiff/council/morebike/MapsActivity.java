package uk.co.cardiff.council.morebike;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.bumptech.glide.load.engine.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import uk.co.cardiff.council.morebike.dialog.LocationInfoDialog;
import uk.co.cardiff.council.morebike.localdb.tables.journey.Journey;
import uk.co.cardiff.council.morebike.localdb.tables.journey.JourneyDao;
import uk.co.cardiff.council.morebike.localdb.tables.journey.JourneyDatabase;
import uk.co.cardiff.council.morebike.tracking.Coordinates;
import uk.co.cardiff.council.morebike.tracking.RouteTracker;
import uk.co.cardiff.council.morebike.utility.ImageHandler;
import uk.co.cardiff.council.morebike.utility.JourneyDataManager;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, JourneyFragment.OnListFragmentInteractionListener {
    private LocationManager locationManager = null;
    private GpsStatusProxy proxy;
    private final int CODE_LOCATION_PERMISSION = 1;

    private GoogleMap mMap;
    private Geocoder geocoder;
    private boolean mLocationPermissionGranted = false;
    private  boolean drawRoute = true;
    private Polyline route = null;
    private PolylineOptions routeOptions = null;

    private static final float DEFAULT_ZOOM = 13f;
    private static int MAPS_ZOOMOUT_PADDING;
    private static WifiManager wifiManager;
    private static JourneyDataManager journeyDataManager;

    private Chronometer chronometer;
    private boolean isStart;
    private long pauseOffset;
    private static JourneyDao db;
    private Fragment openedList;
    private Journey newJourney;

    private double wayLatitude = 0.0, wayLongitude = 0.0;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private RouteTracker routeTrack = new RouteTracker();

    private double totalDistanceTravelledKm;
    private List<Location> traversedPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Button changeLang = findViewById(R.id.changeMyLanguage);
        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show AlertDialog to display list of languages, one can be selected
                showChangeLanguageDialog();
            }
        });
        journeyDataManager = new JourneyDataManager(getApplicationContext(), getString(R.string.hostname));

        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new GpsDialog();

                newFragment.show(getSupportFragmentManager(), "test");
            }
        });

        proxy = GpsStatusProxy.getInstance(getApplicationContext());

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            proxy.register();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
        }


        MAPS_ZOOMOUT_PADDING = (int) (getResources().getDisplayMetrics().heightPixels * 0.10);

        AppCompatButton startButton = findViewById(R.id.start_stop_Button);
        AppCompatButton arrowButton = findViewById(R.id.arrow_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(MapsActivity.this, Locale.UK);

//        Code below adapted from https://medium.com/@droidbyme/get-current-location-using-fusedlocationproviderclient-in-android-cb7ebf5ab88e [Accessed 20/03/2019]
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(2 * 1000);
        locationRequest.setInterval(2*1000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mMap.clear();
                        RouteTracker distance = new RouteTracker();
                        totalDistanceTravelledKm = 0.0;
                        getDeviceLocation();
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        Coordinates currentLocation = new Coordinates(wayLatitude, wayLongitude);
                        routeTrack.getRoute().add(currentLocation);

                        traversedPoints.add(location);

                        Coordinates previousCoord = null;
                        for (Coordinates coord : routeTrack.getRoute()) {
                            if (routeTrack.getRoute().size() > 1 && previousCoord != null) {
                                Polyline line = mMap.addPolyline(new PolylineOptions().add(new
                                        LatLng(previousCoord.getLatitude(),
                                        previousCoord.getLongitude()), new LatLng(coord.getLatitude(),
                                        coord.getLongitude())).color(Color.CYAN).width(20));
                                totalDistanceTravelledKm = totalDistanceTravelledKm +
                                        distance.distanceInKm(previousCoord.getLatitude(),
                                                previousCoord.getLongitude(), coord.getLatitude(),
                                                coord.getLongitude());
                            }
                            previousCoord = coord;

                        }

                    }
                }
            }
        };

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat(getString(R.string.Time));
        chronometer.setBase(SystemClock.elapsedRealtime());

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometerChanged) {
                chronometer = chronometerChanged;
            }
        });

        db = JourneyDatabase.getDatabase(getApplicationContext()).journeyDao();

        ((GestureBottomAppBar) findViewById(R.id.bar)).setOnFlingUpListener(new FlingListener() {
            @Override
            public void onFlingUp() {
                openPastJourneysList();
            }
        });

        arrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPastJourneysList();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopChronometer(v);
            }
        });
    }

    private void showChangeLanguageDialog() {
        //array of languages to display in alert dialog
        final String[] listItems = {"Cymraeg", "English"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        mBuilder.setTitle("Choose Language...");
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    //Welsh
                    setLocale("cy");
                    recreate();
                }
                else if (i == 1){
                    //English
                    setLocale("en");
                    recreate();
                }
                //dismiss alert dialog when language selected
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        //show alert dialog
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        config.setLocale(locale);

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        //save data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        Log.d("LANGUAGE SET", lang);
        editor.commit();
    }

    //load language saved in shared preferences
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        Log.d("LANGUAGE LOAD", language);
        setLocale(language);
    }

    public void openPastJourneysList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<Journey> allElements = db.getAll();
                Collections.reverse(allElements);
                final List<Journey> allJourneys = allElements;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openedList = JourneyFragment.newInstance(allJourneys);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.do_nothing)
                                .replace(R.id.bottom, openedList)
                                .addToBackStack("open_past_journeys")
                                .commit();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(listener);
        proxy.unRegister();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] >= 0) {
                // Permission got granted
                mLocationPermissionGranted = true;
                updateLocationUI();
                getDeviceLocation();
                try {
                    proxy.register();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
                } catch (SecurityException e) {
                    Log.e("LOCATION_PERMISSION", e.toString());
                }

            } else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == -1) {
                LocationInfoDialog locationInfoDialog = new LocationInfoDialog();
                locationInfoDialog.show(getSupportFragmentManager(), getString(R.string.locationInfo));
            }
        }
    }

    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//            proxy.notifyLocation(location);
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
    };



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
        evaluateLocationPermission();
//        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
    public void onZoom(View view){
        if(view.getId() == R.id.zoom_in){
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
        if(view.getId() == R.id.zoom_Out){
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    /**
     * Enable / Disable fine me location button (top-right)
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                // getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("PermissionNotGranted", e.getMessage());
        }
    }

    /**
     * Find the location of the device and move the camera to it.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    moveCamera(location.getLatitude(), location.getLongitude());
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("SecurityException", e.getMessage());
        }
    }

    private void evaluateLocationPermission() {
        //        Code below adapted from https://developer.android.com/training/permissions/requesting#java [Accessed 15/03/2019]
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
        } else {
            // Permission has already been granted
            mLocationPermissionGranted = true;
            updateLocationUI();
            getDeviceLocation();
        }
    }

    /**
     *  Move the camera position to the specified lat/long with the specified zoom level.
     * @param latitude the latitude.
     * @param longitude the longitude.
     * @param zoomLevel the camera zoom level.
     */
    private void moveCamera(double latitude, double longitude, float... zoomLevel) {
        float zoom;
        if (zoomLevel.length == 0)
            zoom = DEFAULT_ZOOM;
        else if (zoomLevel.length != 1)
            throw new IllegalArgumentException("Only one zoom level can be given");
        else
            zoom = zoomLevel[0];

        // https://developers.google.com/maps/documentation/android-sdk/views
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))      // Sets the center of the map to Mountain View
                .zoom(zoom)                   // Sets the zoom
                // .bearing(90)                // Sets the orientation of the camera to east
                //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    

    public void startStopChronometer(View v){
        if(isStart){
            endRouteTracking();
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            isStart = false;
            ((AppCompatButton) v).setText(getString(R.string.start));
        } else {
            AppCompatButton button = (AppCompatButton) findViewById(R.id.start_stop_Button);
            startRouteTracking();
            button.setText(getString(R.string.stop));
            final Animation myAnim = AnimationUtils.loadAnimation(this,
                    R.anim.bounce);
            BounceInterpolator interpolator = new BounceInterpolator();
            myAnim.setDuration(500);
            myAnim.setInterpolator(interpolator);
            button.startAnimation(myAnim);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

            isStart = true;
        }
    }

//    Tracking code adapted from https://stackoverflow.com/questions/27068491/how-do-i-dynamically-add-location-points-to-a-polyline-in-android-googlemap [Accessed on 20/03/2019]
    public void startRouteTracking() {
        if (mMap != null) {
            try {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                newJourney = new Journey();
                newJourney.setStartTime(new Date());
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        try {
                            newJourney.setStartAddress(geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1)
                                    .get(0).getAddressLine(0));
                        } catch (IOException e) {
                            Log.e("JourneyCreation", e.getMessage());
                        }
                    }
                });

            } catch (SecurityException e) {
                Log.e("MainActivity", e.getMessage());
            }

        }
    }

    public void endRouteTracking() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        totalDistanceTravelledKm = Math.round(totalDistanceTravelledKm * 100.0) / 100.0;
        Bike bikeEmissions = new Bike(totalDistanceTravelledKm);
        double emissionsSaved = bikeEmissions.getEmissions();

        EndOfRideInfoDialog endOfRideDialog = EndOfRideInfoDialog.newInstance(totalDistanceTravelledKm, emissionsSaved);
        endOfRideDialog.show(getSupportFragmentManager(), getString(R.string.EndOfRideInfo));

        if (traversedPoints.size() > 0) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (Location point : traversedPoints) {
                boundsBuilder.include(new LatLng(point.getLatitude(), point.getLongitude()));
            }

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(), getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels, MAPS_ZOOMOUT_PADDING);
            mMap.moveCamera(cu);

            final List<Location> localTraversedPoints = traversedPoints;
            final double localTotalDistanceTravelledKm = totalDistanceTravelledKm;


            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    mMap.clear();
                    try {
                        Address endLocation = geocoder.getFromLocation(localTraversedPoints.get(localTraversedPoints.size() - 1).getLatitude(),
                                localTraversedPoints.get(localTraversedPoints.size() - 1).getLongitude(), 1).get(0);
                        newJourney.setEndAddress(endLocation.getAddressLine(0));
                        newJourney.setKilometersTravelled(localTotalDistanceTravelledKm);
                        newJourney.setEndTime(new Date());
                        Log.d("JourneyCreation", newJourney.toString());

                        final Bitmap croppedBitmap;

                        if (bitmap.getWidth() >= bitmap.getHeight()) {
                            croppedBitmap = Bitmap.createBitmap(
                                    bitmap, bitmap.getWidth() / 2 - bitmap.getHeight() / 2, 0, bitmap.getHeight(), bitmap.getHeight()
                            );

                        } else {
                            croppedBitmap = Bitmap.createBitmap(
                                    bitmap, 0, bitmap.getHeight() / 2 - bitmap.getWidth() / 2, bitmap.getWidth(), bitmap.getWidth()
                            );
                        }

                        saveJourneyWithImage(newJourney, croppedBitmap);


                    } catch (Exception e) {
                        Log.e("JourneyCreation", "Couldn't create Journey: " + e);
                    }
                }
            });
        }
        traversedPoints = new ArrayList<>(); // reset points visited
        routeTrack.clearRoute();
    }

    public void saveJourneyWithImage(final Journey journey, final Bitmap bitmap) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                db.createAll(journey);
                int id = db.getLastEntry().getId();

                ImageHandler.saveImageToInternalStorage(MapsActivity.this, bitmap, String.valueOf(id), "png");

                try {
                    journeyDataManager.sendUnsentJourneys();
                } catch (IllegalStateException e) {
                    Log.w("MapsActivity", e.toString());
                }
            }
        });
    }


    @Override
    public void onListFragmentInteraction(Journey item) {
        if (openedList != null ) {
            Log.d("GsonTest", item.toJson());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_through_bottom)
                    .add(openedList.getId(), JourneyDetail.newInstance(item.getId()))
                    .addToBackStack("open_journey_id")
                    .commit();
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLocationUpdates();
//    }

//    private void stopLocationUpdates() {
//        // TODO: Set lower frequency instead
//        mFusedLocationClient.removeLocationUpdates(locationCallback);
//    }
}
