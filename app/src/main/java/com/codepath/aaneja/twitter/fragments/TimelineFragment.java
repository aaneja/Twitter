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

    private final TwitterRestClient.API apiToSet            ;
    private OnFragmentInteractionListener mListener;

    private static final int REQUEST_CODE_COMPOSE = 1;
    private HashMap<Integer, Long> pageToMaxIdMap = new HashMap<>();
    private ArrayList<Tweet> fetchedTweets = new ArrayList<>();
    private TweetItemAdapter tweetItemAdapter;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    private TwitterRestClient twitterClient = RestApplication.getRestClient();
    private RecyclerView rvTweets;


    public TimelineFragment(TwitterRestClient.API apiToSet) {
        this.apiToSet = apiToSet;
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimelineFragment.
     */
    // TODO: Rename and change types and number of parameters
    /*public static TimelineFragment newInstance(String param1, String param2) {
        TimelineFragment fragment = new TimelineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

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
                twitterClient.getTimeLine(prevMaxId-1, apiToSet , new JsonHttpResponseHandler() {
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

        twitterClient.getTimeLine(-1, apiToSet , new JsonHttpResponseHandler() {
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
