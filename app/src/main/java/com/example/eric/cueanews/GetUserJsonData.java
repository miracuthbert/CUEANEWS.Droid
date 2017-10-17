package com.example.eric.cueanews;


import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Cuthbert Mirambo on 10/10/2017.
 */

class GetUserJsonData extends AsyncTask<String, Void, String> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetUserJsonData";

    private String mBaseURL;
    private String mAccessToken;
    private String mUserData;
    private int mResponseCode;

    private final OnUserDataAvailable mCallBack;
    private boolean runningOnSameThread = false;

    interface OnUserDataAvailable {
        void onUserDataAvailable(String data, DownloadStatus status, int responseCode);
    }

    public GetUserJsonData(OnUserDataAvailable callBack, String baseURL, String accessToken) {
        Log.d(TAG, "GetUserJsonData: starts with url " + baseURL + " and token " + accessToken);
        mBaseURL = baseURL;
        mAccessToken = accessToken;
        mCallBack = callBack;
    }

    void executeOnSameThread() {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        String destinationUri = createUri();

        GetRawData getRawData = new GetRawData(this, mAccessToken);
        getRawData.runInSameThread(destinationUri);

    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts with callback " + mCallBack);

        if(mCallBack != null) {
            mCallBack.onUserDataAvailable(mUserData, DownloadStatus.OK, mResponseCode);
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

        return mUserData;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        return Uri.parse(mBaseURL).toString();
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onDownloadComplete: starts with status " + status + " response code " + responseCode);

        if (status == DownloadStatus.OK) {

            //TODO: Uncomment block after updating JSON Data Format
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(data).getJSONObject("data");
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "onDownloadComplete: Error parsing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            mResponseCode = responseCode;
            mUserData = jsonObject.toString();
        }

        if (runningOnSameThread && mCallBack != null) {
            //now inform the caller that the processing is done - possibly returning null if there
            // was an error
            mCallBack.onUserDataAvailable(mUserData, status, responseCode);
        }

        Log.d(TAG, "onDownloadComplete: ends with user data " + mUserData + " and response code" + mResponseCode);
    }
}
