package com.example.eric.cueanews;

import java.io.Serializable;

/**
 * Created by Cuthbert Mirambo on 10/4/2017.
 */

class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    private int mId;
    private String mTitle;
    private String mExcerpt;
    private String mBody;
    private String mImage;
    private String mCreatedAt;
    private String mCreatedAtHuman;
    private int mViewsCount;
    private int mCommentsCount;
    private float mRating;
    private int mRatingCount;
    private String mAuthor;
    private int mAuthor_id;
    private String mAuthorAvatar;
    private String mCategory;
    private String mCategorySlug;

    public Post(int id, String title, String excerpt, String body, String image, String createdAt, String createdAtHuman, int viewsCount, int commentsCount, float rating, int ratingCount, String author, int author_id, String authorAvatar, String category, String categorySlug) {
        mId = id;
        mTitle = title;
        mExcerpt = excerpt;
        mBody = body;
        mImage = image;
        mCreatedAt = createdAt;
        mCreatedAtHuman = createdAtHuman;
        mViewsCount = viewsCount;
        mRating = rating;
        mRatingCount = ratingCount;
        mCommentsCount = commentsCount;
        mAuthor = author;
        mAuthor_id = author_id;
        mAuthorAvatar = authorAvatar;
        mCategory = category;
        mCategorySlug = categorySlug;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getExcerpt() {
        return mExcerpt;
    }

    public String getBody() {
        return mBody;
    }

    public String getImage() {
        return mImage;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getCreatedAtHuman() {
        return mCreatedAtHuman;
    }

    public int getViewsCount() {
        return mViewsCount;
    }

    public int getCommentsCount() {
        return mCommentsCount;
    }

    public float getRating() {
        return mRating;
    }

    public int getRatingCount() {
        return mRatingCount;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public int getAuthor_id() {
        return mAuthor_id;
    }

    public String getAuthorAvatar() {
        return mAuthorAvatar;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getCategorySlug() {
        return mCategorySlug;
    }
}
