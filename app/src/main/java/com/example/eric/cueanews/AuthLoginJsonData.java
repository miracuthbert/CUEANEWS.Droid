package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Cuthbert Mirambo on 10/7/2017.
 */

class AuthLoginJsonData extends AsyncTask<String, Void, String> implements PostJsonData.OnPostComplete {

    private static final String TAG = "AuthLoginJsonData";

    private static final String mBaseUrl = "http://cueanews.jangobid.com/oauth/token";
    private static final String mRequestMethod = "POST";
    private String mLoginData;
    private int mResponseCode;
    private String mUser;

    private final OnLoginDataAvailable mCallback;
    private boolean runningOnSameThread = false;

    interface OnLoginDataAvailable {
        void onLoginDataAvailable(String data, DownloadStatus status, int responseCode);
    }

    public AuthLoginJsonData(OnLoginDataAvailable callback, String user) {
        mUser = user;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts");

        if (mCallback != null) {
            mCallback.onLoginDataAvailable(s, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    void executeOnSameThread(String user) {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        PostJsonData postJsonData = new PostJsonData(this, mRequestMethod, mUser, null);
        postJsonData.runInSameThread(createUri());

        Log.d(TAG, "executeOnSameThread: ends with " + mBaseUrl);
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts with request " + mRequestMethod + " login data\n" + mUser + " params\n" + params);

        PostJsonData jsonData = new PostJsonData(this, mRequestMethod, mUser, null);
        jsonData.runInSameThread(createUri());

        Log.d(TAG, "doInBackground: ends");

        return mLoginData;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        String destinationUri = Uri.parse(mBaseUrl).toString();

        Log.d(TAG, "createUri: destination Uri: " + destinationUri);

        return destinationUri;
    }

    @Override
    public void onPostComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onPostComplete: starts");

        if (status == DownloadStatus.OK) {

            mResponseCode = responseCode;
            mLoginData = data;

            Log.d(TAG, "onPostComplete: User login data " + data + " with response code " + mResponseCode);
        }

        if (runningOnSameThread && mCallback != null) {
            mCallback.onLoginDataAvailable(data, status, responseCode);
        }

        Log.d(TAG, "onPostComplete: login data " + data);
    }
}
