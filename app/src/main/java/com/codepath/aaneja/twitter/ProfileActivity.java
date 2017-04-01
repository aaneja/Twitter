package com.codepath.aaneja.twitter;

import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.aaneja.twitter.fragments.TimelineFragment;
import com.codepath.aaneja.twitter.models.User;
import com.codepath.aaneja.twitter.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity implements TimelineFragment.OnFragmentInteractionListener {

    public static final String USERINFO = "USERINFO";
    private TwitterRestClient twitterClient = RestApplication.getRestClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final long userIdToFetch = getIntent().getLongExtra(USERINFO,0);

        twitterClient.getUser(userIdToFetch, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("ProfileActivity", "Fetched details for userId : "+ userIdToFetch);

                User user = new User(response);
                ((TextView)findViewById(R.id.profile_tvScreenName)).setText(user.getScreenName());
                ((TextView)findViewById(R.id.profile_tvName)).setText(user.getFullName());
                ((TextView)findViewById(R.id.tvFollowersCount)).setText(String.valueOf(user.getFollowersCount()));
                ((TextView)findViewById(R.id.tvFollowingCount)).setText(String.valueOf(user.getFollowingCount()));
                ImageView ivProfile = (ImageView)findViewById(R.id.profile_ivProfile);
                Picasso.with(ivProfile.getContext()).load(user.getProfileImageUrl()).fit().centerCrop().into(ivProfile);

                //And replace the Frame with the user's timeline
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.profile_frUserTimeline, TimelineFragment.newInstance(TwitterRestClient.API.USER_TIMELINE, userIdToFetch));
                ft.commit();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("ProfileActivity", "Failure statusCode : "+statusCode);
                Log.e("ProfileActivity", "Failure JSON: "+ errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
