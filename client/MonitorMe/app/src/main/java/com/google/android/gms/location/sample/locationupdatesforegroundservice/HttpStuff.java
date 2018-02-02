package com.google.android.gms.location.sample.locationupdatesforegroundservice;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NoCache;

import org.json.JSONException;
import org.json.JSONObject;


public class HttpStuff {
    // Android broadcast garbage
    private static final String PACKAGE_NAME = "com.google.android.gms.location.sample.httpstuff";
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static String TAG = "HttpStuff";

    private RequestQueue queue;
    private Context mContext;
    private static String mToken;
    private static String host = "http://monitor.me.teix.co";
    private static String locationUrl = host + "/m/position";
    private static String registerUrl = host + "/m/register";
    private static String unregisterUrl = host + "/m/unregister";

    public String getSharingUrl() {
        return host + "/monitor?token" + mToken;
    }

    public HttpStuff(Context ctx, String token) {
        mContext = ctx;
        mToken = token;
        queue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));
        queue.start();
    }

    public void register() {

        Response.Listener a = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, response.toString());
                try {
                    mToken = response.getString("token");
                }
                catch (JSONException e) {
                    Log.v(TAG, "Exception is: "+ e);
                }

                // Notify anyone listening for broadcasts about the new location.
                Intent intent = new Intent(ACTION_BROADCAST);
                intent.putExtra(EXTRA_LOCATION, mToken);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        };

        Response.ErrorListener b = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Error is: "+ error);
                // Notify anyone listening for broadcasts about the new location.
                mToken = null;
                Intent intent = new Intent(ACTION_BROADCAST);
                intent.putExtra(EXTRA_LOCATION, mToken);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        };


        JSONObject jsonLocation = new JSONObject();
        try {
            jsonLocation.put("token", mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, registerUrl, jsonLocation, a, b);
        queue.add(jsObjRequest);

    }

    public void unregister() {

        Response.Listener a = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "Unregistered");
            }
        };

        Response.ErrorListener b = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Error is: "+ error);
            }
        };


        JSONObject jsonLocation = new JSONObject();
        try {
            jsonLocation.put("token", mToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, unregisterUrl, jsonLocation, a, b);
        queue.add(jsObjRequest);

    }

    public void submitLocation(Location location) {

        Response.Listener a = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {Log.v(TAG, "Response is: "+ response);
            }
        };
        Response.ErrorListener b = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.v(TAG, "Error is: "+ error); }
        };


        JSONObject jsonLocation = new JSONObject();

        try {
            jsonLocation.put("token", mToken);
            jsonLocation.put("lng", location.getLongitude());
            jsonLocation.put("lat", location.getLatitude());
            jsonLocation.put("accuracy", location.getAccuracy());
            jsonLocation.put("bearing", location.getBearing());
            jsonLocation.put("time", location.getTime());
            jsonLocation.put("speed", location.getSpeed());
            jsonLocation.put("provider", location.getProvider());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, locationUrl, jsonLocation, a, b);

        queue.add(jsObjRequest);
    }
}
