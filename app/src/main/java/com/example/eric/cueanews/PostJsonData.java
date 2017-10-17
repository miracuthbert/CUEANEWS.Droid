package com.example.eric.cueanews;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Cuthbert Mirambo on 10/6/2017.
 */

class PostJsonData extends AsyncTask<String, Void, String> {
    private static final String TAG = "PostJsonData";

    private String mRequestMethod;
    private String mJsonData;
    private String mAccessToken;
    private int mResponseCode;

    private DownloadStatus mDownloadStatus;
    private final OnPostComplete mCallback;

    interface OnPostComplete {
        void onPostComplete(String data, DownloadStatus status, int responseCode);
    }

    public PostJsonData(OnPostComplete callback, String requestMethod, String jsonData, String accessToken) {
        mRequestMethod = requestMethod;
        mJsonData = jsonData;
        mAccessToken = accessToken;
        mCallback = callback;
    }

    void runInSameThread(String s) {
        Log.d(TAG, "runInSameThread: starts with " + s);

        if (mCallback != null) {
            mCallback.onPostComplete(doInBackground(s), mDownloadStatus, mResponseCode);
        }
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: starts");

        if (mCallback != null) {
            mCallback.onPostComplete(s, mDownloadStatus, mResponseCode);
        }

        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "doInBackground: starts with " + strings[0]);

        HttpURLConnection connection = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String jsonData = mJsonData;
        String access_token = mAccessToken;
        String result;

        Log.d(TAG, "doInBackground: received data from child " + jsonData + "\nrequest method " + mRequestMethod + " access token " + access_token);

        if (strings == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
        }

        try {
            mDownloadStatus = DownloadStatus.PROCESSING;

            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(mRequestMethod);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            //check if request is authorized
            if (access_token != null) {
                connection.setRequestProperty("Authorization", access_token);
            }
            connection.connect();

            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(jsonData);
            writer.close();

            int response = connection.getResponseCode();
            String responseMsg = connection.getResponseMessage();
            mResponseCode = response;

            Log.d(TAG, "doInBackground: The response code was " + response + " with message " + responseMsg);
            //everything good until here

            InputStream inputStream = null;

            if ((connection.getResponseCode() / 100) == 2) {
                inputStream =  new BufferedInputStream(connection.getInputStream());
            } else {
                inputStream = new BufferedInputStream(connection.getErrorStream());
            }

            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                Log.d(TAG, "doInBackground: Input stream " + inputStream.toString());

                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while (null != (line = reader.readLine())) {
                Log.d(TAG, "doInBackground: buffering " + line);

                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                Log.d(TAG, "doInBackground: buffer length " + buffer.length());

                mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;

                return null;
            }

            mDownloadStatus = DownloadStatus.OK;

            result = buffer.toString();

            Log.d(TAG, "doInBackground: results " + result);

            return result;

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception reading data: " + e.getMessage() + "\n" + e.toString());
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

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;

        return null;
    }
}
