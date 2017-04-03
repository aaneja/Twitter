package com.codepath.aaneja.twitter.network;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthAsyncHttpClient;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.Serializable;

import cz.msebera.android.httpclient.Header;

import static java.lang.Long.getLong;

//import static com.codepath.aaneja.twitter.models.Tweet_Table.body;

public class TwitterRestClient extends OAuthBaseClient{
	public static final Class<? extends Api> REST_API_CLASS  = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = "O51FU4ZRFnExF6vAx7Djd0Nqm";
	public static final String REST_CONSUMER_SECRET = "92yMwa7DNtG1wifUaJk7WKDnsjr49Vr8b6NvJgDkatccmWKyfn";
	public static final String REST_CALLBACK_URL = "oauth://fooTwitter";

	public TwitterRestClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	@Override
	protected OAuthAsyncHttpClient getClient() {
		return super.getClient();
	}

	public void getTimeLine(long max_id, API apiToUse, long userId, AsyncHttpResponseHandler handler) {

        RequestParams params = new RequestParams();
        if(max_id > 0) {
            params.put("max_id", String.valueOf(max_id));
        }

        String apiUrl = getApiUrl("statuses/home_timeline.json");
        switch (apiToUse) {
            case HOME_TIMELINE:
                break;
            case MENTIONS:
				apiUrl = getApiUrl("statuses/mentions_timeline.json");
				break;
            case USER_TIMELINE:
                apiUrl = getApiUrl("statuses/user_timeline.json");
				if(userId != 0) {
					params.put("user_id", userId);
				}
                break;
        }
        Log.d("TwitterRestClient", "getTimeLine: " + apiUrl);

		getClient().get(apiUrl, params, handler);
	}

	public void postTweet(String body, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", body);
		getClient().post(apiUrl, params, handler);
	}

	public void getUser(long userId, AsyncHttpResponseHandler handler) {
		if(userId == 0) {
			getLoggedInUser(handler);
			return;
		}

		Log.i("TwitterRestClient","Getting user timeline for : "+userId);
		String apiUrl = getApiUrl("users/show.json");
		RequestParams params = new RequestParams();
		params.put("user_id", userId);
		params.put("include_entites", false);
		getClient().get(apiUrl, params, handler);
	}

	private void getLoggedInUser(final AsyncHttpResponseHandler handler)
	{
		String apiUrl = getApiUrl("account/verify_credentials.json");
		getClient().get(apiUrl, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					long userId = response.getLong("id");
					Log.i("TwitterRestClient","getLoggedInUser resolved user_id to :" + userId);
					getUser(userId,handler);
				}
				catch (JSONException e){
					e.printStackTrace();
				}
			}
		});
	}

	public enum API implements Serializable {
        HOME_TIMELINE,
        MENTIONS,
        USER_TIMELINE
    }
}
