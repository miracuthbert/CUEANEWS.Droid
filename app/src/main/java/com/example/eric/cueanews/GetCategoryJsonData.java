package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cuthbert Mirambo on 10/3/2017.
 */

class GetCategoryJsonData extends AsyncTask<String, Void, List<Category>> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetCategoryJsonData";

    private List<Category> mCategoryList = null;
    private String mBaseURL;
    private int mResponseCode;

    private final OnCategoryDataAvailable mCallBack;
    private boolean runningOnSameThread = false;

    interface OnCategoryDataAvailable {
        void onCategoryDataAvailable(List<Category> data, DownloadStatus status, int responseCode);
    }

    public GetCategoryJsonData(OnCategoryDataAvailable callBack, String baseURL) {
        Log.d(TAG, "GetCategoryJsonData: called");

        mBaseURL = baseURL;
        mCallBack = callBack;
    }

    void executeOnSameThread(String searchCriteria) {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        //searchCriteria can be used to build more robust query strings

        String destinationUri = createUri();

        GetRawData getRawData = new GetRawData(this, null);
        getRawData.execute(destinationUri);

        Log.d(TAG, "executeOnSameThread: ends");
    }

    @Override
    protected void onPostExecute(List<Category> categories) {
        Log.d(TAG, "onPostExecute: starts with callback "+ mCallBack);

        if (mCallBack != null){
            mCallBack.onCategoryDataAvailable(mCategoryList, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected List<Category> doInBackground(String... params) {
        Log.d(TAG, "doInBackground starts");

        String destinationUri = createUri();

        GetRawData getRawData = new GetRawData(this, null);
        getRawData.runInSameThread(destinationUri);

        Log.d(TAG, "doInBackground: ends");
        
        return mCategoryList;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        return Uri.parse(mBaseURL).toString();
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onDownloadComplete: starts. Status = " + status);

        if (status == DownloadStatus.OK && (data != null)) {
            mCategoryList = new ArrayList<>();

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("data");

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonCategory = itemsArray.getJSONObject(i);
                    String name = jsonCategory.getString("name");
                    String slug = jsonCategory.getString("slug");
                    String details = jsonCategory.getString("details");
                    int postsCount = jsonCategory.getInt("posts_count");

                    Category categoryObject = new Category(name, slug, details, postsCount);
                    mCategoryList.add(categoryObject);

                    Log.d(TAG, "onDownloadComplete: " + categoryObject.toString());
                }
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.e(TAG, "onDownloadComplete: Error processing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }

        if (runningOnSameThread && mCallBack != null) {
            //now inform the caller that the processing is done - possibly returning null if there
            // was an error
            mCallBack.onCategoryDataAvailable(mCategoryList, status, responseCode);
        }

        Log.d(TAG, "onDownloadComplete: ends");
    }
}
