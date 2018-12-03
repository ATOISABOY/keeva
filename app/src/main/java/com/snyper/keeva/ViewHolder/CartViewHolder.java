package com.snyper.keeva.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Interface.ItemClickListener;
import com.snyper.keeva.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener{


   public TextView txt_cart_name,txt_price;
   public ImageView img_cart_count;
   public ElegantNumberButton btn_quantity;
   public ImageView cart_image;

   public RelativeLayout view_background;
   public LinearLayout view_foreground;


   private ItemClickListener itemClickListener;

   public void setTxt_cart_name(TextView txt_cart_name) {
       this.txt_cart_name = txt_cart_name;
   }

   public CartViewHolder(View itemView) {
       super(itemView);

       txt_cart_name=(TextView) itemView.findViewById(R.id.cart_item_name);
       txt_price=(TextView) itemView.findViewById(R.id.cart_item_Price);
       img_cart_count=(ImageView) itemView.findViewById(R.id.cart_item_count);
       cart_image=(ImageView) itemView.findViewById(R.id.cart_image);
       btn_quantity=(ElegantNumberButton) itemView.findViewById(R.id.btn_quantity);
       view_background=(RelativeLayout)itemView.findViewById(R.id.view_background);
       view_foreground=(LinearLayout)itemView.findViewById(R.id.view_foreground);
       itemView.setOnCreateContextMenuListener(this);
   }

   @Override
   public void onClick(View view) {

   }

   @Override
   public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo contextMenuInfo) {
       contextMenu.setHeaderTitle("select Action");
       contextMenu.add(0,0,getAdapterPosition(), Common.DELETE);
   }
}
