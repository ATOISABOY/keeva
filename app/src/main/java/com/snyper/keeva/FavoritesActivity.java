package com.snyper.keeva;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Database.Database;
import com.snyper.keeva.Helper.RecyclerItemTouchHelper;
import com.snyper.keeva.Interface.RecyclerItemTouchHelperListener;
import com.snyper.keeva.ViewHolder.FavoritesAdapter;
import com.snyper.keeva.ViewHolder.FavoritesViewHolder;
import com.snyper.keeva.model.Favorites;
import com.snyper.keeva.model.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    FirebaseDatabase database;
    DatabaseReference foodList;

    FavoritesAdapter adapter;
    RelativeLayout rootLayout;

    ShimmerRecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        rootLayout=(RelativeLayout)findViewById(R.id.root_layout);
        recyclerView=(ShimmerRecyclerView)findViewById(R.id.recycler_fav);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.showShimmerAdapter();


        ItemTouchHelper.SimpleCallback itemTouchHelperCallback= new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFavorites();
            }
        }, 3000);



    }

    private void loadFavorites() {

        adapter= new FavoritesAdapter(this,new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
        recyclerView.hideShimmerAdapter();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder){

            String name=((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();
            final Favorites deleteItem=((FavoritesAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();

            adapter.removeItem(viewHolder.getAdapterPosition());
            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            //make snackbar
            Snackbar snackbar=Snackbar.make(rootLayout,name+"removed from favorites!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem);

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
