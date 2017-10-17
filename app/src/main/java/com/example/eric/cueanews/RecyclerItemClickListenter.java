package com.example.eric.cueanews;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Cuthbert Mirambo on 10/5/2017.
 */

class RecyclerItemClickListenter extends RecyclerView.SimpleOnItemTouchListener {
    private static final String TAG = "RecyclerItemClickListen";

    interface OnRecyclerItemClickListenter {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private final OnRecyclerItemClickListenter mListenter;
    private final GestureDetectorCompat mGestureDetector;

    public RecyclerItemClickListenter(Context context, final RecyclerView recyclerView, OnRecyclerItemClickListenter listenter) {
        mListenter = listenter;
        mGestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp: starts");

                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (childView != null && mListenter != null){
                    Log.d(TAG, "onSingleTapUp: calling listener.onItemClick");

                    mListenter.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                }

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: starts");

                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListenter != null){
                    Log.d(TAG, "onLongPress: calling listener.onItemLongClick");

                    mListenter.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.d(TAG, "onInterceptTouchEvent: starts");

        if (mGestureDetector != null) {
            boolean result = mGestureDetector.onTouchEvent(e);
            Log.d(TAG, "onInterceptTouchEvent(): returned: " + result);
            return result;
        } else {
            Log.d(TAG, "onInterceptTouchEvent(): returned: false");
            return false;
        }
    }
}
