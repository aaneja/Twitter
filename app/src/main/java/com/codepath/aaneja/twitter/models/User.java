package com.codepath.aaneja.twitter.models;

import com.raizlabs.android.dbflow.annotation.Column;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

/**
 * Created by aaneja on 01/04/17.
 */

@Parcel(analyze={User.class})
public class User {
    @Column
    String userScreenName;
    @Column
    String userName;
    @Column
    String userProfileUrl;

    public String getUserScreenName() {
        return userScreenName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public User() {
    }

    public User(JSONObject user){
        super();

        try {
            this.userScreenName = user.getString("screen_name");
            this.userName = user.getString("name");
            this.userProfileUrl = user.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
