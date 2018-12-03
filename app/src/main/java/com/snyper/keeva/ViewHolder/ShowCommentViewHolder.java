package com.snyper.keeva.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.snyper.keeva.R;

/**
 * Created by stephen snyper on 10/18/2018.
 */

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {
    public TextView txtUserName,txtComment;
    public RatingBar ratingBar;

    public ShowCommentViewHolder(View itemView) {
        super(itemView);
        txtComment=(TextView)itemView.findViewById(R.id.txtComment);
        txtUserName=(TextView)itemView.findViewById(R.id.txtUserName);
        ratingBar=(RatingBar)itemView.findViewById(R.id.ratingBar);


    }
}
