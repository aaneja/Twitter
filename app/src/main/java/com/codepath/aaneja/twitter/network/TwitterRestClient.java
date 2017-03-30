package com.codepath.aaneja.twitter.network;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthAsyncHttpClient;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.Serializable;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
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

	public void getTimeLine(long max_id, API apiToUse, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        switch (apiToUse) {
            case HOME_TIMELINE:
                break;
            case MENTIONS:
                apiUrl = getApiUrl("statuses/mentions_timeline.json");
        }
        Log.d("TwitterRestClient", "getTimeLine: " + apiUrl);
        RequestParams params = new RequestParams();
		if(max_id > 0) {
			params.put("max_id", String.valueOf(max_id));
		}
		getClient().get(apiUrl, params, handler);
	}

	public void postTweet(String body, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", body);
		getClient().post(apiUrl, params, handler);
	}

	public enum API implements Serializable {
        HOME_TIMELINE,
        MENTIONS
    }



	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}
