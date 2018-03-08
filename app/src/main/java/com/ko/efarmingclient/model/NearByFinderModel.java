package com.ko.efarmingclient.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by admin on 27-Nov-17.
 */

public class NearByFinderModel {
    public Double distance;
    public LatLng coordinates;
    public NearByFinderModel(Double distance, LatLng coordinates) {
        this.distance = distance;
        this.coordinates = coordinates;
    }
}
