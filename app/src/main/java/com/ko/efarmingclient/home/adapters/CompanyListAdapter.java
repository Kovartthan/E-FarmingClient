package com.ko.efarmingclient.home.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ko.efarmingclient.R;
import com.ko.efarmingclient.home.activities.ProductListActivity;
import com.ko.efarmingclient.listener.OnProductInfoOpenListener;
import com.ko.efarmingclient.model.CompanyInfoPublic;
import com.ko.efarmingclient.util.Constants;
import com.ko.efarmingclient.util.TextUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by NEW on 3/18/2018.
 */

public class CompanyListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private Context context;
    private ArrayList<CompanyInfoPublic> companyInfoPublicArrayList;

    public CompanyListAdapter(Context context, ArrayList<CompanyInfoPublic> companyInfoPublicArrayList) {
        this.context = context;
        this.companyInfoPublicArrayList = companyInfoPublicArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_company_item, parent, false);
        return new CompanyItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final CompanyInfoPublic companyInfoPublic = companyInfoPublicArrayList.get(position);
        ((CompanyItemHolder) holder).txtCompanyName.setText(TextUtils.capitalizeFirstLetter(companyInfoPublic.name));
        ((CompanyItemHolder) holder).txtCompanyLocation.setText(TextUtils.capitalizeFirstLetter(companyInfoPublic.city));
        if (!TextUtils.isEmpty(companyInfoPublic.photoUrl)) {
            ((CompanyItemHolder) holder).txtPreviewText.setText("Loading Image");
            Picasso.get().load(companyInfoPublic.photoUrl).resize(300,300).into(((CompanyItemHolder) holder).imgProduct, new Callback() {
                @Override
                public void onSuccess() {
                    ((CompanyItemHolder) holder).txtPreviewText.setText("");
                }

                @Override
                public void onError(Exception e) {
                    ((CompanyItemHolder) holder).txtPreviewText.setText("Error occurred");
                }
            });
        }
        ((CompanyItemHolder) holder).txtExploreProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context,ProductListActivity.class).putExtra(Constants.COMPANY_INFO,companyInfoPublic));
            }
        });
    }

    @Override
    public int getItemCount() {
        return companyInfoPublicArrayList.size();
    }

    public void updateList(ArrayList<CompanyInfoPublic> companyInfoPublicArrayList) {
        this.companyInfoPublicArrayList = companyInfoPublicArrayList;
        notifyDataSetChanged();
    }

    public void clearList() {
        this.companyInfoPublicArrayList.clear();
        notifyDataSetChanged();
    }

    public class CompanyItemHolder extends RecyclerView.ViewHolder {

        private ImageView imgProduct;
        private TextView txtCompanyName;
        private TextView txtCompanyLocation;
        private TextView txtExploreProduct;
        private TextView txtPreviewText;
        public CompanyItemHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtCompanyName = itemView.findViewById(R.id.txt_company_name);
            txtCompanyLocation = itemView.findViewById(R.id.txt_company_loc);
            txtExploreProduct = itemView.findViewById(R.id.txt_explore_products);
            txtPreviewText = itemView.findViewById(R.id.txt_preview);
        }

    }
}
