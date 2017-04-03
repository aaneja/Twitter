package com.codepath.aaneja.twitter;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.aaneja.twitter.databinding.ActivityComposeTweetBinding;
import com.codepath.aaneja.twitter.models.Tweet;
import com.codepath.aaneja.twitter.network.TwitterRestClient;
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
    }

    public void onCancelClick(View view) {
        setResult(RESULT_CANCELED);
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

                Intent data = new Intent();
                data.putExtra(NEW_TWEET, Parcels.wrap(postedTweet));
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(ComposeTweetActivity.this,String.format("Error posting tweet : StatusCode : %d, ExceptionText: %s",statusCode,throwable.getMessage()),Toast.LENGTH_LONG);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        } );

    }
}
