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
 * Created by Cuthbert Mirambo on 10/12/2017.
 */

class GetPostCommentsJsonData extends AsyncTask<String, Void, List<Comment>> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetPostCommentsJsonData";

    private List<Comment> mCommentsList = null;
    private String mBaseURL;
    private String mAccessToken;
    private int mResponseCode;

    private final OnCommentsDataAvailable mCallBack;
    private boolean runningOnSameThread = false;

    interface OnCommentsDataAvailable {
        void onCommentsDataAvailable(List<Comment> data, DownloadStatus status, int responseCode);
    }

    public GetPostCommentsJsonData(OnCommentsDataAvailable callBack, String baseURL, String accessToken) {
        mCallBack = callBack;
        mBaseURL = baseURL;
        mAccessToken = accessToken;
    }

    void executeOnSameThread() {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        //searchCriteria can be used to build more robust query strings

        String destinationUri = createUri();

        GetRawData getRawData = new GetRawData(this, mAccessToken);
        getRawData.execute(destinationUri);

        Log.d(TAG, "executeOnSameThread: ends with " + destinationUri);
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        Log.d(TAG, "onPostExecute: starts with comments " + comments.toString());

        if (mCallBack != null) {
            mCallBack.onCommentsDataAvailable(comments, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected List<Comment> doInBackground(String... params) {
        Log.d(TAG, "doInBackground: starts");

        String destinationUri = createUri();

        GetRawData getRawData = new GetRawData(this, mAccessToken);
        getRawData.runInSameThread(destinationUri);

        Log.d(TAG, "doInBackground: ends");

        return mCommentsList;
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        return Uri.parse(mBaseURL).toString();
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onDownloadComplete: starts with " + data + " and status" + status + "and response code" + responseCode);

        if (status == DownloadStatus.OK && (data != null)) {
            mCommentsList = new ArrayList<>();

            try {
                JSONObject jsonComments = new JSONObject(data);
                JSONArray itemsArray = jsonComments.getJSONArray("data");

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonComment = itemsArray.getJSONObject(i);

                    String body = jsonComment.getString("body");
                    String created_at = jsonComment.getString("created_at");
                    String created_at_human = jsonComment.getString("created_at_human");

                    JSONObject jsonUser = jsonComment.getJSONObject("user");
                    JSONObject jsonAuthor = jsonUser.getJSONObject("data");

                    int author_id = jsonAuthor.getInt("id");
                    String author = jsonAuthor.getString("name");
                    String avatar = jsonAuthor.getString("avatar");

                    Comment comment = new Comment(body, created_at, created_at_human, author, avatar);

                    mCommentsList.add(comment);

                    Log.d(TAG, "onDownloadComplete: comments " + mCommentsList.toString());
                }
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.e(TAG, "onDownloadComplete: Error processing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }

        if (runningOnSameThread && mCallBack != null) {
            mCallBack.onCommentsDataAvailable(mCommentsList, status, responseCode);
        }

        Log.d(TAG, "onDownloadComplete: ends");
    }
}
