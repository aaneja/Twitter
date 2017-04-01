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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.aaneja.twitter.fragments.TimelineFragment;
import com.codepath.aaneja.twitter.models.Tweet;
import com.codepath.aaneja.twitter.network.TwitterRestClient;

import org.parceler.Parcels;

import static com.codepath.aaneja.twitter.R.id.rvTweets;


public class MainActivity extends AppCompatActivity implements TimelineFragment.OnFragmentInteractionListener {

    private static final int REQUEST_CODE_COMPOSE = 1;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerHomeAdapter viewPagerAdapter;

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
        viewPager = (ViewPager) findViewById(R.id.vpPager);
        viewPagerAdapter = new ViewPagerHomeAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
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
           // Extract name value from result extras
           Tweet newTweet = (Tweet) Parcels.unwrap(data.getExtras().getParcelable(ComposeTweetActivity.NEW_TWEET));
           Log.d("NEWTWEET", "MainActivity/NewTweet/Id : " +newTweet.getId());

           ((TimelineFragment)viewPagerAdapter.getItem(0)).newTweetPosted(newTweet);
           viewPager.setCurrentItem(0); //navigate back to the home timeline
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public static class ViewPagerHomeAdapter extends FragmentPagerAdapter {

        private static int NUM_ITEMS = 2;
        private final FragmentManager fragmentManager;
        private TimelineFragment homeTimeline;
        private TimelineFragment mentionsTimeline;

        public ViewPagerHomeAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
            homeTimeline = TimelineFragment.newInstance(TwitterRestClient.API.HOME_TIMELINE,0);
            mentionsTimeline = TimelineFragment.newInstance(TwitterRestClient.API.MENTIONS, 0);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return homeTimeline;
                case 1:
                    return mentionsTimeline;
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
