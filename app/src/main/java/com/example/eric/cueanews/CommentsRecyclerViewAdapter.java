package com.example.eric.cueanews;

import android.content.Context;
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
 * Created by Cuthbert Mirambo on 10/12/2017.
 */

class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentsViewHolder> {

    private static final String TAG = "CommentsRecyclerViewAda";

    private List<Comment> mCommentList = null;
    private Context mContext;

    public CommentsRecyclerViewAdapter(Context context, List<Comment> commentList) {
        mCommentList = commentList;
        mContext = context;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: starts");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_browse, parent, false);

        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        Comment commentItem = mCommentList.get(position);
        Log.d(TAG, "onBindViewHolder: " + commentItem.getBody() + " --> " + position);

        //set post thumbnail
        Picasso.with(mContext).load(commentItem.getPivotUserAvatar())
                .error(R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(holder.avatar);

        //set post title
        holder.name.setText(commentItem.getPivotUser());
        holder.body.setText(commentItem.getBody());
        holder.date.setText(commentItem.getCreatedAtHuman());

    }

    @Override
    public int getItemCount() {
        return ((mCommentList != null) && (mCommentList.size() != 0) ? mCommentList.size() : 0);
    }

    void loadNewData(List<Comment> newComments) {
        mCommentList = newComments;
        notifyDataSetChanged();
    }

    public Comment getComment(int position) {
        return ((mCommentList != null) && (mCommentList.size() != 0) ? mCommentList.get(position) : null);
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "PostsViewHolder";

        ImageView avatar = null;
        TextView name = null;
        TextView body = null;
        TextView date = null;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            Log.d(TAG, "PostsViewHolder: starts");

            this.avatar = (ImageView) itemView.findViewById(R.id.comment_avatar);
            this.name = (TextView) itemView.findViewById(R.id.comment_user);
            this.body = (TextView) itemView.findViewById(R.id.comment_body);
            this.date = (TextView) itemView.findViewById(R.id.comment_date);
        }
    }
}
