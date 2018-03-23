package com.ko.efarmingclient.home.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.base.BaseActivity;
import com.ko.efarmingclient.home.adapters.ProductListAdapter;
import com.ko.efarmingclient.listener.OnProductInfoOpenListener;
import com.ko.efarmingclient.model.CompanyInfoPublic;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.util.Constants;
import com.ko.efarmingclient.util.TextUtils;

import java.util.ArrayList;

public class ProductListActivity extends BaseActivity implements OnProductInfoOpenListener {
    private static final int REQUEST_PHONE_CALL = 1500;
    private RecyclerView recyclerView;
    private ArrayList<ProductInfo> productInfoArrayList;
    private ProductListAdapter productListAdapter;
    private FloatingActionButton fabFilter;
    private String companyKey;
    private ProgressDialog progressDialog;
    private int userRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_list);
        init();
        setupDefault();
        setupEvent();
    }

    private void init() {
        recyclerView = findViewById(R.id.rv_product_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productInfoArrayList = new ArrayList<>();
        productListAdapter = new ProductListAdapter(this, productInfoArrayList);
        productListAdapter.setOnProductInfoOpenListener(this);
        fabFilter = findViewById(R.id.fab_filter);
        recyclerView.setAdapter(productListAdapter);
        progressDialog = new ProgressDialog(this);
    }

    private void setupDefault() {
        if (getIntent() != null && getIntent().hasExtra(Constants.COMPANY_INFO)) {
            CompanyInfoPublic companyInfo = (CompanyInfoPublic) getIntent().getSerializableExtra(Constants.COMPANY_INFO);
            companyKey = companyInfo.location;
            setTitle(TextUtils.capitalizeFirstLetter(companyInfo.name) + " Product's");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fabFilter.setVisibility(View.GONE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        getProductListFromPublicDataBase();
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
                        if (productInfo.company_info.location.equals(companyKey)) {
                            productInfoArrayList.add(productInfo);
                        }
                    }
                }
                productListAdapter.updateList(productInfoArrayList);
                removeListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", "onCancelled" + databaseError);
            }
        };

        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).addListenerForSingleValueEvent(firstValueListener);

    }

    private void removeListener() {
        FirebaseDatabase.getInstance().getReference().removeEventListener(firstValueListener);
    }

    private void setupEvent() {

    }

    @Override
    public void openChat(ProductInfo productInfo) {
        startActivity(new Intent(this, ChatActivity.class).putExtra("Product_id", productInfo));
    }

    @Override
    public void callToUser(ProductInfo productInfo) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + productInfo.company_info.phone));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    @Override
    public void onSetRatingToProducts(int rating, ProductInfo productInfo) {
        progressDialog.setMessage("Updating rating...");
        progressDialog.show();
        getRatingFromTheDb(productInfo, false, rating);
    }

    @Override
    public int onGetRatingFromFirebase(ProductInfo productInfo) {
        getRatingFromTheDb(productInfo, true, 0);
        return userRating;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PHONE_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                }
                break;
        }
    }

    private void setRatingToTheDb(final int rating, final ProductInfo productInfo) {

        FirebaseDatabase.getInstance().getReference().removeEventListener(setRatingListener);

        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).child(productInfo.productID).child("userRating").child(getApp().getFireBaseAuth().getCurrentUser().getUid()).setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    boolean isRated = false;

                    if(userRating != 0)
                        isRated = true;

                    if (!isRated) {
                        callRatingNofPerson(productInfo.ratingNoOfPerson, productInfo, rating);
                    } else {
                        putOverallRatingToDb(productInfo, rating);
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProductListActivity.this, "Error occurred while accessing into database,please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void callRatingNofPerson(int ratingNoOfPerson, final ProductInfo productInfo, final int rating) {

        int finalRatingNoOfPerson = ratingNoOfPerson + 1;

        FirebaseDatabase.getInstance()
                .getReference().child(Constants.PRODUCT_INFO).child(productInfo.productID).child("ratingNoOfPerson").setValue(finalRatingNoOfPerson).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    putOverallRatingToDb(productInfo, rating);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProductListActivity.this, "Error occurred while accessing into database,please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void putOverallRatingToDb(ProductInfo productInfo, int rating) {

        int finalOverallRating = rating + productInfo.overAllRating;

        FirebaseDatabase.getInstance().getReference().child(Constants.PRODUCT_INFO).child(productInfo.productID).child("overAllRating").setValue(finalOverallRating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    productListAdapter.clearList();
                    getProductListFromPublicDataBase();
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProductListActivity.this, "Error occurred while accessing into database,please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ValueEventListener getRatingListener;

    private void getRatingFromTheDb(final ProductInfo productInfo, final boolean isToGetRating, final int rating) {


        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.PRODUCT_INFO).child(productInfo.productID);

        getRatingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("userRating")) {
                    decideToSetRating(databaseReference,isToGetRating,rating,productInfo);
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addListenerForSingleValueEvent(getRatingListener);


    }

    ValueEventListener setRatingListener;

    private void decideToSetRating(DatabaseReference databaseReference, final boolean isToGetRating, final int rating,final ProductInfo productInfo) {

        FirebaseDatabase.getInstance().getReference().removeEventListener(getRatingListener);

        setRatingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userRating = Integer.parseInt("" + dataSnapshot.getValue());
                if(!isToGetRating)
                    setRatingToTheDb(rating,productInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.child("userRating").child(getApp().getFireBaseAuth().getCurrentUser().getUid()).addListenerForSingleValueEvent(setRatingListener);

    }

    private void getRatingArrayList(){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.PRODUCT_INFO);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
