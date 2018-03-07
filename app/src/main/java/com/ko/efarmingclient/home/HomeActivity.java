package com.ko.efarmingclient.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.ko.efarmingclient.EFApp;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.base.BaseActivity;
import com.ko.efarmingclient.home.fragments.MapFragment;
import com.ko.efarmingclient.home.fragments.ProductListFragment;

import static com.ko.efarmingclient.util.Constants.REQUEST_CHECK_SETTINGS;

public class HomeActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private BottomNavigationView navigation;
    private Fragment fragment;
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public final static int FAST_LOCATION_FREQUENCY = 1000;
    public final static int LOCATION_FREQUENCY = 2 * 1000;
    private FusedLocationProviderClient mFusedLocationClient;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    fragment = new MapFragment();
                    applyFragment();
                    return true;
                case R.id.navigation_products:
                    fragment = new ProductListFragment();
                    applyFragment();
                    return true;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    applyFragment();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        setupDefaults();
        setupEvents();
    }

    private void init() {
        navigation = findViewById(R.id.navigation);
        buildGoogleApiClient();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationUpdates(HomeActivity.this);
            }
        },200);
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    private void setupDefaults() {
        setMapFragmentAtInit();
    }

    private void setMapFragmentAtInit(){
        fragment = new MapFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    private void setupEvents() {
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void applyFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopLocationUpdates();
    }


    private synchronized void buildGoogleApiClient() {
        // setup googleapi client
        mGoogleApiClient = new GoogleApiClient.Builder(EFApp.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
        // setup location updates
        configRequestLocationUpdate();
    }


    /**
     * config request location update
     */
    private void configRequestLocationUpdate() {
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_FREQUENCY)
                .setFastestInterval(FAST_LOCATION_FREQUENCY);
    }


    /**
     * request location updates
     */
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );


    }

    /**
     * start location updates
     */
    public void startLocationUpdates(final Activity activity) {
        // connect and force the updates
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            requestLocationUpdates();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

                    final Status status = locationSettingsResult.getStatus();

                    Log.e("Fused", "onResult() called with: " + "result = [" + status.getStatusMessage() + "]");
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                Log.d("Fused", "", e);
                                // Ignore the error.
                            }

                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }

                }
            });
        }

    }

    /**
     * removes location updates from the FusedLocationApi
     */
    public void stopLocationUpdates() {
        // stop updates, disconnect from google api
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        // do location updates
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // connection to Google Play services was lost for some reason
        Toast.makeText(HomeActivity.this, "Retrying....", Toast.LENGTH_LONG).show();
        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect(); // attempt to establish a new connection
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(HomeActivity.this, "Check your gps signal or click and try again", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("Map","onLocationChanged");
        if(location != null){
            if(fragment instanceof MapFragment){
                ((MapFragment)fragment).setCurrentLocation(location);
            }
        }
    }

}
