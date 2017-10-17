package com.example.eric.cueanews;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Cuthbert Mirambo on 10/13/2017.
 */

class DownloadPostToPdf extends AsyncTask<String, Void, Boolean> implements DownloadToPdf.OnPdfDownloadComplete {

    private static final String TAG = "DownloadPostToPdf";

    private static final String mRequestMethod = "GET";
    private String mBaseURL;
    private String mFileName;
    private String mAccessToken;
    private String mPostData;
    private String mPostDownload;
    private int mResponseCode;

    private final OnPostDownloadDataAvailable mCallback;
    private boolean runningOnSameThread = false;

    interface OnPostDownloadDataAvailable {
        void onPostDownloadDataAvailable(boolean data, DownloadStatus status, int responseCode);
    }

    public DownloadPostToPdf(OnPostDownloadDataAvailable callBack, String baseURL, String fileName, String accessToken, String postData) {
        mCallback = callBack;
        mBaseURL = baseURL;
        mFileName = fileName;
        mAccessToken = accessToken;
        mPostData = postData;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        Log.d(TAG, "onPostExecute: starts");

        if (mCallback != null) {
            mCallback.onPostDownloadDataAvailable(s, DownloadStatus.OK, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String downloadLocation = Environment.getExternalStorageDirectory().toString();

        File postsDownloads = new File(downloadLocation, "posts");
        postsDownloads.mkdir();

        File postPdf = new File(postsDownloads, mFileName + ".pdf");

        try {
            postPdf.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();

            Log.e(TAG, "doInBackground: Failed downloading post with error " + e.getMessage());
        }

        DownloadToPdf downloadToPdf = new DownloadToPdf(this, mBaseURL, postPdf, mAccessToken);
        downloadToPdf.downloadFile();

        mResponseCode = downloadToPdf.getResponseCode();

        return postPdf.exists();
    }

    private String createUri() {
        Log.d(TAG, "createUri: starts");

        String destinationUri = Uri.parse(mBaseURL).toString();

        Log.d(TAG, "createUri: destination Uri: " + destinationUri);

        return destinationUri;
    }

    @Override
    public void onPdfDownloadComplete(DownloadStatus status, int responseCode) {
        Log.d(TAG, "onDownloadComplete: starts");

        mResponseCode = responseCode;

        boolean isDownloaded = responseCode == 200 ? true : false;

        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onPostComplete: completed with status " + status + " and response code " + responseCode);
        }

        if (runningOnSameThread && mCallback != null) {
            mCallback.onPostDownloadDataAvailable(isDownloaded, status, responseCode);
        }

        Log.d(TAG, "onDownloadComplete: ends");
    }
}
