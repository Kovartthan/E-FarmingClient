package com.ko.efarmingclient.home.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.activities.ChatActivity;
import com.ko.efarmingclient.home.adapters.ProductListAdapter;
import com.ko.efarmingclient.listener.OnProductInfoOpenListener;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.model.UserRating;
import com.ko.efarmingclient.ui.EFProgressDialog;
import com.ko.efarmingclient.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;


public class ProductListFragment extends Fragment implements OnProductInfoOpenListener {
    private static final int REQUEST_PHONE_CALL = 1500;
    private RecyclerView recyclerView;
    private ArrayList<ProductInfo> productInfoArrayList;
    private ProductListAdapter productListAdapter;
    private FloatingActionButton fabFilter;
    private String filterLocation;
    private EFProgressDialog efProgressDialog;
    private TextView txtPop;
    private int filterPostion = -1;

    public ProductListFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        init(view);
        setupDefault();
        setupEvent();
        return view;
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.rv_product_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setItemViewCacheSize(20);
//        recyclerView.setDrawingCacheEnabled(true);
//        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        productInfoArrayList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(getActivity(), productInfoArrayList, new ArrayList<UserRating>());
        productListAdapter.setOnProductInfoOpenListener(this);
        fabFilter = view.findViewById(R.id.fab_filter);
        recyclerView.setAdapter(productListAdapter);
        efProgressDialog = new EFProgressDialog(getActivity());
    }

    AlertDialog alertDialog;
    View dialogView;
    AlertDialog.Builder dialogBuilder;
    LayoutInflater inflater;

    private void setupDefault() {
        dialogBuilder = new AlertDialog.Builder(getActivity());
        inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.alert_filter, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        dynamicSpinner = dialogView.findViewById(R.id.dynamic_spinner);
        txtSubmit = dialogView.findViewById(R.id.txt_submit);
        txtCancel = dialogView.findViewById(R.id.txt_cancel);
        alertDialog.setCancelable(false);
        txtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                getFilteredProductFromDb();
            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (valueEventListener != null)
                    FirebaseDatabase.getInstance().getReference().removeEventListener(valueEventListener);
//                if (spinnerEventListener != null)
//                    FirebaseDatabase.getInstance().getReference().removeEventListener(spinnerEventListener);
                if (locationFilterList != null)
                    locationFilterList.clear();
                filterPostion = -1;
                getProductListFromPublicDataBase();
            }
        });
        getProductListFromPublicDataBase();
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
    }

    ValueEventListener firstValueListener;

    private void getProductListFromPublicDataBase() {
        firstValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("TAG", "onDataChange called ");
                productInfoArrayList = new ArrayList<>();
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ProductInfo productInfo = snapshot.getValue(ProductInfo.class);
                        productInfoArrayList.add(productInfo);
                    }
                }
                productListAdapter.updateList(productInfoArrayList);
                if (productInfoArrayList.size() > 0) {
                    fabFilter.setVisibility(View.VISIBLE);
                } else {
                    fabFilter.setVisibility(View.GONE);
                }
                getLocationFilter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled" + databaseError);
            }
        };

        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).addValueEventListener(firstValueListener);

//        getLocationFilter();

    }

    private void setupEvent() {
        fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterAlert();
            }
        });
    }

    private AppCompatSpinner dynamicSpinner;
    private TextView txtSubmit;
    private TextView txtCancel;

    private ArrayList<String> locationFilterList = new ArrayList<>();


    private void showFilterAlert() {
        if (isAdded()) {
            efProgressDialog.show();
        }
        if (isAdded()) {
            efProgressDialog.dismiss();
        }
        alertDialog.show();

    }

    private ValueEventListener spinnerEventListener;
    String[] strings = null;
    ArrayAdapter<String> adapter;

    private synchronized void getLocationFilter() {
        spinnerEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String location = snapshot.getValue().toString();
                    locationFilterList.add(location);
                }
                HashSet<String> hashSet = new HashSet<String>();
                hashSet.addAll(locationFilterList);
                locationFilterList.clear();
                locationFilterList.addAll(hashSet);

                strings = new String[locationFilterList.size()];

                for (int i = 0; i < strings.length; i++) {
                    strings[i] = locationFilterList.get(i);
                }

                if (strings != null) {
                    adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, strings);
                    dynamicSpinner.setAdapter(adapter);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterLocation = (String) parent.getAdapter().getItem(position);
                filterPostion = position;
                dynamicSpinner.setSelection(filterPostion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        FirebaseDatabase.getInstance().getReference().child("location_filter").addListenerForSingleValueEvent(spinnerEventListener);

    }

    ValueEventListener valueEventListener;

    private void getFilteredProductFromDb() {
        FirebaseDatabase.getInstance().getReference().removeEventListener(firstValueListener);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productInfoArrayList = new ArrayList<>();
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ProductInfo productInfo = snapshot.getValue(ProductInfo.class);
                        if (productInfo != null && productInfo.company_info.location.contains(filterLocation))
                            productInfoArrayList.add(productInfo);
                    }
                }
                productListAdapter.updateList(productInfoArrayList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled" + databaseError);
            }
        };
        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).addValueEventListener(valueEventListener);
    }

    @Override
    public void openChat(ProductInfo productInfo) {
        startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("Product_id", productInfo));
    }

    @Override
    public void callToUser(ProductInfo productInfo) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + productInfo.company_info.phone));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    @Override
    public void onSetRatingToProducts(int rating, ProductInfo productInfo) {

    }

    @Override
    public int onGetRatingFromFirebase(ProductInfo productInfo) {
        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                }
                break;
        }
    }
}
