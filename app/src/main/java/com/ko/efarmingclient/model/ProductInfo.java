package com.ko.efarmingclient.model;

import java.io.Serializable;

/**
 * Created by admin on 3/5/2018.
 */

public class ProductInfo implements Serializable {
    public String productName;
    public String productQuantity;
    public String productPrice;
    public String imageUrl;
    public String productID;
    public UserInfo user_info;
    public CompanyInfoPublic company_info;
    public int ratingNoOfPerson;
    public int overAllRating;
    public ProductInfo(){

    }
    public ProductInfo(String productName, String productQuantity, String productPrice,String imageUrl,String productID,UserInfo user) {
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.imageUrl = imageUrl;
        this.productID = productID;
        this.user_info = user;
    }
}
