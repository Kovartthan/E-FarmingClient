package com.ko.efarmingclient.model;

/**
 * Created by admin on 3/23/2018.
 */

public class UserRating {
    public String productID;
    public int rating;

    public UserRating(String productID, int rating) {
        this.productID = productID;
        this.rating = rating;
    }
}
