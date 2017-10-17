package com.example.eric.cueanews;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity implements
        GetPostDetailJsonData.OnPostDataAvailable, PostCommentJsonData.OnPostCommentDataAvailable,
        GetPostCommentsJsonData.OnCommentsDataAvailable, DownloadPostToPdf.OnPostDownloadDataAvailable,
        PostRatingJsonData.OnPostRatingDataAvailable {

    private static final String TAG = "PostDetailActivity";

    private TextView mPostTitle;
    private TextView mPostCategory;
    private TextView mPostBody;
    private TextView mPostViews;
    private TextView mPostComments;
    private TextView mPostDate;
    private TextView mPostAuthor;
    private CollapsingToolbarLayout mToolbarLayout;
    private ImageView mPostImagePlaceholder;
    private EditText mComment;
    private ImageButton mCommentButton;
    private RecyclerView mRecyclerView;
    private RatingBar mPostRating;
    private TextView mPostRatingSummary;
    FloatingActionButton fab;

    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;

    Post mPost;
    SharedPreferences preferences;

    private String mStoredAccessToken;
    private String mPostUrl;
    private String mPostCommentUrl;
    private String mPostDownloadUrl;
    private String mPostRatingUrl;

    static final String POST_TRANSFER = "POST_TRANSFER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_post_comments);

        //set recycler view layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set recycler view item touch listener
//        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListenter(this, mRecyclerView, this));

        //init posts recycler view adapter
        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this, new ArrayList<Comment>());

        //set recycler view adapter
        mRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                attemptPostDownload();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mPost = (Post) intent.getSerializableExtra(POST_TRANSFER);

        mPostTitle = (TextView) findViewById(R.id.post_title);
        mPostCategory = (TextView) findViewById(R.id.post_category);
        mPostBody = (TextView) findViewById(R.id.post_body);
        mPostViews = (TextView) findViewById(R.id.post_views);
        mPostComments = (TextView) findViewById(R.id.post_comments);
        mPostDate = (TextView) findViewById(R.id.post_date);
        mPostRating = (RatingBar) findViewById(R.id.post_rating);
        mPostRatingSummary = (TextView) findViewById(R.id.post_rating_summary);
        mPostAuthor = (TextView) findViewById(R.id.post_author);
        mPostImagePlaceholder = (ImageView) findViewById(R.id.post_img_placeholder);

        if (mPost != null) {

            setupPostDetails(mPost);

            //setup urls
            mPostUrl = "http://cueanews.jangobid.com/api/posts/" + mPost.getId();
            mPostCommentUrl = "http://cueanews.jangobid.com/api/posts/" + mPost.getId() + "/comments";
            mPostDownloadUrl = "http://cueanews.jangobid.com/api/posts/" + mPost.getId() + "/download";
            mPostRatingUrl = "http://cueanews.jangobid.com/api/posts/" + mPost.getId() + "/ratings";
        }

        preferences = getApplication().getSharedPreferences(getString(R.string.app_name), 0);
        mStoredAccessToken = preferences.getString("access_token", null);

        //handle rating bar
        mPostRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                JSONObject jsonRating = new JSONObject();

                try {

                    jsonRating.put("rating", rating);

                } catch (JSONException jsone) {
                    jsone.printStackTrace();

                    Log.d(TAG, "onRatingChanged: Error creating Json data " + jsone.getMessage());
                }

                if ((jsonRating != null) && (jsonRating.length() > 0)) {
                    Log.d(TAG, "attemptCommentPost: comment details parsed as " + jsonRating.toString());

                    //store post rating
                    PostRatingJsonData postRating = new PostRatingJsonData(PostDetailActivity.this, mPostRatingUrl, mStoredAccessToken, jsonRating.toString());
                    postRating.execute((String[]) null);
                } else {
                    Toast.makeText(PostDetailActivity.this, "Some error occured. Please try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //handle comment actions
        mComment = (EditText) findViewById(R.id.message);
        mCommentButton = (ImageButton) findViewById(R.id.send_message_button);
        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCommentPost();
            }
        });
    }

    void setupPostDetails(Post post) {
        getSupportActionBar().setTitle(mPost.getTitle());
        mPostTitle.setText(mPost.getTitle());
        mPostCategory.setText(mPost.getCategory());
        mPostViews.setText("Read by " + mPost.getViewsCount());
        mPostComments.setText("Comments (" + String.valueOf(mPost.getCommentsCount()) + ")");
        mPostDate.setText(mPost.getCreatedAt());
        mPostRating.setRating(mPost.getRating());
        mPostRatingSummary.setText("Rated: " + String.valueOf(mPost.getRating()) + " | " + String.valueOf(mPost.getRatingCount()) + " readers");
        mPostAuthor.setText(mPost.getAuthor());
        mPostBody.setText(mPost.getBody());

        try {
            String imageUri = Uri.parse("http://cueanews.jangobid.com" + mPost.getImage()).toString();
            Log.d(TAG, "setupPostDetails: image url " + imageUri);

            //set post image
            Picasso.with(PostDetailActivity.this).load(imageUri)
                    .error(R.drawable.cueaicon)
                    .placeholder(R.drawable.cueaicon)
                    .into(mPostImagePlaceholder, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: picasso downloaded image");
                            mToolbarLayout.setBackgroundDrawable(mPostImagePlaceholder.getDrawable());
                        }

                        @Override
                        public void onError() {
                            Log.d(TAG, "onError: picasso failed downloading post image");
                            //mToolbarLayout.setBackgroundResource(R.drawable.placeholder);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "setupPostDetails: picasso exception " + e.getMessage());
        }
    }

    void attemptPostDownload() {
        String fileName = mPostTitle.getText().toString();

        if (attemptDownloadView(fileName) == false) {

            DownloadPostToPdf pdfDownload = new DownloadPostToPdf(this, mPostDownloadUrl, fileName, mStoredAccessToken, null);
            pdfDownload.execute((String[]) null);

            fab.setImageResource(android.R.drawable.stat_sys_download);
        }
    }

    boolean attemptDownloadView(String fileName) {
        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/posts/" + fileName + ".pdf");

        if (pdfFile.exists()) {
            Uri path = Uri.fromFile(pdfFile);

            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");
            pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            try {
                Log.d(TAG, "attemptDownloadView: started opening pdf");
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();

                Toast.makeText(PostDetailActivity.this, "Failed opening PDF.", Toast.LENGTH_SHORT).show();

                Log.e(TAG, "attemptDownloadView: Failed to open PDF --> " + e.getMessage());
            }

            return true;
        }

        return false;
    }

    void attemptCommentPost() {
        String comment = mComment.getText().toString();

        if (!comment.isEmpty()) {

            JSONObject jsonComment = new JSONObject();

            try {

                jsonComment.put("body", comment);

            } catch (JSONException jsone) {
                Log.d(TAG, "attemptCommentPost: Error creating Json data " + jsone.getMessage());
            }

            if ((jsonComment != null) && (jsonComment.length() > 0)) {
                Log.d(TAG, "attemptCommentPost: comment details parsed as " + jsonComment.toString());

                PostCommentJsonData postComment = new PostCommentJsonData(this, mPostCommentUrl, mStoredAccessToken, jsonComment.toString());
                postComment.execute((String[]) null);
            } else {
                Toast.makeText(this, "Some error occured. Please try again!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Comment cannot be empty!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: starts");

        super.onResume();

        GetPostDetailJsonData postDetailJsonData = new GetPostDetailJsonData(this, mPostUrl, mStoredAccessToken);
        postDetailJsonData.execute((String[]) null);

        GetPostCommentsJsonData postCommentsJsonData = new GetPostCommentsJsonData(this, mPostCommentUrl, mStoredAccessToken);
        postCommentsJsonData.executeOnSameThread();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_post_share) {
            // Handle the share action
            try {
                //String phn = "null";
                String msg = mPostTitle.getText() + ". " +
                        "Visit https://cueanews.jangobid.com " + " and download the app to read.";

                Intent msgintent = new Intent(Intent.ACTION_VIEW);

                // Invokes only SMS/MMS clients
                msgintent.setType("vnd.android-dir/mms-sms");

                // Specify the Phone Number
                // msgintent.putExtra("address", phn);
                // Specify the Message
                msgintent.putExtra("sms_body", msg);

                startActivity(msgintent);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Failed to sending SMS, please try again later!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnPostDataAvailable(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "OnPostDataAvailable: starts");

        Post postObject = null;
        JSONObject jsonPost = null;

        if (status == DownloadStatus.OK) {
            try {
                jsonPost = new JSONObject(data);

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

                postObject = new Post(id, title, excerpt, body, image, created_at, created_at_human, viewsCount, commentsCount, rating, ratingsCount, author, author_id, avatar, category, categorySlug);

                Log.d(TAG, "onDownloadComplete: " + postObject.toString());
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "OnPostDataAvailable: Error processing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            if (postObject != null) {
                setupPostDetails(postObject);
                mPostViews.setText("Read by " + postObject.getViewsCount());
            }

            Log.d(TAG, "OnPostDataAvailable: post data " + data + " with response code " + responseCode);
        } else {
            Log.d(TAG, "OnPostDataAvailable: failed with status " + status + " and response code " + responseCode);
        }
    }

    @Override
    public void onPostCommentAvailable(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onPostCommentAvailable: starts");

        if (status == DownloadStatus.OK && (data != null)) {

            GetPostCommentsJsonData postCommentsJsonData = new GetPostCommentsJsonData(this, mPostCommentUrl, mStoredAccessToken);
            postCommentsJsonData.executeOnSameThread();

            mComment.setText("");
            mComment.clearFocus();
            mRecyclerView.requestFocus();
            Toast.makeText(PostDetailActivity.this, "Comment posted successfully", Toast.LENGTH_SHORT).show();

            Log.d(TAG, "onPostCommentAvailable: ends with comment data " + data + " with response code " + responseCode);
        } else {
            Toast.makeText(PostDetailActivity.this, "Failed posting comment. Try again!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onPostCommentAvailable: failed with status " + status + " and response code " + responseCode);
        }
    }

    @Override
    public void onCommentsDataAvailable(List<Comment> data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onCommentsDataAvailable: starts");

        if (status == DownloadStatus.OK) {
            mCommentsRecyclerViewAdapter.loadNewData(data);
            mPostComments.setText("Comments (" + String.valueOf(mCommentsRecyclerViewAdapter.getItemCount()) + ")");
        } else {
            Log.e(TAG, "onCommentsDataAvailable: failed with status " + status + " and response code " + responseCode);
        }

    }

    @Override
    public void onPostDownloadDataAvailable(boolean data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onPostDownloadDataAvailable: starts");

        fab.setImageResource(android.R.drawable.stat_sys_download_done);

        if (status == DownloadStatus.OK && data) {
            Toast.makeText(PostDetailActivity.this, "Post downloaded successfully.", Toast.LENGTH_SHORT).show();

            attemptDownloadView(mPostTitle.getText().toString());

            Log.d(TAG, "onPostDownloadDataAvailable: returned " + data + " with status " + status + " with response " + responseCode);
        } else {
            Toast.makeText(PostDetailActivity.this, "Post download failed.", Toast.LENGTH_SHORT).show();

            Log.e(TAG, "onPostDownloadDataAvailable: returned " + data + " with status " + status + " with response " + responseCode);
        }

        Log.d(TAG, "onPostDownloadDataAvailable: ends");
    }

    @Override
    public void onPostRatingDataAvailable(String data, DownloadStatus status, int responseCode) {
        Log.d(TAG, "onPostRatingDataAvailable: starts");

        Post postObject = null;
        JSONObject jsonPost = null;

        if (status == DownloadStatus.OK && (data != null) && ((responseCode / 100) == 2)) {
            try {
                jsonPost = new JSONObject(data);

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
                int author_id = jsonAuthor.getInt("id");
                String avatar = jsonAuthor.getString("avatar");

                JSONObject jsonCat = jsonPost.getJSONObject("category");
                JSONObject jsonCategory = jsonCat.getJSONObject("data");

                String category = jsonCategory.getString("name");
                String categorySlug = jsonCategory.getString("slug");

                postObject = new Post(id, title, excerpt, body, image, created_at, created_at_human, viewsCount, commentsCount, rating, ratingsCount, author, author_id, avatar, category, categorySlug);

                Log.d(TAG, "onPostRatingDataAvailable: " + postObject.toString());
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "onPostRatingDataAvailable: Error processing Json data " + jsone.getMessage());

                status = DownloadStatus.FAILED_OR_EMPTY;
            }

            if (postObject != null) {
                setupPostDetails(postObject);
                mPostRating.setRating(postObject.getRating());
                mPostRatingSummary.setText("Rated: " + String.valueOf(postObject.getRating()) + " | " + String.valueOf(postObject.getRatingCount()) + " readers");

                Toast.makeText(PostDetailActivity.this, "Your rating has been added post", Toast.LENGTH_SHORT).show();
            }

            Log.d(TAG, "onPostRatingDataAvailable: post data " + postObject.toString() + " with response code " + responseCode);
        } else if (status == DownloadStatus.OK && (data == null) && (responseCode == 409)) {
            Toast.makeText(PostDetailActivity.this, "You have already rated this post", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "onPostRatingDataAvailable: failed with status " + status + " and response code " + responseCode);
        }

        Log.d(TAG, "onPostRatingDataAvailable: post raw data " + data + " with response code " + responseCode);
    }
}
