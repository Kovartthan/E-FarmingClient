package com.ko.efarmingclient.home.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ko.efarmingclient.R;
import com.ko.efarmingclient.listener.OnChatOpenListener;
import com.ko.efarmingclient.model.ProductInfo;
import com.ko.efarmingclient.util.TextUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class ProductListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<ProductInfo> productInfoArrayList;
    private OnChatOpenListener onChatOpenListener;

    public void  setOnChatOpenListener(OnChatOpenListener onChatOpenListener){
        this.onChatOpenListener = onChatOpenListener;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ProductInfo productInfo = productInfoArrayList.get(position);
        if (!TextUtils.isEmpty(productInfo.imageUrl)) {
            Picasso.with(context).load(productInfo.imageUrl).into(((ProductItemHolder) holder).imgProduct);
        }
        ((ProductItemHolder) holder).txtProductName.setText(TextUtils.capitalizeFirstLetter(productInfo.productName));
        ((ProductItemHolder) holder).txtProductPrice.setText("Rs " + productInfo.productPrice);
        ((ProductItemHolder) holder).txtProductQuantity.setText("Available units : " + productInfo.productQuantity);
        ((ProductItemHolder) holder).txtRequestProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChatOpenListener.openChat(productInfo);
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

    public class ProductItemHolder extends RecyclerView.ViewHolder {


        private ImageView imgProduct;
        private TextView txtProductName;
        private TextView txtProductPrice;
        private TextView txtProductQuantity;
        private TextView txtRequestProduct;

        public ProductItemHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtProductPrice = itemView.findViewById(R.id.txt_product_price);
            txtProductQuantity = itemView.findViewById(R.id.txt_product_quantity);
            txtRequestProduct = itemView.findViewById(R.id.txt_req_chat);

        }

    }
}
