package com.codepath.aaneja.twitter.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.codepath.aaneja.twitter.databinding.ItemTweetBinding;
import com.codepath.aaneja.twitter.helpers.Helpers;
import com.codepath.aaneja.twitter.models.Tweet;
import com.codepath.aaneja.twitter.network.TwitterRestClient;

import java.util.List;

/**
 * Created by aaneja on 3/24/2017.
 */

public class TweetItemAdapter extends
        RecyclerView.Adapter<TweetItemAdapter.ViewHolder> {
    private List<Tweet> FetchedTweets;

    public TweetItemAdapter(List<Tweet> fetchedTweets) {
        FetchedTweets = fetchedTweets;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {

        private ItemTweetBinding binding;

        public ViewHolder(ItemTweetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Tweet tweet) {
            binding.tvTweetText.setText(tweet.getBody());
            binding.tvTimeStamp.setText(Helpers.getRelativeTimeAgo(tweet.getTimestamp()));
            binding.tvUserInfo.setText(String.format("(@%s) %s",tweet.getUser().getScreenName(),tweet.getUser().getFullName()));
            Glide.with(binding.ivProfile.getContext()).load(TwitterRestClient.GetImageVariantUrl(tweet.getUser().getProfileImageUrl(),"")).into(binding.ivProfile);
            binding.executePendingBindings();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tweet tweet = getItemForPosition(position);
        holder.bind(tweet);
    }

    private Tweet getItemForPosition(int position) {
        return FetchedTweets.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTweetBinding itemBinding =  ItemTweetBinding.inflate(inflater, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemBinding);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return FetchedTweets.size();
    }
}
