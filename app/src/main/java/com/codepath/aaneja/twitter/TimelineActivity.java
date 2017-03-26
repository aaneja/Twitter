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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class TimelineActivity extends AppCompatActivity {

    private HashMap<Integer, Long> pageToMaxIdMap = new HashMap<>();
    private ArrayList<Tweet> fetchedTweets = new ArrayList<>();
    private TweetItemAdapter tweetItemAdapter;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    private TwitterRestClient twitterClient;

    public TimelineActivity() {
        twitterClient = RestApplication.getRestClient();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);


        tweetItemAdapter = new TweetItemAdapter(fetchedTweets);
        RecyclerView rvTweets =  (RecyclerView) findViewById(R.id.rvTweets);
        rvTweets.setAdapter(tweetItemAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);


        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(final int newPage, int totalItemsCount, RecyclerView view) {
                Log.d("NEWTWEETS", "Requesting page:  "+ String.valueOf(newPage));
                if (!pageToMaxIdMap.containsKey(newPage-1)) {
                    //We don't have the max_id from the last page, something is wrong
                    Log.d("NEWTWEETS/Exception", "No max_id mapping exists for page: " + String.valueOf(newPage-1));
                }
                long prevMaxId = pageToMaxIdMap.get(newPage-1);
                Log.d("NEWTWEETS", "Previous max_id: "+ String.valueOf(prevMaxId));
                twitterClient.getHomeTimeline(prevMaxId+1, new JsonHttpResponseHandler() {
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
}
