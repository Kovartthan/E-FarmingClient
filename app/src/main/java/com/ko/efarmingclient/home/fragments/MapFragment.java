package com.ko.efarmingclient.home.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.ko.efarmingclient.EFApp;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.adapters.MarkerInfoViewAdapter;
import com.ko.efarmingclient.home.navigationcomponents.HttpConnection;
import com.ko.efarmingclient.home.navigationcomponents.PathJSONParser;
import com.ko.efarmingclient.listener.OnNavigationListener;
import com.ko.efarmingclient.model.ClusterCompanyInfoMarker;
import com.ko.efarmingclient.model.CompanyInfoPublic;
import com.ko.efarmingclient.util.Constants;
import com.ko.efarmingclient.util.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import static com.ko.efarmingclient.EFApp.getApp;
import static com.ko.efarmingclient.util.Constants.REQUEST_CHECK_SETTINGS;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener,OnNavigationListener {
    private MapView mMapView;
    private GoogleMap googleMap;
    private ArrayList<CompanyInfoPublic> companyInfoPublicArrayList;
    private ClusterManager<ClusterCompanyInfoMarker> mClusterManager;
    private MarkerInfoViewAdapter markerInfoViewAdapter;
    private Location currentLocation = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        init(view);
        setupDefault();
        setupEvents();
        return view;
    }

    private void init(View view) {
        companyInfoPublicArrayList = new ArrayList<>();

    }

    private void setupDefault() {
        setupGoogleMap();
    }

    private void setupGoogleMap() {
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupEvents() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                mClusterManager = new ClusterManager<>(getActivity(), googleMap);
                googleMap.setOnCameraIdleListener(mClusterManager);
                googleMap.setOnMarkerClickListener(mClusterManager);
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
        mMapView.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onNavigationStart(Marker marker) {
        if(marker != null & currentLocation != null) {
            String url = getMapsApiDirectionsUrl(marker);
            ReadTask downloadTask = new ReadTask();
            downloadTask.execute(url);
        }
    }

    private String getMapsApiDirectionsUrl(Marker marker) {
        String waypoints = "waypoints=optimize:true|"
                + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                + "|" + "|" + marker.getPosition().latitude + ","
                + marker.getPosition().longitude;
        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
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
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            googleMap.addPolyline(polyLineOptions);
        }
    }


}
