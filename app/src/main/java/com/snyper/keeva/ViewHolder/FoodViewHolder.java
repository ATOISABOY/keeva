package com.snyper.keeva.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.snyper.keeva.Interface.ItemClickListener;
import com.snyper.keeva.R;

/**
 * Created by stephen snyper on 9/5/2018.
 */

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView food_name,food_price;
    public ImageView food_image,fav_image,share_image,quick_cart;


    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name=(TextView)itemView.findViewById(R.id.food_name);
        food_price=(TextView)itemView.findViewById(R.id.food_price);
        food_image=(ImageView)itemView. findViewById(R.id.food_image);
        fav_image=(ImageView)itemView. findViewById(R.id.fav);
        share_image=(ImageView)itemView. findViewById(R.id.btnShare);
        quick_cart=(ImageView)itemView. findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
