package com.codepath.aaneja.twitter;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.codepath.aaneja.twitter.databinding.ActivityComposeTweetBinding;
import com.codepath.aaneja.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeTweetActivity extends AppCompatActivity {

    public static final String NEW_TWEET = "newTweet";
    private TwitterRestClient twitterClient = RestApplication.getRestClient();
    private ActivityComposeTweetBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding =  DataBindingUtil.setContentView(this, R.layout.activity_compose_tweet);
//        setContentView(R.layout.activity_compose_tweet);
    }

    public void onCancelClick(View view) {
        setResult(RESULT_CANCELED); // set result code and bundle data for response
        finish();
    }

    public void onPublishTweetClick(View view) {
        final String tweetText = binding.etTweetText.getText().toString();
        twitterClient.postTweet(tweetText, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //TODO:Check statusCode first
                final Tweet postedTweet = new Tweet(response);
                Log.d("POSTTWEET","New tweet id:" + postedTweet.getId());

                // Prepare data intent
                Intent data = new Intent();
                // Pass relevant data back as a result
                data.putExtra(NEW_TWEET, Parcels.wrap(postedTweet));
                // Activity finished ok, return the data
                setResult(RESULT_OK, data); // set result code and bundle data for response
                finish(); // closes the activity, pass data to parent
            }
        } );

    }
}
