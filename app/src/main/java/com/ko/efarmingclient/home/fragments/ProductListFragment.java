package com.ko.efarmingclient.home.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.adapters.ProductListAdapter;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.util.Constants;

import java.util.ArrayList;


public class ProductListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<ProductInfo> productInfoArrayList;
    private ProductListAdapter productListAdapter;

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
        recyclerView.setAdapter(productListAdapter);
    }

    private void setupDefault() {
        getProductListFromPublicDataBase();

    }

    private void getProductListFromPublicDataBase() {
        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).addValueEventListener(new ValueEventListener() {
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled" + databaseError);
            }
        });
        for (ProductInfo productInfo : productInfoArrayList) {
            Log.e("TAG", "" + productInfo.productName);
        }

    }

    private void setupEvent() {

    }

}
