package com.codepath.aaneja.twitter.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.aaneja.twitter.ComposeTweetActivity;
import com.codepath.aaneja.twitter.ProfileActivity;
import com.codepath.aaneja.twitter.R;
import com.codepath.aaneja.twitter.RestApplication;
import com.codepath.aaneja.twitter.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.aaneja.twitter.adapters.TweetItemAdapter;
import com.codepath.aaneja.twitter.helpers.ItemClickSupport;
import com.codepath.aaneja.twitter.models.Tweet;
import com.codepath.aaneja.twitter.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.media.CamcorderProfile.get;

public class TimelineFragment extends Fragment {

    private static final String APITOSET = "APITOSET";
    private static final String USERIDTOGET = "USERIDTOGET";
    private static final String PROFILELOADSWITCH = "PROFILELOADSWITCH";
    private long userIdToFetch = 0;
    private TwitterRestClient.API apiToSet;

    private static final int REQUEST_CODE_COMPOSE = 1;
    private HashMap<Integer, Long> pageToMaxIdMap = new HashMap<>();
    private ArrayList<Tweet> fetchedTweets = new ArrayList<>();
    private TweetItemAdapter tweetItemAdapter;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    private TwitterRestClient twitterClient = RestApplication.getRestClient();
    private RecyclerView rvTweets;
    private boolean loadProfileOnItemClick = true;

    public TimelineFragment() {
    }

    /**
     * @param apiToSet The @TwitterRestClient.API to call using this fragment
     * @param userIdToGet The user_id of the twitter user to fetch. Can be 0 to represent the currently logged in user
     * @param loadProfileOnItemClick A flag to control if the ProfileActivity should be called on clicking the item in the displayed Timeline
     * @return
     */
    public static TimelineFragment newInstance(TwitterRestClient.API apiToSet, long userIdToGet, boolean loadProfileOnItemClick) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putSerializable(APITOSET, apiToSet);
        args.putLong(USERIDTOGET,userIdToGet);
        args.putBoolean(PROFILELOADSWITCH,loadProfileOnItemClick);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            apiToSet = (TwitterRestClient.API) getArguments().getSerializable(APITOSET);
            userIdToFetch = getArguments().getLong(USERIDTOGET);
            loadProfileOnItemClick = getArguments().getBoolean(PROFILELOADSWITCH);
            Log.d("NEW_FRAGMENT","apiToSet: "+String.valueOf(apiToSet)+" userIdToFetch: "+ userIdToFetch+ " loadProfileOnItemClick: "+ loadProfileOnItemClick);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tweetItemAdapter = new TweetItemAdapter(fetchedTweets);
        rvTweets = (RecyclerView) view.findViewById(R.id.rvTweets);
        rvTweets.setAdapter(tweetItemAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);

        if(loadProfileOnItemClick) {
            ItemClickSupport.addTo(rvTweets).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    Tweet tweet = fetchedTweets.get(position);
                    long userIdForProfileLoad = tweet.getUser().getUserId();
                    Intent i = new Intent(TimelineFragment.this.getContext(), ProfileActivity.class);
                    i.putExtra(ProfileActivity.USERINFO, userIdForProfileLoad);
                    startActivity(i);
                }
            });
        }


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

                FetchNextPageofTweets(newPage, prevMaxId);
            }
        };
        rvTweets.addOnScrollListener(endlessRecyclerViewScrollListener);

        FetchNextPageofTweets(endlessRecyclerViewScrollListener.getCurrentPage(),0);
    }

    private void FetchNextPageofTweets(final int newPageToFetch, final long prevPageMinId) {
        //We need older tweets, so we fetch tweets less than the min of the previously seen id's
        twitterClient.getTimeLine(prevPageMinId -1, apiToSet, userIdToFetch, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                Log.d("NEWTWEETS/fetched", "count: " + jsonArray.length());
                final ArrayList<Tweet> newTweets = Tweet.fromJson(jsonArray);
                SetPageToMaxIdMapping(newPageToFetch, newTweets);

                //Since this is a new load ensure we only notify new range
                final int beforeAddCount = fetchedTweets.size();
                fetchedTweets.addAll(newTweets);
                tweetItemAdapter.notifyItemRangeInserted(beforeAddCount,newTweets.size());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(TimelineFragment.this.getContext(),String.format("Error getting tweets for Page: %d, StatusCode : %d, ExceptionText: %s",newPageToFetch, statusCode,throwable.getMessage()),Toast.LENGTH_LONG);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }


    public void newTweetPosted(Tweet newTweet) {
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

    private void SetPageToMaxIdMapping(int currentPage, List<Tweet> newTweets) {
        if(newTweets.size() == 0) {
            Log.w("NEWTWEETS", "No new tweets fetched. No mapping will be set for: " + String.valueOf(currentPage));
        }
        long max_id = getMaxId(newTweets);
        Log.d("NEWTWEETS", "SetPageToMaxIdMapping: (" + String.valueOf(currentPage) +"," +String.valueOf(max_id) +")");
        pageToMaxIdMap.put(currentPage,max_id);
    }

    //Get the smalled id from a List of Tweets; this is used to fetch older tweets from a timeline
    private long getMaxId(List<Tweet> tweets) {
        if(tweets.size() == 0) {
            return -1;
        }
        long max_id = tweets.get(0).getLongId();
        for (Tweet tweet :
                tweets) {
            //We need to find the smallest id of the set; this becomes the 'max_id'
            max_id = tweet.getLongId() <  max_id ? tweet.getLongId() : max_id;
        }
        return max_id;
    }
}
