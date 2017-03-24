package com.codepath.aaneja.twitter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

import com.codepath.aaneja.twitter.adapters.TweetItemAdapter;
import com.codepath.aaneja.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class TimelineActivity extends AppCompatActivity {

    private ArrayList<Tweet> fetchedTweets = new ArrayList<>();
    private TweetItemAdapter tweetItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);


        tweetItemAdapter = new TweetItemAdapter(fetchedTweets);
        RecyclerView rvTweets =  (RecyclerView) findViewById(R.id.rvTweets);
        rvTweets.setAdapter(tweetItemAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);

        final TwitterRestClient client = RestApplication.getRestClient();
        client.getHomeTimeline(1, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                Log.d("DEBUG", "timeline: " + jsonArray.toString());
                fetchedTweets.clear();
                fetchedTweets.addAll(Tweet.fromJson(jsonArray));
                tweetItemAdapter.notifyDataSetChanged();
            }
        });

    }
}
