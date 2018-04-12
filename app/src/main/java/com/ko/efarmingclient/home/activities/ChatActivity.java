package com.ko.efarmingclient.home.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ko.efarmingclient.EFApp;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.fragments.ChatFragment;
import com.ko.efarmingclient.model.ProductInfo;

import org.greenrobot.eventbus.EventBus;

public class ChatActivity extends AppCompatActivity {
    private Fragment fragment;
    private ProductInfo productId;
    public TextView txtChatUserName, txtRequestFor;
    public ImageView imgProfile;
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
        txtChatUserName = (TextView) findViewById(R.id.txt_user_name);
        txtRequestFor = findViewById(R.id.txt_request_for);
        imgProfile = findViewById(R.id.img_profile_img);
        Bundle bundle = new Bundle();
        productId = (ProductInfo) getIntent().getSerializableExtra("Product_id");
        bundle.putSerializable("Product_id", productId);
        fragment.setArguments(bundle);
    }

    private void setupDefaults() {
        applyFragment();
    }


    /**
     * *used to change the fragment *****/

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
        findViewById(R.id.img_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EFApp.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EFApp.setChatActivityOpen(false);
    }



}
