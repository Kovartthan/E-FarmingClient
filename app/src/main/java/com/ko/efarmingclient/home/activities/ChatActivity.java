package com.ko.efarmingclient.home.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.fragments.ChatFragment;
import com.ko.efarmingclient.model.ProductInfo;

public class ChatActivity extends AppCompatActivity {
    private Fragment fragment;
    private ProductInfo productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        setupDefaults();
        setupEvents();
    }

    private void init() {
        fragment = new ChatFragment();
        Bundle bundle = new Bundle();
        productId = (ProductInfo) getIntent().getSerializableExtra("Product_id");
        bundle.putSerializable("Product_id", productId);
        fragment.setArguments(bundle);
    }

    private void setupDefaults() {
        setupToolbar();
        applyFragment();
    }

    private void setupToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Request product to ShopKeeper");
    }

    private void applyFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment, fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupEvents() {

    }

}
