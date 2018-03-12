package com.ko.efarmingclient.model;

import java.io.Serializable;

/**
 * Created by NEW on 3/10/2018.
 */

public class UserInfo implements Serializable {
    public String uid;
    public String email;
    public String name;
    public String firebaseToken;
    public String userImage;
    public boolean isCompanyProfileUpdated;
    public UserInfo() {

    }
}
