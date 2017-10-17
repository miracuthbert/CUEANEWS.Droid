package com.example.eric.cueanews;

/**
 * Created by Cuthbert Mirambo on 10/3/2017.
 */

class Category {
    private String mName;
    private String mSlug;
    private String mDetails;
    private int mPostsCount;

    public Category(String name, String slug, String details, int postsCount) {
        mName = name;
        mSlug = slug;
        mDetails = details;
        mPostsCount = postsCount;
    }

    String getName() {
        return mName;
    }

    String getSlug() {
        return mSlug;
    }

    String getDetails() {
        return mDetails;
    }

    int getPostsCount() {
        return mPostsCount;
    }

    @Override
    public String toString() {
        return "Category{" +
                "mName='" + mName + '\'' +
                ", mSlug='" + mSlug + '\'' +
                ", mDetails='" + mDetails + '\'' +
                ", mPostsCount=" + mPostsCount +
                '}';
    }
}
