package com.snyper.keeva;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Database.Database;
import com.snyper.keeva.model.Food;
import com.snyper.keeva.model.Order;
import com.snyper.keeva.model.Rating;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {


    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnRating;
    CounterFab btnCart;
    ElegantNumberButton numberButton;
    String foodId;

    RatingBar ratingBar;

    FButton btnShowComment;

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;

    Food currentFood;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());



        btnShowComment=(FButton)findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);
            }
        });

        database=FirebaseDatabase.getInstance();
        foods= database.getReference("Foods");
        ratingTbl=database.getReference("Rating");


        numberButton=(ElegantNumberButton) findViewById(R.id.number_button);
        btnCart=(CounterFab) findViewById(R.id.btnCart);
        btnRating=(FloatingActionButton) findViewById(R.id.btn_rating);
        ratingBar=(RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new Database(getBaseContext()).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            foodId,
                             currentFood.getName(),
                            numberButton.getNumber(),
                            currentFood.getPrice(),
                            currentFood.getDiscount(),
                            currentFood.getImage()

                    ));
                   // Toast.makeText(FoodDetail.this,"Added to Cart",Toast.LENGTH_SHORT).show();
                Toasty.success(FoodDetail.this,"Added to Cart",Toast.LENGTH_SHORT,true).show();
            }
        });

        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        food_description=(TextView)findViewById(R.id.food_description);
        food_name=(TextView)findViewById(R.id.food_name);
        food_price=(TextView)findViewById(R.id.food_price);
        food_image=(ImageView)findViewById(R.id.img_food);


        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);



        if(getIntent()!=null)
            foodId=getIntent().getStringExtra("FoodId");

        if(!foodId.isEmpty()){

            if (Common.isConnectedToInternet(getBaseContext())){
                getDetailFood(foodId);
                getRatingFood(foodId);
            }

            else
            {
                //Toast.makeText(FoodDetail.this,"Please check your internet connection",Toast.LENGTH_SHORT).show();
                Toasty.error(FoodDetail.this,"Please check your internet connection",Toast.LENGTH_SHORT,true).show();
                return;
            }
        }

    }

    private void getRatingFood(String foodId) {

        Query foodrating=ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodrating.addValueEventListener(new ValueEventListener() {

            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Rating item=postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count!=0){
                    float average= sum/count;
                    ratingBar.setRating(average);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Exellent"))
                .setDefaultRating(1)
                .setTitle("Rate this Food")
                .setDescription("Please let us know ur reponse")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please comment here")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimary)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId) {
foods.child(foodId).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

         currentFood= dataSnapshot.getValue(Food.class);
        Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);


        collapsingToolbarLayout.setTitle(currentFood.getName());
        food_price.setText(currentFood.getPrice());
        food_name.setText(currentFood.getName());
       //food_image.setImageDrawable(Drawable.createFromPath(food.getImage()));
        food_description.setText(currentFood.getDescription());


    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
});
       // Toast.makeText(FoodDetail.this,"error Loading",Toast.LENGTH_SHORT).show();
        Toasty.error(FoodDetail.this,"Error Loading",Toast.LENGTH_SHORT,true).show();

    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        final Rating rating=new Rating(Common.currentUser.getPhone(),foodId,String.valueOf(value),
               comments );


        //Users can do multiple ratings using this algorithms
        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                       // Toast.makeText(FoodDetail.this, "Thanks for rating", Toast.LENGTH_SHORT).show();
                        Toasty.success(FoodDetail.this,"THanks for Rating",Toast.LENGTH_SHORT,true).show();
                    }
                });


        /*
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.currentUser.getPhone()).exists())
                {
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }else {
                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
