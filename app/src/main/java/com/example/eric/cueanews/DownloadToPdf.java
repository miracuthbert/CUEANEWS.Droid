package com.example.eric.cueanews;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Cuthbert Mirambo on 10/13/2017.
 */

class DownloadToPdf {

    private static final String TAG = "DownloadToPdf";

    private static final int MEGABYTE = 1024 * 1024;

    private int mResponseCode;
    private int mFileName;
    private String mBaseUrl;
    private File mDir;
    private String mAccessToken;

    private OnPdfDownloadComplete mCallback;
    private DownloadStatus mDownloadStatus;

    interface OnPdfDownloadComplete {
        void onPdfDownloadComplete(DownloadStatus status, int responseCode);
    }

    public DownloadToPdf(OnPdfDownloadComplete callback, String baseUrl, File dir, String accessToken) {
        mCallback = callback;
        mBaseUrl = baseUrl;
        mDir = dir;
        mAccessToken = accessToken;
    }

    public void downloadFile() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String access_token = mAccessToken;

        try {

            URL url = new URL(mBaseUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            //check if request is authorized
            if (access_token != null) {
                connection.setRequestProperty("Authorization", access_token);
            }
            connection.connect();

            byte[] buffer = new byte[MEGABYTE];
            InputStream inputStream = connection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(mDir);
            int totalSize = connection.getContentLength();

            int response = connection.getResponseCode();
            mResponseCode = response;

            Log.d(TAG, "doInBackground: The response code was " + response);

            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();

            mDownloadStatus = DownloadStatus.OK;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "doInBackground: File not found " + e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception reading data: " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception. Needs permission? " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing stream " + e.getMessage());
                }
            }
        }

        if(mCallback != null) {
            mCallback.onPdfDownloadComplete(mDownloadStatus, mResponseCode);
        }
    }

    public int getResponseCode() {
        return mResponseCode;
    }
}
