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
 * Created by Cuthbert Mirambo on 10/4/2017.
 */

class GetPostsJsonData extends AsyncTask<String, Void, List<Post>> implements GetRawData.OnDownloadComplete {

    private static final String TAG = "GetPostsJsonData";

    private List<Post> mPostList = null;
    private String mBaseURL;
    private String mCategory;
    private int mAuthor;

    private final OnPostDataAvailable mCallBack;
    private boolean runningOnSameThread = false;

    interface OnPostDataAvailable {
        void onPostDataAvailable(List<Post> data, DownloadStatus status);
    }

    public GetPostsJsonData(OnPostDataAvailable callBack, String baseURL, String category, int author) {
        mBaseURL = baseURL;
        mCategory = category;
        mAuthor = author;
        mCallBack = callBack;
    }

    void executeOnSameThread(String searchCriteria) {
        Log.d(TAG, "executeOnSameThread: starts");

        runningOnSameThread = true;

        //searchCriteria can be used to build more robust query strings

        String destinationUri = createUri(searchCriteria, mCategory, mAuthor);

        GetRawData getRawData = new GetRawData(this, null);
        getRawData.execute(destinationUri);

        Log.d(TAG, "executeOnSameThread: ends with " + destinationUri);
    }

    @Override
    protected void onPostExecute(List<Post> posts) {
        Log.d(TAG, "onPostExecute: starts with callback " + posts.toString());

        if (mCallBack != null) {
            mCallBack.onPostDataAvailable(mPostList, DownloadStatus.OK);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected List<Post> doInBackground(String... params) {
        Log.d(TAG, "doInBackground starts");

        String destinationUri = createUri(params[0], mCategory, mAuthor);

        GetRawData getRawData = new GetRawData(this, null);
        getRawData.runInSameThread(destinationUri);

        Log.d(TAG, "doInBackground: ends");

        return mPostList;
    }

    private String createUri(String searchCriteria, String category, int author) {
        Log.d(TAG, "createUri: starts");

        if ((searchCriteria != null) || (category != null) || (author > 0)) {
            Uri uri = Uri.parse(mBaseURL);
            Uri.Builder builder = uri.buildUpon();

            if (searchCriteria != null) {
                builder = builder.appendQueryParameter("q", searchCriteria);
            }

            if (category != null) {
                builder = builder.appendQueryParameter("category", category);
            }

            if (author > 0) {
                builder = builder.appendQueryParameter("author", String.valueOf(author));
            }

            uri = builder.build();

            return uri.toString();
        }

        return Uri.parse(mBaseURL).toString();
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onDownloadComplete: starts. Status = " + status);

        if (status == DownloadStatus.OK) {
            mPostList = new ArrayList<>();

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("data");

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonPost = itemsArray.getJSONObject(i);
                    int id = jsonPost.getInt("id");
                    String title = jsonPost.getString("title");
                    String excerpt = jsonPost.getString("excerpt");
                    String body = jsonPost.getString("body");
                    String image = jsonPost.getString("image");
                    String created_at = jsonPost.getString("created_at");
                    String created_at_human = jsonPost.getString("created_at_human");
                    int viewsCount = jsonPost.getInt("views_count");
                    int commentsCount = jsonPost.getInt("comments_count");
                    float rating = jsonPost.getInt("rating");
                    int ratingsCount = jsonPost.getInt("ratings_count");

                    JSONObject jsonUser = jsonPost.getJSONObject("user");
                    JSONObject jsonAuthor = jsonUser.getJSONObject("data");

                    String author = jsonAuthor.getString("name");
                    int author_id = /*jsonAuthor.getInt("id")*/ 0;
                    String avatar = jsonAuthor.getString("avatar");

                    JSONObject jsonCat = jsonPost.getJSONObject("category");
                    JSONObject jsonCategory = jsonCat.getJSONObject("data");

                    String category = jsonCategory.getString("name");
                    String categorySlug = jsonCategory.getString("slug");

                    Post postObject = new Post(id, title, excerpt, body, image, created_at, created_at_human, viewsCount, commentsCount, rating, ratingsCount, author, author_id, avatar, category, categorySlug);
                    mPostList.add(postObject);

                    Log.d(TAG, "onDownloadComplete: " + postObject.toString());
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
            mCallBack.onPostDataAvailable(mPostList, status);
        }

        Log.d(TAG, "onDownloadComplete: ends");
    }
}

