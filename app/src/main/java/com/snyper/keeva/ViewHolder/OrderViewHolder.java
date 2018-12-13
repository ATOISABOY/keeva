package com.snyper.keeva.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.snyper.keeva.Interface.ItemClickListener;
import com.snyper.keeva.R;

/**
 * Created by stephen snyper on 9/8/2018.
 */

public class OrderViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener{

public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAdress,txtOrderDate;

private ItemClickListener itemClickListener;

public ImageView btn_delete;

    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrderAdress=(TextView) itemView.findViewById(R.id.order_address);
        txtOrderId=(TextView) itemView.findViewById(R.id.order_id);
        txtOrderStatus=(TextView) itemView.findViewById(R.id.order_status);
        txtOrderPhone=(TextView) itemView.findViewById(R.id.order_phone);
        txtOrderDate=(TextView) itemView.findViewById(R.id.order_date);
        btn_delete=(ImageView)itemView.findViewById(R.id.btn_delete);

       itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {


        itemClickListener.onClick(view,getAdapterPosition(),false);

    }
}
