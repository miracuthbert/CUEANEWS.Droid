package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Cuthbert Mirambo on 10/12/2017.
 */

class PostCommentJsonData extends AsyncTask<String, Void, String> implements PostJsonData.OnPostComplete {

    private static final String TAG = "PostCommentJsonData";

    private String mBaseURL;
    private static final String mRequestMethod = "POST";
    private String mAccessToken;
    private String mPostData;
    private String mCommentData;
    private int mResponseCode;

    private final OnPostCommentDataAvailable mCallback;
    private boolean runningOnSameThread = false;

    interface OnPostCommentDataAvailable {
        void onPostCommentAvailable(String data, DownloadStatus status, int responseCode);
    }

    public PostCommentJsonData(OnPostCommentDataAvailable callBack, String baseURL, String accessToken, String postData) {
        mBaseURL = baseURL;
        mAccessToken = accessToken;
        mPostData = postData;
        mCallback = callBack;
    }

    void executeOnSameThread(String user) {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        PostJsonData postJsonData = new PostJsonData(this, mRequestMethod, mPostData, mAccessToken);
        postJsonData.runInSameThread(createUri());

        Log.d(TAG, "executeOnSameThread: ends with " + mBaseURL);
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts");

        if (mCallback != null) {
            mCallback.onPostCommentAvailable(s, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts with request " + mRequestMethod + " post comment data\n" + mPostData + " params\n" + params);

        PostJsonData jsonData = new PostJsonData(this, mRequestMethod, mPostData, mAccessToken);
        jsonData.runInSameThread(createUri());

        Log.d(TAG, "doInBackground: ends");

        return mCommentData;
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

                JSONObject jsonComment = new JSONObject(data).getJSONObject("data");
                mCommentData = jsonComment.toString();

            }  catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "onPostComplete: Error processing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            Log.d(TAG, "onPostComplete: Post comment data " + data + " with response code " + mResponseCode);
        }

        if (runningOnSameThread && mCallback!= null) {
            mCallback.onPostCommentAvailable(mCommentData, status, mResponseCode);
        }

        Log.d(TAG, "onPostComplete: ends with comment raw data " + data);
    }
}
