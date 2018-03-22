package com.ko.efarmingclient.listener;

import com.ko.efarmingclient.model.ProductInfo;

/**
 * Created by NEW on 3/9/2018.
 */

public interface OnProductInfoOpenListener {
    void openChat(ProductInfo productInfo);
    void callToUser(ProductInfo productInfo);
    void onSetRatingToProducts(int rating ,ProductInfo productInfo);
}
