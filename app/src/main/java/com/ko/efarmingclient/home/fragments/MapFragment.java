package com.ko.efarmingclient.home.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.ko.efarmingclient.EFApp;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.adapters.MarkerInfoViewAdapter;
import com.ko.efarmingclient.home.navigationcomponents.HttpConnection;
import com.ko.efarmingclient.home.navigationcomponents.PathJSONParser;
import com.ko.efarmingclient.listener.OnNavigationListener;
import com.ko.efarmingclient.model.ClusterCompanyInfoMarker;
import com.ko.efarmingclient.model.CompanyInfoPublic;
import com.ko.efarmingclient.model.NearByFinderModel;
import com.ko.efarmingclient.util.Constants;
import com.ko.efarmingclient.util.GpsUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.ko.efarmingclient.EFApp.getApp;
import static com.ko.efarmingclient.util.Constants.REQUEST_CHECK_SETTINGS;
import static com.ko.efarmingclient.util.Constants.WRONG_WAY_DISTANCE;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnNavigationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private MapView googleMapView;
    private GoogleMap googleMap;
    private ArrayList<CompanyInfoPublic> companyInfoPublicArrayList;
    private ClusterManager<ClusterCompanyInfoMarker> mClusterManager;
    private MarkerInfoViewAdapter markerInfoViewAdapter;
    private Location currentLocation = null;
    protected GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public final static int FAST_LOCATION_FREQUENCY = 800;
    public final static int LOCATION_FREQUENCY = 2 * 800;
    private FusedLocationProviderClient mFusedLocationClient;
    private double latitude, longitude;
    private Polyline polylineFinal;
    private ArrayList<LatLng> points; //added
    private Polyline line, wrongLine; //added
    private Marker currentNavigationMarker;
    private boolean isNavigationStart = false;
    private static final float SMALLEST_DISPLACEMENT = 0.25F;
    private ArrayList<LatLng> navigationTrackList;
    private PolylineOptions options, wrongPolylineOptions;
    private double destinationLatitude, destinationLongitude;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        startLocationUpdates(getActivity());
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        googleMapView = view.findViewById(R.id.mapView);
        googleMapView.onCreate(savedInstanceState);
        init(view);
        setupDefault();
        setupEvents();
        return view;
    }

    private void init(View view) {
        companyInfoPublicArrayList = new ArrayList<>();
        buildGoogleApiClient();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startLocationUpdates(getActivity());
            }
        }, 200);
        points = new ArrayList<LatLng>();
    }

    private void setupDefault() {
        setupGoogleMap();
    }

    private void setupGoogleMap() {
        googleMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupEvents() {
        googleMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapFragment.this.googleMap = googleMap;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                MapFragment.this.googleMap.setMyLocationEnabled(true);
                mClusterManager = new ClusterManager<>(getActivity(), googleMap);
                MapFragment.this.googleMap.setOnCameraIdleListener(mClusterManager);
                MapFragment.this.googleMap.setOnMarkerClickListener(mClusterManager);
                populateCompanyLocationOnMap();
                setupInfoWindowAdapterToMarker();
            }
        });
    }

    private void setupInfoWindowAdapterToMarker() {
        markerInfoViewAdapter = new MarkerInfoViewAdapter(LayoutInflater.from(getActivity()));
        markerInfoViewAdapter.setOnNavigationListener(this);
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(markerInfoViewAdapter);
        googleMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                onNavigationStart(marker);
            }
        });
    }

    private void populateCompanyLocationOnMap() {
        getApp().getFireBaseDataBase().child(Constants.COMPANY_INFO).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CompanyInfoPublic companyInfoPublic = snapshot.getValue(CompanyInfoPublic.class);
                        companyInfoPublicArrayList.add(companyInfoPublic);
                    }
                    generateMarkersOnMap(companyInfoPublicArrayList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void generateMarkersOnMap(ArrayList<CompanyInfoPublic> companyInfoPublicArrayList) {
        for (CompanyInfoPublic companyInfo : companyInfoPublicArrayList) {
            LatLng latLng = new LatLng(companyInfo.latitude, companyInfo.longitude);
            ClusterCompanyInfoMarker clusterCompanyInfoMarker = new ClusterCompanyInfoMarker(latLng, companyInfo.name);
            mClusterManager.addItem(clusterCompanyInfoMarker);
        }
        mClusterManager.cluster();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        googleMapView.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onNavigationStart(Marker marker) {
        if (marker != null & currentLocation != null) {
            String url = getMapsApiDirectionsUrl(marker);
            removePreviousPolyline();
            ReadTask downloadTask = new ReadTask();
            downloadTask.execute(url);
        }
    }

    private void removePreviousPolyline() {
        if (polylineFinal != null)
            polylineFinal.remove();
        if (line != null) {
            line.remove();
        }
    }

    private String getMapsApiDirectionsUrl(Marker marker) {
        destinationLatitude = 0.0;
        destinationLatitude = 0.0;
        String str_origin = "origin=" + currentLocation.getLatitude() + ","
                + currentLocation.getLongitude();
        String str_dest = "destination=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
        String parameters = str_origin + "&" + str_dest + "&";
        String output = "json";
        String key = "key" + "AIzaSyBH7_9bk85TYmDQMMXVXW_JpyB-Rln4wv4";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + parameters + key;
        destinationLatitude = marker.getPosition().latitude;
        destinationLongitude = marker.getPosition().longitude;
        return url;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                navigationTrackList = new ArrayList<>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                navigationTrackList.addAll(points);
                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
                polyLineOptions.color(Color.BLUE);
            }

            polylineFinal = googleMap.addPolyline(polyLineOptions);

            isNavigationStart = true;

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
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

    private void getLastKnownLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            setCurrentLocation(location);
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            moveMap(latitude, longitude);
                        } else {

                        }
                    }
                });
    }


    /**
     * config request location update
     */
    private void configRequestLocationUpdate() {
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_FREQUENCY)
                .setSmallestDisplacement(SMALLEST_DISPLACEMENT)
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
        } else {
            mGoogleApiClient.connect();
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
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // connection to Google Play services was lost for some reason
        Toast.makeText(getActivity(), "Retrying....", Toast.LENGTH_LONG).show();
        if (null != mGoogleApiClient) {
            mGoogleApiClient.connect(); // attempt to establish a new connection
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Check your gps signal or click and try again", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e("Map", "onLocationChanged");
        if (currentNavigationMarker != null) {
            currentNavigationMarker.remove();
        }
        if (location != null) {
            setCurrentLocation(location);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            if (isNavigationStart) {
                points.add(latLng);
                if (PolyUtil.isLocationOnEdge(latLng, polylineFinal.getPoints(), true, 10)) {
                    redrawLineCorrectPath(latLng);
                } else {
                    redrawWrongPath(latLng);
                }
                checkNavigationIsReachedDestination(latLng);
            }
        }
    }

    private synchronized void redrawWrongPath(LatLng latLng) {
        wrongPolylineOptions = new PolylineOptions().width(12).color(Color.GREEN).geodesic(true);

        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            wrongPolylineOptions.add(point);
        }

        addMarkerForNavigation(latLng);
        wrongLine = googleMap.addPolyline(wrongPolylineOptions);

    }


    private synchronized void redrawLineCorrectPath(LatLng latLng) {

        if (wrongLine != null) {
            wrongLine.remove();
            wrongLine = null;
        }

        options = new PolylineOptions().width(12).color(Color.GREEN).geodesic(true);

        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }

        addMarkerForNavigation(latLng);
        line = googleMap.addPolyline(options);

