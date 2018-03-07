package com.ko.efarmingclient.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by admin on 3/7/2018.
 */

public class ClusterCompanyInfoMarker implements ClusterItem {
    private LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public ClusterCompanyInfoMarker(LatLng mPosition, String mTitle) {
        this.mPosition = mPosition;
        this.mTitle = mTitle;
    }

    public ClusterCompanyInfoMarker(LatLng mPosition, String mTitle, String mSnippet) {
        this.mPosition = mPosition;
        this.mTitle = mTitle;
        this.mSnippet = mSnippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
