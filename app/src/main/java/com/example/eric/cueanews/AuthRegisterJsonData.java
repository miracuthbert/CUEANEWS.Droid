package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by Cuthbert Mirambo on 10/6/2017.
 */

class AuthRegisterJsonData extends AsyncTask<String, Void, String> implements PostJsonData.OnPostComplete {

    private static final String TAG = "AuthRegisterJsonData";

    private static final String mBaseUrl = "http://cueanews.jangobid.com/api/register";
    private static final String mRequestMethod = "POST";
    private String mSignUpData;
    private int mResponseCode;
    private String mUser;

    private final OnSignUpDataAvailable mCallback;
    private boolean runningOnSameThread = false;

    interface OnSignUpDataAvailable {
        void onSignUpDataAvailable(String data, DownloadStatus status, int responseCode);
    }

    public AuthRegisterJsonData(OnSignUpDataAvailable callback, String user) {
        mUser = user;
        mCallback = callback;
    }

    void executeOnSameThread(String user) {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        PostJsonData postJsonData = new PostJsonData(this, mRequestMethod, mUser, null);
        postJsonData.runInSameThread(createUri());

        Log.d(TAG, "executeOnSameThread: ends with " + mBaseUrl);
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts");

        if (mCallback != null) {
            mCallback.onSignUpDataAvailable(mSignUpData, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts");

        PostJsonData postJsonData = new PostJsonData(this, mRequestMethod, mUser, null);
        postJsonData.runInSameThread(createUri());

        Log.d(TAG, "doInBackground: ends");

        return mSignUpData;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        String destinationUri = Uri.parse(mBaseUrl).toString();

        Log.d(TAG, "createUri: destination uri " + destinationUri);

        return destinationUri;
    }

    @Override
    public void onPostComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onPostComplete: starts");

        if (status == DownloadStatus.OK) {

            JSONObject jsonObject = null;

            try {

                jsonObject = new JSONObject(data).getJSONObject("data");

            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "onPostComplete: Error parsing Json data " + jsone.getMessage());
            }


            mResponseCode = responseCode;
            mSignUpData = jsonObject.toString();
        }

        if (runningOnSameThread && mCallback != null) {
            mCallback.onSignUpDataAvailable(mSignUpData, status, responseCode);
        }

        Log.d(TAG, "onPostComplete: data: " + mSignUpData + " with response code " + responseCode);
    }
}
