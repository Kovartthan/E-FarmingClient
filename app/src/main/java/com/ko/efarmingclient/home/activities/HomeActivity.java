package com.ko.efarmingclient.home.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.ko.efarmingclient.R;
import com.ko.efarmingclient.base.BaseActivity;
import com.ko.efarmingclient.home.fragments.CompanyListFragment;
import com.ko.efarmingclient.home.fragments.ProfileFragment;
import com.ko.efarmingclient.home.fragments.MapFragment;
import com.ko.efarmingclient.home.fragments.ProductListFragment;

public class HomeActivity extends BaseActivity {

    private BottomNavigationView navigation;
    private Fragment fragment;

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
                    fragment = new CompanyListFragment();
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
    private boolean isLocationSet;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

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
    }

    private void setupDefaults() {
        setMapFragmentAtInit();
        setOnlineStatus(true);
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


}
