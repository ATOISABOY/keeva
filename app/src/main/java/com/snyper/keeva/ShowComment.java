package com.snyper.keeva;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.ViewHolder.ShowCommentViewHolder;
import com.snyper.keeva.model.Rating;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by stephen snyper on 10/24/2018.
 */

public class ShowComment extends AppCompatActivity{

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating,ShowCommentViewHolder> adapter;

    String foodId;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        //firebase
        database=FirebaseDatabase.getInstance();
        ratingTbl=database.getReference("Rating");

        recyclerView=(RecyclerView)findViewById(R.id.recyclerComment);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //swipe
        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent()!=null){
                    foodId= getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }
                if (!foodId.isEmpty() && foodId!=null){
                    Query query=ratingTbl.orderByChild("foodId").equalTo(foodId);
                    FirebaseRecyclerOptions<Rating> options= new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();

                    adapter= new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder holder, int position, @NonNull Rating model) {
                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserName.setText(model.getUserPhone());
                        }

                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View view= LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.show_comment,parent,false);
                            return new  ShowCommentViewHolder(view);
                        }
                    };
                    loadComment(foodId);
                }
            }
        });
mSwipeRefreshLayout.post(new Runnable() {
    @Override
    public void run() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (getIntent()!=null){
            foodId= getIntent().getStringExtra(Common.INTENT_FOOD_ID);
        }
        if (!foodId.isEmpty() && foodId!=null){
            Query query=ratingTbl.orderByChild("foodId").equalTo(foodId);
            FirebaseRecyclerOptions<Rating> options= new FirebaseRecyclerOptions.Builder<Rating>()
                    .setQuery(query,Rating.class)
                    .build();

            adapter= new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull ShowCommentViewHolder holder, int position, @NonNull Rating model) {
                    holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                    holder.txtComment.setText(model.getComment());
                    holder.txtUserName.setText(model.getUserPhone());
                }

                @Override
                public ShowCommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view= LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.show_comment,parent,false);
                    return new  ShowCommentViewHolder(view);
                }
            };
            loadComment(foodId);
        }
    }
});
    }

    private void loadComment(String foodId) {
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter!=null){
            adapter.stopListening();
        }
    }
}
