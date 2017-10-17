package com.example.eric.cueanews;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Cuthbert Mirambo on 10/4/2017.
 */

class PostsRecyclerViewAdapter extends RecyclerView.Adapter<PostsRecyclerViewAdapter.PostsViewHolder> {
    private static final String TAG = "PostsRecyclerViewAdapte";

    private List<Post> mPostList = null;
    private Context mContext;

    public PostsRecyclerViewAdapter(Context context, List<Post> postList) {
        mContext = context;
        mPostList = postList;
    }

    @Override
    public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Called by the layout manager when it needs a new view
        Log.d(TAG, "onCreateViewHolder: starts");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_browse, parent, false);

        return new PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostsViewHolder holder, int position) {
        //Called by the layout manager when it wants new data in an existing row

        Post postItem = mPostList.get(position);
        Log.d(TAG, "onBindViewHolder: " + postItem.getTitle() + " --> " + position);

        String imageUri = Uri.parse("http://cueanews.jangobid.com" + postItem.getImage()).toString();
        Log.d(TAG, "onBindViewHolder: post image url " + postItem.getImage() + " --> " + position);

        //set post thumbnail
        Picasso.with(mContext).load(imageUri)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(holder.thumbnail);

        //set post title & other details
        holder.title.setText(postItem.getTitle());
        holder.category.setText(postItem.getCategory());
        holder.date.setText(postItem.getCreatedAtHuman());
        holder.views.setText("Views: " + String.valueOf(postItem.getViewsCount()));
        holder.comments.setText("Comments: " + String.valueOf(postItem.getCommentsCount()));
    }

    @Override
    public int getItemCount() {
        return ((mPostList != null) && (mPostList.size() != 0) ? mPostList.size() : 0);
    }

    void loadNewData(List<Post> newPosts) {
        mPostList = newPosts;
        notifyDataSetChanged();
    }

    public Post getPost(int position) {
        return ((mPostList != null) && (mPostList.size() != 0) ? mPostList.get(position) : null);
    }

    static class PostsViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "PostsViewHolder";

        ImageView thumbnail = null;
        TextView title = null;
        TextView category = null;
        TextView date = null;
        TextView views = null;
        TextView comments = null;

        public PostsViewHolder(View itemView) {
            super(itemView);

            Log.d(TAG, "PostsViewHolder: starts");

            this.thumbnail = (ImageView) itemView.findViewById(R.id.post_thumbnail);
            this.title = (TextView) itemView.findViewById(R.id.post_title);
            this.category = (TextView) itemView.findViewById(R.id.post_category);
            this.date = (TextView) itemView.findViewById(R.id.post_date);
            this.views = (TextView) itemView.findViewById(R.id.post_views);
            this.comments = (TextView) itemView.findViewById(R.id.post_comments);
        }
    }
}
