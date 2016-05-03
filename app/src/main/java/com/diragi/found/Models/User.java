package com.diragi.found.Models;

import android.graphics.drawable.Drawable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Created by joe on 4/28/16.
 */
public class User {
    String fullName;
    String userName;
    String userEmail;
    String uid;
    String userIcon;
    @JsonIgnore
    Following following;

    public User() {

    }

    public String getFullName() {
        return fullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public String getUid() {
        return uid;
    }
}
