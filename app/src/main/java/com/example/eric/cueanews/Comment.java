package com.example.eric.cueanews;

/**
 * Created by Cuthbert Mirambo on 10/11/2017.
 */

class Comment {

    private String mBody;
    private String mCreatedAt;
    private String mCreatedAtHuman;
    private String mPivotUser;
    private String mPivotUserAvatar;

    public Comment(String body, String createdAt, String createdAtHuman, String pivotUser, String pivotUserAvatar) {
        mBody = body;
        mCreatedAt = createdAt;
        mCreatedAtHuman = createdAtHuman;
        mPivotUser = pivotUser;
        mPivotUserAvatar = pivotUserAvatar;
    }

    public String getBody() {
        return mBody;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getCreatedAtHuman() {
        return mCreatedAtHuman;
    }

    public String getPivotUser() {
        return mPivotUser;
    }

    public String getPivotUserAvatar() {
        return mPivotUserAvatar;
    }
}
