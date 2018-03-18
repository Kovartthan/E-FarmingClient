package com.ko.efarmingclient.home.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.adapters.CompanyListAdapter;
import com.ko.efarmingclient.model.CompanyInfoPublic;
import com.ko.efarmingclient.ui.EFProgressDialog;
import com.ko.efarmingclient.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;

import static com.ko.efarmingclient.EFApp.getApp;

public class CompanyListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<CompanyInfoPublic> companyInfoArrayList;
    private CompanyListAdapter companyListAdapter;
    private FloatingActionButton fabFilter;
    private String filterLocation;
    private EFProgressDialog efProgressDialog;
    private TextView txtPop;
    private int filterPostion = -1;
    private AlertDialog alertDialog;
    private View dialogView;
    private AlertDialog.Builder dialogBuilder;
    private LayoutInflater inflater;

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
        companyInfoArrayList = new ArrayList<>();
        companyListAdapter = new CompanyListAdapter(getActivity(), companyInfoArrayList);
        recyclerView = view.findViewById(R.id.rv_product_list);
        fabFilter = view.findViewById(R.id.fab_filter);
        efProgressDialog = new EFProgressDialog(getActivity());
        dialogBuilder = new AlertDialog.Builder(getActivity());
        inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.alert_filter, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        dynamicSpinner = dialogView.findViewById(R.id.dynamic_spinner);
        txtSubmit = dialogView.findViewById(R.id.txt_submit);
        txtCancel = dialogView.findViewById(R.id.txt_cancel);
        alertDialog.setCancelable(false);
    }


    private void setupDefault() {
        recyclerView.setAdapter(companyListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getComapnyInfoFromPublicDb();
    }

    private void setupEvent() {
        txtSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                getFilteredCompanyFromDb();
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
                getComapnyInfoFromPublicDb();
            }
        });
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

    ValueEventListener firstValueListener;

    private void getComapnyInfoFromPublicDb() {
        firstValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyInfoArrayList = new ArrayList<>();
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CompanyInfoPublic companyInfoPublic = snapshot.getValue(CompanyInfoPublic.class);
                        companyInfoArrayList.add(companyInfoPublic);
                    }
                }
                companyListAdapter.updateList(companyInfoArrayList);
                if (companyInfoArrayList.size() > 0) {
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
                .getReference().child(Constants.COMPANY_INFO).addValueEventListener(firstValueListener);
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

                if (locationFilterList.size() > 0) {
                    fabFilter.setVisibility(View.VISIBLE);
                } else {
                    fabFilter.setVisibility(View.GONE);
                }

                HashSet<String> hashSet = new HashSet<String>();
                hashSet.addAll(locationFilterList);
                locationFilterList.clear();
                locationFilterList.addAll(hashSet);

                strings = new String[locationFilterList.size()];

                for (int i = 0; i < strings.length; i++) {
                    strings[i] = locationFilterList.get(i);
                }

                if (isAdded() && strings != null && locationFilterList != null) {
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

    private void getFilteredCompanyFromDb() {
        FirebaseDatabase.getInstance().getReference().removeEventListener(firstValueListener);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyInfoArrayList = new ArrayList<>();
                if (dataSnapshot != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CompanyInfoPublic companyInfoPublic = snapshot.getValue(CompanyInfoPublic.class);
                        if (companyInfoPublic != null && companyInfoPublic.city.equals(filterLocation))
                            companyInfoArrayList.add(companyInfoPublic);
                    }
                }
                companyListAdapter.updateList(companyInfoArrayList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled" + databaseError);
            }
        };
        FirebaseDatabase.getInstance()
                .getReference().child(Constants.COMPANY_INFO).addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (spinnerEventListener != null)
            getApp().getFireBaseDataBase().removeEventListener(spinnerEventListener);
    }
}
