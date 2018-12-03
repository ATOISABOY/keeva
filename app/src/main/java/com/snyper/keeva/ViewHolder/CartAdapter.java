package com.snyper.keeva.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.snyper.keeva.Cart;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Database.Database;
import com.snyper.keeva.Interface.ItemClickListener;
import com.snyper.keeva.R;
import com.snyper.keeva.model.Order;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {


    private List<Order> listData= new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(cart);
        View itemView= inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);



    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {

        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .resize(75,75)
                .centerCrop()
                .into(holder.cart_image);


        TextDrawable drawable= TextDrawable.builder()
                .buildRound(""+listData.get(position).getQuality(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);

        //elegent
        holder.btn_quantity.setNumber(listData.get(position).getQuality());
        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order=listData.get(position);
                order.setQuality(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update the cart
                //calculation total price
                float total =0;
                List<Order> orders= new Database(cart).getCarts(Common.currentUser.getPhone());
                for (Order item:orders)
                    total+=(Float.parseFloat(order.getPrice()))*(Float.parseFloat(item.getQuality()));
                Locale locale= new Locale("en","US");
                NumberFormat fmt= NumberFormat.getCurrencyInstance(locale);
                cart.txtTotalPrice.setText(fmt.format(total));

            }
        });

        Locale locale= new Locale("en","US");
        NumberFormat fmt= NumberFormat.getCurrencyInstance(locale);
        float price = (Float.parseFloat(listData.get(position).getPrice())*(Float.parseFloat(listData.get(position).getQuality())));
        holder.txt_price.setText(fmt.format(price));
        holder.txt_cart_name.setText(listData.get(position).getProductName() );


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position){
        return listData.get(position);
    }

    public void removeItem(int position){
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item ,int position){
        listData.add(position,item);
        notifyItemInserted(position);
    }
}
