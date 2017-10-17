package com.example.eric.cueanews;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackActivity extends AppCompatActivity implements PostFeedbackJsonData.OnPostFeedbackDataAvailable {
    private static final String TAG = "FeedbackActivity";

    private AutoCompleteTextView mFeedbackView;

    SharedPreferences preferences;

    private String mStoredAccessToken;
    private String mPostFeedbackUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFeedbackView = (AutoCompleteTextView) findViewById(R.id.feeback);

        preferences = getApplication().getSharedPreferences(getString(R.string.app_name), 0);
        mStoredAccessToken = preferences.getString("access_token", null);

        mPostFeedbackUrl = "http://cueanews.jangobid.com/api/feedbacks";

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptPostingFeedback();

                Snackbar.make(view, "Sending feedback...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void attemptPostingFeedback() {
        JSONObject jsonPostFeeback = null;
        String feedback = mFeedbackView.getText().toString();

        if (feedback != null) {
            try {
                jsonPostFeeback = new JSONObject();

                jsonPostFeeback.put("body", feedback);
            } catch (JSONException jsone) {
                jsone.printStackTrace();

                Log.d(TAG, "attemptPostingFeedback: Error creating Json data " + jsone.getMessage());
            }

            if ((jsonPostFeeback != null) && (jsonPostFeeback.length() > 0)) {
                Log.d(TAG, "attemptPostingFeedback: ");

                //call feedback store helper
                PostFeedbackJsonData postFeedback = new PostFeedbackJsonData(this, mPostFeedbackUrl, mStoredAccessToken, jsonPostFeeback.toString());
                postFeedback.execute((String[]) null);

            }
        } else {
            mFeedbackView.setError("Feedback message is required");
        }
    }

    @Override
    public void onPostFeedbackDataAvailable(String data, DownloadStatus status, int responseCode) {
        if (status == DownloadStatus.OK && (data == null) && ((responseCode / 100) == 2)) {
            Toast.makeText(this, "Feedback sent successfully. Thank you!", Toast.LENGTH_SHORT).show();
            mFeedbackView.setText("");
        } else {
            Toast.makeText(this, "Failed sending feedback. Try again!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onPostFeedbackDataAvailable: failed with status " + status + " and response code " + responseCode);
        }

        Log.d(TAG, "onPostFeedbackDataAvailable: ends");
    }
}
