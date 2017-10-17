package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Cuthbert Mirambo on 10/11/2017.
 */

class GetPostDetailJsonData extends AsyncTask<String, Void, String> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetPostDetailJsonData";


    private String mBaseURL;
    private String mAccessToken;
    private String mPostData;
    private int mResponseCode;

    private final OnPostDataAvailable mCallBack;
    private boolean runningOnSameThread = false;

    interface OnPostDataAvailable {
        void OnPostDataAvailable(String data, DownloadStatus status, int responseCode);
    }

    public GetPostDetailJsonData(OnPostDataAvailable callBack, String baseURL, String accessToken) {
        mBaseURL = baseURL;
        mAccessToken = accessToken;
        mCallBack = callBack;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts with callback " + mCallBack);

        if (mCallBack != null) {
            mCallBack.OnPostDataAvailable(mPostData, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts with url " + mBaseURL + " and token " + mAccessToken);

        String destinationUri = createUri();

        GetRawData getRawData = new GetRawData(this, mAccessToken);
        getRawData.runInSameThread(destinationUri);

        Log.d(TAG, "doInBackground: ends");

        return mPostData;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        return Uri.parse(mBaseURL).toString();
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onDownloadComplete: starts with status " + status + " response code " + responseCode);

        if (status == DownloadStatus.OK) {

            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(data).getJSONObject("data");

                mPostData = jsonObject.toString();

                Log.d(TAG, "onDownloadComplete: post data " + mPostData);
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "onDownloadComplete: Error parsing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            mResponseCode = responseCode;
        }

        if (runningOnSameThread && mCallBack != null) {
            mCallBack.OnPostDataAvailable(mPostData, status, responseCode);
        }

        Log.d(TAG, "onDownloadComplete: ends with post data " + mPostData + " and response code" + mResponseCode);
    }
}
