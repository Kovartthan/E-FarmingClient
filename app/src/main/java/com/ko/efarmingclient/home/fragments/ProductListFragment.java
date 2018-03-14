package com.ko.efarmingclient.home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.activities.ChatActivity;
import com.ko.efarmingclient.home.adapters.ProductListAdapter;
import com.ko.efarmingclient.listener.OnChatOpenListener;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.util.Constants;

import java.util.ArrayList;


public class ProductListFragment extends Fragment implements OnChatOpenListener {
    private RecyclerView recyclerView;
    private ArrayList<ProductInfo> productInfoArrayList;
    private ProductListAdapter productListAdapter;
    private FloatingActionButton fabFilter;
    private String filterLocation;

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
        productInfoArrayList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(getActivity(), productInfoArrayList);
        productListAdapter.setOnChatOpenListener(this);
        fabFilter = view.findViewById(R.id.fab_filter);
        recyclerView.setAdapter(productListAdapter);
    }

    private void setupDefault() {
        getProductListFromPublicDataBase();
    }

    ValueEventListener firstValueListener;

    private void getProductListFromPublicDataBase() {
        firstValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled" + databaseError);
            }
        };

        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).addValueEventListener(firstValueListener);

        for (ProductInfo productInfo : productInfoArrayList) {
            Log.e("TAG", "" + productInfo.productName);
        }

    }

    private void setupEvent() {
        fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterAlert();
            }
        });
    }

    private Spinner dynamicSpinner;
    private TextView txtSubmit;
    private TextView txtCancel;

    private void showFilterAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_filter, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(false);
        dynamicSpinner = dialogView.findViewById(R.id.dynamic_spinner);
        txtSubmit = dialogView.findViewById(R.id.txt_submit);
        txtCancel = dialogView.findViewById(R.id.txt_cancel);

        ArrayList<String> locationFilterList = getLocationFilter();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, locationFilterList);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterLocation = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dynamicSpinner.setAdapter(adapter);

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
            }
        });

        alertDialog.show();

    }

    private ArrayList<String> getLocationFilter() {
        final ArrayList<String> locationFilterList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("location_filter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String location = snapshot.getValue().toString();
                    locationFilterList.add(location);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return locationFilterList;
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
                        if (productInfo.company_info.location.equalsIgnoreCase(filterLocation))
                            productInfoArrayList.add(productInfo);
                    }
                }
                productListAdapter.updateList(productInfoArrayList);
                if (productInfoArrayList.size() > 0) {
                    fabFilter.setVisibility(View.VISIBLE);
                } else {
                    fabFilter.setVisibility(View.GONE);
                }
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
}
