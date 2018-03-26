package com.ko.efarmingclient.home.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ko.efarmingclient.R;
import com.ko.efarmingclient.listener.OnProductInfoOpenListener;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.model.UserRating;
import com.ko.efarmingclient.util.TextUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<ProductInfo> productInfoArrayList;
    private OnProductInfoOpenListener onProductInfoOpenListener;
    private ArrayList<UserRating> ratingArrayList;
    private boolean isRated = false;

    public void setOnProductInfoOpenListener(OnProductInfoOpenListener onProductInfoOpenListener) {
        this.onProductInfoOpenListener = onProductInfoOpenListener;
    }

    public ProductListAdapter(Context context, ArrayList<ProductInfo> productInfoArrayList, ArrayList<UserRating> ratingArrayList) {
        this.context = context;
        this.productInfoArrayList = productInfoArrayList;
        this.ratingArrayList = ratingArrayList;
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
        if(ratingArrayList != null) {
            for (UserRating userRating : ratingArrayList) {
                if (productInfo.productID.equals(userRating.productID)) {
                    if (userRating.rating != 0)
                        ((ProductItemHolder) holder).ratingBar.setRating(userRating.rating);
                }
            }
        }
//        int userRating = onProductInfoOpenListener.onGetRatingFromFirebase(productInfo);
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

        ((ProductItemHolder) holder).ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float touchPositionX = event.getX();
                    float width = ((ProductItemHolder) holder).ratingBar.getWidth();
                    float starsf = (touchPositionX / width) * 5.0f;
                    int stars = (int)starsf + 1;
                    ((ProductItemHolder) holder).ratingBar.setRating(stars);
                    onProductInfoOpenListener.onSetRatingToProducts((int)    ((ProductItemHolder) holder).ratingBar.getRating(), productInfo);
                    v.setPressed(false);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                }

                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setPressed(false);
                }
                return true;
            }});

    }

    @Override
    public int getItemCount() {
        return productInfoArrayList.size();
    }

    public void updateList(ArrayList<ProductInfo> productInfoArrayList, ArrayList<UserRating> ratingArrayList) {
        this.productInfoArrayList = productInfoArrayList;
        this.ratingArrayList = ratingArrayList;
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


}
