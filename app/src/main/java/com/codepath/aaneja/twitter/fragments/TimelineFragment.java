package com.codepath.aaneja.twitter.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.aaneja.twitter.R;
import com.codepath.aaneja.twitter.RestApplication;
import com.codepath.aaneja.twitter.adapters.EndlessRecyclerViewScrollListener;
import com.codepath.aaneja.twitter.adapters.TweetItemAdapter;
import com.codepath.aaneja.twitter.models.Tweet;
import com.codepath.aaneja.twitter.network.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineFragment extends Fragment {

    private static final String APITOSET = "APITOSET";
    private static final String USERIDTOGET = "USERIDTOGET";
    private long userIdToFetch = 0;
    private TwitterRestClient.API apiToSet;
    private OnFragmentInteractionListener mListener;

    private static final int REQUEST_CODE_COMPOSE = 1;
    private HashMap<Integer, Long> pageToMaxIdMap = new HashMap<>();
    private ArrayList<Tweet> fetchedTweets = new ArrayList<>();
    private TweetItemAdapter tweetItemAdapter;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    private TwitterRestClient twitterClient = RestApplication.getRestClient();
    private RecyclerView rvTweets;

    public TimelineFragment() {
    }

    public static TimelineFragment newInstance(TwitterRestClient.API apiToSet, long userIdToGet) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putSerializable(APITOSET, apiToSet);
        args.putLong(USERIDTOGET,userIdToGet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            apiToSet = (TwitterRestClient.API) getArguments().getSerializable(APITOSET);
            userIdToFetch = getArguments().getLong(USERIDTOGET);
            Log.d("NEW_FRAGMENT","apiToSet: "+String.valueOf(apiToSet)+" userIdToFetch: "+ userIdToFetch);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        tweetItemAdapter = new TweetItemAdapter(fetchedTweets);
        rvTweets = (RecyclerView) view.findViewById(R.id.rvTweets);
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
                twitterClient.getTimeLine(prevMaxId-1, apiToSet, userIdToFetch, new JsonHttpResponseHandler() {
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

        twitterClient.getTimeLine(-1, apiToSet, userIdToFetch, new JsonHttpResponseHandler() {
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
