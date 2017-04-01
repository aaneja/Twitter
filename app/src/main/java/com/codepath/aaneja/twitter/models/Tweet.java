package com.codepath.aaneja.twitter.models;

/**
 * Created by aaneja on 23/03/17.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import com.codepath.aaneja.twitter.MyDatabase;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.ArrayList;


@Parcel(analyze={Tweet.class})
@Table(database = MyDatabase.class)
public class Tweet extends BaseModel {
    @PrimaryKey
    String id;
    @Column
    long longId;


    @Column
    String userScreenName;
    @Column
    String userName;
    @Column
    String userProfileUrl;
    @Column
    String timestamp;
    @Column
    String body;



    public String getUserScreenName() {
        return userScreenName;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public String getId() {
        return id;
    }

    public long getLongId() {
        return longId;
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
            this.longId =  object.getLong("id");
            this.id = object.getString("id_str");
            this.timestamp = object.getString("created_at");
            this.body = object.getString("text");

            //First get the inner 'user' object
            JSONObject user = object.getJSONObject("user");
            this.userScreenName = user.getString("screen_name");
            this.userName = user.getString("name");
            this.userProfileUrl = user.getString("profile_image_url");
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
            //tweet.save();
            tweets.add(tweet);
        }

        return tweets;
    }
}