//        if (checkRacerRoutePath != null) {
//            checkRacerRoutePath.cancel(true);
//            checkRacerRoutePath = null;
//        }
//
//        checkRacerRoutePath = new CheckNavigationRoutePath(latLng);
//        checkRacerRoutePath.execute();

    }

    private void addMarkerForNavigation(LatLng latLng) {

//        currentNavigationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation_blue_600_24dp)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    private void moveMap(double latitude, double longitude) {

        googleMap.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    private synchronized void checkNavigationIsReachedDestination(LatLng latLng) {
        double tempDestinationLatitude = destinationLatitude;
        double tempDestinationLongitude = destinationLongitude;
//        if (((latLng.latitude > tempDestinationLatitude) && (latLng.latitude < tempDestinationLatitude + 0.002))
//                && ((latLng.longitude > tempDestinationLongitude) && (latLng.longitude < tempDestinationLongitude + 0.002))) {
//            Toast.makeText(getActivity(), "Desit plus  reached", Toast.LENGTH_LONG).show();
//            isNavigationStart = false;
//            line.remove();
//            polylineFinal.remove();
//            wrongLine.remove();
//        }else  if (((latLng.latitude > tempDestinationLatitude) && (latLng.latitude < tempDestinationLatitude - 0.002))
//                && ((latLng.longitude > tempDestinationLongitude) && (latLng.longitude < tempDestinationLongitude - 0.002))) {
//            Toast.makeText(getActivity(), "Desit minus   reached", Toast.LENGTH_LONG).show();
//            isNavigationStart = false;
//            line.remove();
//            polylineFinal.remove();
//            wrongLine.remove();
//        }

        if (destinationLatitude == latLng.latitude && destinationLongitude == latLng.longitude) {
            isNavigationStart = false;
            line.remove();
            polylineFinal.remove();
            wrongLine.remove();
        }
    }


//    private CheckNavigationRoutePath checkRacerRoutePath;
//    class CheckNavigationRoutePath extends AsyncTask<Void, Void, ArrayList<NearByFinderModel>> {
//
//        boolean isInNavigationTrack = false;
//        LatLng target;
//        ArrayList<NearByFinderModel> navigationTrackingList = new ArrayList<>();
//        LatLng latLng;
//
//        public CheckNavigationRoutePath(LatLng latLng) {
//            this.latLng = latLng;
//        }
//
//        @Override
//        protected ArrayList<NearByFinderModel> doInBackground(Void... voids) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    navigationTrackingList = getNavigationTrackingList();
//                }
//            });
//            return navigationTrackingList;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<NearByFinderModel> distance) {
//            super.onPostExecute(distance);
//            target = new LatLng(currentNavigationMarker.getPosition().latitude, currentNavigationMarker.getPosition().longitude);
//            if (navigationTrackingList.size() > 0 && navigationTrackingList.get(navigationTrackingList.size() -1).distance > WRONG_WAY_DISTANCE) {
//                isInNavigationTrack = false;
//                Log.e("Navigation","Going in the wrong direction");
////                options = new PolylineOptions().width(10).color(Color.YELLOW).geodesic(true);
////                for (int i = 0; i < points.size(); i++) {
////                    LatLng point = points.get(i);
////                    options.add(point);
////                }
////                addMarkerForNavigation(latLng);
////                line = googleMap.addPolyline(options);
//            } else {
//                isInNavigationTrack = true;
//                Log.e("Navigation","Going in the right direction");
////                options = new PolylineOptions().width(10).color(Color.GREEN).geodesic(true);
////                for (int i = 0; i < points.size(); i++) {
////                    LatLng point = points.get(i);
////                    options.add(point);
////                }
////                addMarkerForNavigation(latLng);
////                line = googleMap.addPolyline(options);
//            }
//        }
//    }
//
//    public ArrayList<NearByFinderModel> getNavigationTrackingList() {
//
//        ArrayList<NearByFinderModel> distance = new ArrayList<>();
//
//        if (polylineFinal == null) {
//            return distance;
//        }
//
//        if (navigationTrackList != null && navigationTrackList.size() > 0) {
//
//            for (LatLng latLng : navigationTrackList) {
//                Double dis = GpsUtils.distance(currentNavigationMarker.getPosition().latitude, currentNavigationMarker.getPosition().longitude, latLng.latitude, latLng.longitude) * 1000;
//                distance.add(new NearByFinderModel(dis, latLng));
//            }
//
//        }
//        if (distance.size() > 0) {
//
//            Collections.sort(distance, new Comparator<NearByFinderModel>() {
//                @Override
//                public int compare(NearByFinderModel c1, NearByFinderModel c2) {
//                    return Double.compare(c1.distance, c2.distance);
//                }
//            });
//
//            Log.e("Navigation", "Lowest Distance" + distance.get(0).distance);
//        }
//
//        return distance;
//
//    }


}
