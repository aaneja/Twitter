package com.codepath.aaneja.twitter;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.aaneja.twitter.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.aaneja.twitter.adapters.TweetItemAdapter;
import com.codepath.aaneja.twitter.fragments.TimelineFragment;
import com.codepath.aaneja.twitter.models.Tweet;
import com.codepath.aaneja.twitter.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class TimelineActivity extends AppCompatActivity implements TimelineFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_COMPOSE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.phTimeline, new TimelineFragment());
        ft.commit();
    }

    public void onComposeAction(MenuItem item) {
        Intent i = new Intent(TimelineActivity.this, ComposeTweetActivity.class);
        startActivityForResult(i, REQUEST_CODE_COMPOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
       if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_COMPOSE) {
           /*// Extract name value from result extras
           Tweet newTweet = (Tweet) Parcels.unwrap(data.getExtras().getParcelable(ComposeTweetActivity.NEW_TWEET));
           Log.d("NEWTWEET", "TimelineActivity/NewTweet/Id : " +newTweet.getId());

           //Add in the new item
           fetchedTweets.add(0,newTweet);
           //Now we can notify the adapter of the change
           tweetItemAdapter.notifyItemInserted(0);
           rvTweets.scrollToPosition(0);

           //The act of adding a new item messes up state in the endlessRecyclerViewScrollListener. We reset its state and clear the dictionary that defines pages to max_id mappings
           endlessRecyclerViewScrollListener.resetState();
           pageToMaxIdMap.clear();
           SetPageToMaxIdMapping(endlessRecyclerViewScrollListener.getCurrentPage(),fetchedTweets);*/
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
