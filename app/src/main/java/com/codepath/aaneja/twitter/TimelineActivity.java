package com.codepath.aaneja.twitter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.aaneja.twitter.adapters.TweetItemAdapter;
import com.codepath.aaneja.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;


public class TimelineActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_COMPOSE = 1;
    private HashMap<Integer, Long> pageToMaxIdMap = new HashMap<>();
    private ArrayList<Tweet> fetchedTweets = new ArrayList<>();
    private TweetItemAdapter tweetItemAdapter;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    private TwitterRestClient twitterClient = RestApplication.getRestClient();
    private RecyclerView rvTweets;

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


        tweetItemAdapter = new TweetItemAdapter(fetchedTweets);
        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        rvTweets.setAdapter(tweetItemAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);


        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(final int newPage, int totalItemsCount, RecyclerView view) {
                Log.d("NEWTWEETS", "Requesting page:  "+ String.valueOf(newPage));
                if (!pageToMaxIdMap.containsKey(newPage-1)) {
                    //We don't have the max_id from the last page, something is wrong.
                    Log.e("NEWTWEETS/Exception", "No max_id mapping exists for page: " + String.valueOf(newPage-1) + "cannot fetch new page #:" +String.valueOf(newPage));
                    return;
                }
                long prevMaxId = pageToMaxIdMap.get(newPage-1);
                Log.d("NEWTWEETS", "Previous max_id: "+ String.valueOf(prevMaxId));
                //We need older tweets, so we fetch tweets less than the min of the previously seen id's
                twitterClient.getHomeTimeline(prevMaxId-1, new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                        Log.d("NEWTWEETS/fetched", "count: " + jsonArray.length());
                        final ArrayList<Tweet> newTweets = Tweet.fromJson(jsonArray);
                        SetPageToMaxIdMapping(newPage, newTweets);

                        //Since this is a new load ensure we only notify new range
                        final int beforeAddCount = fetchedTweets.size();
                        fetchedTweets.addAll(newTweets);
                        tweetItemAdapter.notifyItemRangeInserted(beforeAddCount,newTweets.size());
                    }
                });
            }
        };
        rvTweets.addOnScrollListener(endlessRecyclerViewScrollListener);

        twitterClient.getHomeTimeline(-1, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                Log.d("DEBUG", "timeline: " + jsonArray.toString());
                final ArrayList<Tweet> newTweets = Tweet.fromJson(jsonArray);

                SetPageToMaxIdMapping(endlessRecyclerViewScrollListener.getCurrentPage(), newTweets);

                //Since first load of the timeline, clear and full notify
                fetchedTweets.clear();
                fetchedTweets.addAll(newTweets);
                tweetItemAdapter.notifyDataSetChanged();
            }
        });

    }

    private void SetPageToMaxIdMapping(int currentPage, List<Tweet> newTweets) {
        long max_id = getMaxId(newTweets);
        Log.d("NEWTWEETS", "SetPageToMaxIdMapping: (" + String.valueOf(currentPage) +"," +String.valueOf(max_id) +")");
        pageToMaxIdMap.put(currentPage,max_id);
    }

    //Get the smalled id from a List of Tweets; this is used to fetch older tweets from a timeline
    private long getMaxId(List<Tweet> tweets) {
        long max_id = tweets.get(0).getLongId();
        for (Tweet tweet :
                tweets) {
            //We need to find the smallest id of the set; this becomes the 'max_id'
            max_id = tweet.getLongId() <  max_id ? tweet.getLongId() : max_id;
        }
        return max_id;
    }

    public void onComposeAction(MenuItem item) {
        Intent i = new Intent(TimelineActivity.this, ComposeTweetActivity.class);
        i.putExtra("mode", 2); // pass arbitrary data to launched activity
        startActivityForResult(i, REQUEST_CODE_COMPOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
       if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_COMPOSE) {
           // Extract name value from result extras
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
           SetPageToMaxIdMapping(endlessRecyclerViewScrollListener.getCurrentPage(),fetchedTweets);
        }
    }
}
