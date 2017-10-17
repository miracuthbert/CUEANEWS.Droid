package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Cuthbert Mirambo on 10/16/2017.
 */

class PostRatingJsonData extends AsyncTask<String, Void, String> implements PostJsonData.OnPostComplete {

    private static final String TAG = "PostRatingJsonData";


    private String mBaseURL;
    private static final String mRequestMethod = "POST";
    private String mAccessToken;
    private String mPostData;
    private String mRatingData;
    private int mResponseCode;

    private final OnPostRatingDataAvailable mCallback;
    private boolean runningOnSameThread = false;

    interface OnPostRatingDataAvailable {
        void onPostRatingDataAvailable(String data, DownloadStatus status, int responseCode);
    }

    public PostRatingJsonData(OnPostRatingDataAvailable callback, String baseURL, String accessToken, String postData) {
        mBaseURL = baseURL;
        mAccessToken = accessToken;
        mPostData = postData;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts");

        if (mCallback != null) {
            mCallback.onPostRatingDataAvailable(s, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts with request " + mRequestMethod + " post comment data\n" + mPostData + " params\n" + params);

        PostJsonData jsonData = new PostJsonData(this, mRequestMethod, mPostData, mAccessToken);
        jsonData.runInSameThread(createUri());

        Log.d(TAG, "doInBackground: ends");

        return mRatingData;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        String destinationUri = Uri.parse(mBaseURL).toString();

        Log.d(TAG, "createUri: destination Uri: " + destinationUri);

        return destinationUri;
    }

    @Override
    public void onPostComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onPostComplete: starts");

        mResponseCode = responseCode;

        if (status == DownloadStatus.OK) {
            try {

                JSONObject jsonRating = new JSONObject(data).getJSONObject("data");
                mRatingData = jsonRating.toString();

            }  catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "onPostComplete: Error processing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            Log.d(TAG, "onPostComplete: Post rating data " + mRatingData + " with response code " + mResponseCode);

        }

        if (runningOnSameThread && mCallback != null) {
            mCallback.onPostRatingDataAvailable(mRatingData, status, mResponseCode);
        }

        Log.d(TAG, "onPostComplete: ends with rating raw data " + data);
    }
}
