package com.ko.efarmingclient.home.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ko.efarmingclient.R;
import com.ko.efarmingclient.listener.OnProductInfoOpenListener;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.util.Constants;
import com.ko.efarmingclient.util.TextUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.ko.efarmingclient.EFApp.getApp;


public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<ProductInfo> productInfoArrayList;
    private OnProductInfoOpenListener onProductInfoOpenListener;

    public void setOnProductInfoOpenListener(OnProductInfoOpenListener onProductInfoOpenListener) {
        this.onProductInfoOpenListener = onProductInfoOpenListener;
    }

    public ProductListAdapter(Context context, ArrayList<ProductInfo> productInfoArrayList) {
        this.context = context;
        this.productInfoArrayList = productInfoArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_product_item, parent, false);
        return new ProductItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ProductInfo productInfo = productInfoArrayList.get(position);
        if (!TextUtils.isEmpty(productInfo.imageUrl)) {
            ((ProductItemHolder) holder).txtPreviewText.setText("Loading Image");
            Picasso.get().load(productInfo.imageUrl).resize(300, 300).into(((ProductItemHolder) holder).imgProduct, new Callback() {
                @Override
                public void onSuccess() {
                    ((ProductItemHolder) holder).txtPreviewText.setText("");
                }

                @Override
                public void onError(Exception e) {
                    ((ProductItemHolder) holder).txtPreviewText.setText("Error occurred");
                }
            });
        }
        ((ProductItemHolder) holder).txtProductName.setText(TextUtils.capitalizeFirstLetter(productInfo.productName));
        ((ProductItemHolder) holder).txtProductPrice.setText("Rs " + productInfo.productPrice);
        ((ProductItemHolder) holder).txtProductQuantity.setText("Available units : " + productInfo.productQuantity);
        int userRating = onProductInfoOpenListener.onGetRatingFromFirebase(productInfo);
        if (userRating != 0) {
            ((ProductItemHolder) holder).ratingBar.setRating(userRating);
        }

        if (productInfo.overAllRating != 0 && productInfo.ratingNoOfPerson != 0) {
            int rating = Math.round(productInfo.overAllRating / productInfo.ratingNoOfPerson);
            ((ProductItemHolder) holder).overallRatingBar.setRating(rating);
        }
        ((ProductItemHolder) holder).txtRequestProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProductInfoOpenListener.openChat(productInfo);
            }
        });
        ((ProductItemHolder) holder).txtCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProductInfoOpenListener.callToUser(productInfo);
            }
        });
        ((ProductItemHolder) holder).ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                onProductInfoOpenListener.onSetRatingToProducts((int) ratingBar.getRating(), productInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productInfoArrayList.size();
    }

    public void updateList(ArrayList<ProductInfo> productInfoArrayList) {
        this.productInfoArrayList = productInfoArrayList;
        notifyDataSetChanged();
    }

    public void clearList() {
        this.productInfoArrayList.clear();
        notifyDataSetChanged();
    }

    public class ProductItemHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductName;
        private TextView txtProductPrice;
        private TextView txtProductQuantity;
        private LinearLayout txtRequestProduct, txtCall;
        private TextView txtPreviewText;
        private RatingBar ratingBar;
        private RatingBar overallRatingBar;

        public ProductItemHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtProductPrice = itemView.findViewById(R.id.txt_product_price);
            txtProductQuantity = itemView.findViewById(R.id.txt_product_quantity);
            txtRequestProduct = itemView.findViewById(R.id.txt_req_chat);
            txtCall = itemView.findViewById(R.id.txt_call);
            txtPreviewText = itemView.findViewById(R.id.txt_preview);
            ratingBar = itemView.findViewById(R.id.rb_star);
            overallRatingBar = itemView.findViewById(R.id.rb_overall_star);
        }

    }
//
//    ValueEventListener valueEventListener,setRatingListener;
//
//    private void getRatingFromTheDb(final ProductInfo productInfo, ProductItemHolder holder) {
//
//
//        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.PRODUCT_INFO).child(productInfo.productID);
//
//        valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.hasChild("userRating")) {
//                    decideToSetRating(databaseReference,productInfo);
//                } else {
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
//        databaseReference.addListenerForSingleValueEvent(valueEventListener);
//
//    }
//
//    private void decideToSetRating(DatabaseReference databaseReference,final ProductInfo productInfo) {
//
//        FirebaseDatabase.getInstance().getReference().removeEventListener(valueEventListener);
//
//        setRatingListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                int userRating = Integer.parseInt("" + dataSnapshot.getValue());
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
//        databaseReference.child("userRating").child(getApp().getFireBaseAuth().getCurrentUser().getUid()).addListenerForSingleValueEvent(setRatingListener);
//
//    }

}
