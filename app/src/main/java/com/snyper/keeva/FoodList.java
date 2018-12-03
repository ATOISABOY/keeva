package com.snyper.keeva;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Database.Database;
import com.snyper.keeva.Interface.ItemClickListener;
import com.snyper.keeva.ViewHolder.FoodViewHolder;
import com.snyper.keeva.model.Category;
import com.snyper.keeva.model.Favorites;
import com.snyper.keeva.model.Food;
import com.snyper.keeva.model.Order;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId="";

    ShimmerRecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    //search function
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchadapter;
    List<String> suggestList= new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //favorites
    Database localDB;
    //facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    //refresh
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    Target target= new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            SharePhoto photo= new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class))
            {
                SharePhotoContent content= new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        database=FirebaseDatabase.getInstance();
        foodList=database.getReference("Foods");

        //swipe
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_orange_dark,android.R.color.holo_orange_dark,android.R.color.holo_orange_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null)
                {

                    if (Common.isConnectedToInternet(getBaseContext())){
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadListFood(categoryId);
                            }
                        }, 3000);



                    recyclerView.hideShimmerAdapter();}
                    else
                    {
                        //Toast.makeText(FoodList.this,"Please check your internet connection",Toast.LENGTH_SHORT).show();
                        Toasty.error(FoodList.this,"Please check your internet connection",Toast.LENGTH_SHORT,true).show();
                        return;
                    }
                }


            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if(getIntent()!=null)
                    categoryId=getIntent().getStringExtra("CategoryId");
                if(!categoryId.isEmpty() && categoryId!=null)
                {

                    if (Common.isConnectedToInternet(getBaseContext())){
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadListFood(categoryId);
                            }
                        }, 3000);


                    }

                    else
                    {
                       // Toast.makeText(FoodList.this,"Please check your internet connection",Toast.LENGTH_SHORT).show();
                        Toasty.error(FoodList.this,"Please check your internet connection",Toast.LENGTH_SHORT,true).show();
                        return;
                    }
                }
                ///search
                materialSearchBar=(MaterialSearchBar)findViewById(R.id.search_bar);
                materialSearchBar.setHint("Enter your food");

                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadSuggest();//Load from firebase
                    }
                }, 3000);




                materialSearchBar.setCardViewElevation(10);
                materialSearchBar.addTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        //use to change suggestion list
                        List<String> suggest = new ArrayList<>();
                        for (String search:suggestList){
                            if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                                suggest.add(search);
                        }
                        materialSearchBar.setLastSuggestions(suggest);


                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                    @Override
                    public void onSearchStateChanged(boolean enabled) {
                        //wen search bar is closed restore original suggestions
                        if (!enabled)
                            recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onSearchConfirmed(CharSequence text) {

                        //wen search finish
                        startSearch(text);

                    }

                    @Override
                    public void onButtonClicked(int buttonCode) {

                    }
                });
            }
        });


        //Init facebook
        callbackManager=  CallbackManager.Factory.create();
        shareDialog= new ShareDialog(this);


        //LocalDB
        localDB= new Database(this);

        recyclerView=(ShimmerRecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.showShimmerAdapter();


    }

    private void startSearch(CharSequence text) {

        //create a query by name
        Query  seachByName=foodList.orderByChild("name").equalTo(text.toString());
        //create options for query
        FirebaseRecyclerOptions<Food> foodOptions= new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(seachByName,Food.class)
                .build();

        searchadapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()). into(viewHolder.food_image);

                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Toast.makeText(FoodList.this,""+local.getName(),Toast.LENGTH_SHORT).show();

                        Intent foodDetail= new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchadapter.getRef(position).getKey());//send foodid details to de next activity
                        startActivity(foodDetail);

                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };

        searchadapter.startListening();
        recyclerView.setAdapter(searchadapter);//set adapter for search result
    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){

                            Food item= postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());//addname of food to suggestion
                        }
                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        recyclerView.hideShimmerAdapter();

    }

    private void loadListFood(String categoryId) {
        //create a query by name
        Query  seachByName=foodList.orderByChild("menuId").equalTo(categoryId);
        //create options for query
        FirebaseRecyclerOptions<Food> foodOptions= new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(seachByName,Food.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder viewHolder, final int position, @NonNull final Food model) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("GH %s",model.getPrice().toString()));
                Picasso.with(getBaseContext()).load(model.getImage()). into(viewHolder.food_image);


                //Quick cart

                    viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isExists=new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            if (!isExists) {
                                new Database(getBaseContext()).addToCart(new Order(
                                        Common.currentUser.getPhone(),
                                        adapter.getRef(position).getKey(),
                                        model.getName(),
                                        "1",
                                        model.getPrice(),
                                        model.getDiscount(),
                                        model.getImage()
                                ));

                            }else {

                                new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(),adapter.getRef(position).getKey());
                            }
                           // Toast.makeText(FoodList.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                            Toasty.success(FoodList.this,"Added to Cart",Toast.LENGTH_SHORT,true).show();
                        }
                    });




                //add favorites

                if (localDB.isFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);

                //click to share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });


                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites= new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());

                        if (!localDB.isFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone())){
                            localDB.addToFavorites(favorites);
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                           // Toast.makeText(FoodList.this,""+model.getName()+"was added to favorites",Toast.LENGTH_SHORT).show();
                            Toasty.success(FoodList.this,""+model.getName()+"was added to favorites",Toast.LENGTH_SHORT,true).show();
                        }else { localDB.removeFromFavorites(adapter.getRef(position).getKey(),Common.currentUser.getPhone());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                           // Toast.makeText(FoodList.this,""+model.getName()+"was removed from favorites",Toast.LENGTH_SHORT).show();
                            Toasty.success(FoodList.this,""+model.getName()+"was removed from favorites",Toast.LENGTH_SHORT,true).show();
                            }

                    }
                });

                final Food local=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // Toast.makeText(FoodList.this,""+local.getName(),Toast.LENGTH_SHORT).show();

                        Intent foodDetail= new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());//send foodid details to de next activity
                        startActivity(foodDetail);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

                    }
                });
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
        recyclerView.hideShimmerAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
       //- searchadapter.stopListening();
    }
}