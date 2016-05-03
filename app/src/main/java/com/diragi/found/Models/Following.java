package com.diragi.found.Models;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Created by joe on 4/30/16.
 */
public class Following {

    private String userName;
    private String fullName;
    private String userEmail;
    private String followID;
    private String userIcon;

    public Following() {

    }

    public Following(String user) {
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getFollowID() {
        return followID;
    }

    public String getUserIcon() {
        return userIcon;
    }
}
