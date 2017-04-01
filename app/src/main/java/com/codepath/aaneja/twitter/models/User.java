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

    long userId;
    @Column
    String screenName;
    @Column
    String fullName;
    @Column
    String profileImageUrl;

    @Column
    int followersCount;
    @Column
    int followingCount;

    public long getUserId() {
        return userId;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public User() {
    }

    public User(JSONObject user){
        super();

        try {
            this.userId = user.getLong("id");
            this.screenName = user.getString("screen_name");
            this.fullName = user.getString("name");
            this.profileImageUrl = user.getString("profile_image_url");
            this.followersCount = user.getInt("followers_count");
            this.followingCount = user.getInt("friends_count");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
