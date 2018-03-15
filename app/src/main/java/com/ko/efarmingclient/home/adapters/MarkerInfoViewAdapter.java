package com.ko.efarmingclient.home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.listener.OnNavigationListener;
import com.ko.efarmingclient.util.TextUtils;

public class MarkerInfoViewAdapter implements GoogleMap.InfoWindowAdapter {

    private final LayoutInflater mInflater;

    private OnNavigationListener onNavigationListener;

    public void setOnNavigationListener(OnNavigationListener onNavigationListener){
        this.onNavigationListener = onNavigationListener;
    }

    public MarkerInfoViewAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        final View popup = mInflater.inflate(R.layout.info_title, null);
        ((TextView) popup.findViewById(R.id.title)).setText(TextUtils.capitalizeFirstLetter(marker.getTitle()));
//        ((Button) popup.findViewById(R.id.btn_navigate)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onNavigationListener.onNavigationStart(marker);
//            }
//        });
        return popup;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        final View popup = mInflater.inflate(R.layout.info_title, null);
        ((TextView) popup.findViewById(R.id.title)).setText(TextUtils.capitalizeFirstLetter(marker.getTitle()));
//        ((Button) popup.findViewById(R.id.btn_navigate)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onNavigationListener.onNavigationStart(marker);
//            }
//        });
        return popup;
    }
}