package com.codepath.aaneja.twitter;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.aaneja.twitter.fragments.TimelineFragment;
import com.codepath.aaneja.twitter.network.TwitterRestClient;


public class MainActivity extends AppCompatActivity implements TimelineFragment.OnFragmentInteractionListener {

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
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.vpPager);
        viewPager.setAdapter(new ViewPagerHomeAdapter(getSupportFragmentManager()));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.phTimeline, TimelineFragment.newInstance(TwitterRestClient.API.USER_TIMELINE,"YawnOkPlease"));
        ft.commit();*/
        //viewPager.setCurrentItem(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(MainActivity.this, "Selected tab with Name: " + tab.getText(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void onComposeAction(MenuItem item) {
        Intent i = new Intent(MainActivity.this, ComposeTweetActivity.class);
        startActivityForResult(i, REQUEST_CODE_COMPOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
       if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_COMPOSE) {
           /*// Extract name value from result extras
           Tweet newTweet = (Tweet) Parcels.unwrap(data.getExtras().getParcelable(ComposeTweetActivity.NEW_TWEET));
           Log.d("NEWTWEET", "MainActivity/NewTweet/Id : " +newTweet.getId());

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

    public static class ViewPagerHomeAdapter extends FragmentPagerAdapter {

        private static int NUM_ITEMS = 2;
        private final FragmentManager fragmentManager;

        public ViewPagerHomeAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TimelineFragment.newInstance(TwitterRestClient.API.HOME_TIMELINE,"");
                case 1:
                    return TimelineFragment.newInstance(TwitterRestClient.API.MENTIONS,"");
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
            return "HOME";
            case 1: // Fragment # 0 - This will show FirstFragment different title
            return "MENTIONS";
            default:
            return null;
            }
        }
    }
}
