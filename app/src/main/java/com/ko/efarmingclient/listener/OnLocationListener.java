package com.ko.efarmingclient.listener;

import android.location.Location;

public interface OnLocationListener {
    void onLocationChanged(Location location);

    void onLocationConnected();

    void onLocationConnectionSuspended();

    void onLocationConnectionFailed();
}