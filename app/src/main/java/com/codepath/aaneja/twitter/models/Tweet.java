package com.codepath.aaneja.twitter.models;

/**
 * Created by aaneja on 23/03/17.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codepath.aaneja.twitter.MyDatabase;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.ArrayList;


@Table(database = MyDatabase.class)
public class Tweet extends BaseModel {
    // Define database columns and associated fields
    @PrimaryKey @Column
    Long id;
    @Column
    String userId;
    @Column
    String userHandle;
    @Column
    String timestamp;
    @Column
    String body;

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getBody() {
        return body;
    }

    public Tweet() {
    }

    // Add a constructor that creates an object from the JSON response
    public Tweet(JSONObject object){
        super();

        try {
            this.userId = object.getString("user_id");
            this.userHandle = object.getString("user_username");
            this.timestamp = object.getString("timestamp");
            this.body = object.getString("body");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            Tweet tweet = new Tweet(tweetJson);
            tweet.save();
            tweets.add(tweet);
        }

        return tweets;
    }
}


