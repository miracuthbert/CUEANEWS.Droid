package com.example.eric.cueanews;

/**
 * Created by Cuthbert Mirambo on 10/6/2017.
 */

class User {

    private String mFirstName;
    private String mLastName;
    private String mUsername;
    private String mEmail;
    private String mCreatedAt;
    private String mUpdatedAt;
    private String mAccessToken;

    public User(String firstName, String lastName, String username, String email) {
        mFirstName = firstName;
        mLastName = lastName;
        mUsername = username;
        mEmail = email;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public String getAccessToken() {
        return mAccessToken;
    }
}
