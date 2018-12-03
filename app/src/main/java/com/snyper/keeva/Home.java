package com.snyper.keeva;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.facebook.accountkit.AccountKit;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Database.Database;
import com.snyper.keeva.Interface.ItemClickListener;
import com.snyper.keeva.ViewHolder.MenuViewHolder;
import com.snyper.keeva.model.Banner;
import com.snyper.keeva.model.Category;
import com.snyper.keeva.model.Token;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FirebaseDatabase database;
    DatabaseReference category;

     FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    TextView txtFullname;
    CounterFab fab;
    //slider
    HashMap<String,String> image_list;
    SliderLayout mSlider;

    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);


        //initialze firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");



        FirebaseRecyclerOptions<Category> options= new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();
        adapter= new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()). into(viewHolder.imageView);
                final Category clickItem=model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent foodList= new Intent(Home.this,FoodList.class);
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        //   Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView =LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(itemView);

            }
        };

        Paper.init(this);


        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_orange_dark,android.R.color.holo_orange_dark,android.R.color.holo_orange_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else
                {
                   // Toast.makeText(getBaseContext(),"Please check internet connection",Toast.LENGTH_SHORT).show();
                    Toasty.error(getBaseContext(),"Please check internet connection",Toast.LENGTH_SHORT,true).show();
                    return;
                }

            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else
                {
                   // Toast.makeText(getBaseContext(),"Please check internet connection",Toast.LENGTH_SHORT).show();
                    Toasty.error(getBaseContext(),"Please check internet connection",Toast.LENGTH_SHORT,true).show();
                    return;
                }

            }
        });

         fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cartIntent=new  Intent(Home.this,Cart.class);
                startActivity(cartIntent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);




                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView=navigationView.getHeaderView(0);

        //set name on top
        txtFullname=(TextView) headerView.findViewById(R.id.txtFullName);
        txtFullname.setText(Common.currentUser.getName());
        //loading the menu
        recycler_menu=(RecyclerView)findViewById(R.id.recycler_menu);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller= AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);
        //register service
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //setup slider
        // need to call this function after initializing database 
        setupSlider();
    }

    private void setupSlider() {
        mSlider=(SliderLayout)findViewById(R.id.slider);
        image_list= new HashMap<>();
        //get banner from database
        final DatabaseReference banners=database.getReference("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Banner banner=postSnapShot.getValue(Banner.class);
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for (String key:image_list.keySet()){
                    String[] keySplit =key.split("@@@");
                    String nameOfFood=keySplit[0];
                    String idOfFood=keySplit[1];

                    //Create slider
                    final TextSliderView textSliderView= new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent= new Intent(Home.this,FoodDetail.class);
                                    //send food id to food details class
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);
                                }
                            });

                    //extra bundles
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idOfFood);
                    mSlider.addSlider(textSliderView);
                    //remove event after finish
                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.ZoomOut);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }


    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token, false);//
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {


        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


        //animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
       //fix
        if (adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.menu_search)

            startActivity(new Intent(Home.this,SearchActivity.class));
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);




        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {


            Intent cartIntent= new Intent(Home.this,Cart.class);
            startActivity(cartIntent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

        } else if (id == R.id.nav_orders) {

            Intent orderIntent= new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

         } else if (id == R.id.nav_signout) {

            AccountKit.logOut();
            Intent signIn=new  Intent(Home.this,MainActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

        }
        else if (id == R.id.nav_change_pwd){

            showChangePasswordDialog();
        }
        else if (id == R.id.nav_home_address){

            showHomeAddressDialog();
        }
        else if (id == R.id.nav_settings){

            showSettingsDialog();
        }else if (id == R.id.nav_fav){

      startActivity(new Intent(Home.this,FavoritesActivity.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("SETTINGS");

        LayoutInflater inflater=LayoutInflater.from(this);
        View layout_setting= inflater.inflate(R.layout.setting_layout,null);
        final CheckBox ckb_suscribe_news=(CheckBox)layout_setting.findViewById(R.id.ckb_sub_news);
        //remember state of checkbox using paper.db
        Paper.init(this);
        String isSubscribe=Paper.book().read("sub_new");
        if (isSubscribe==null || TextUtils.isEmpty(isSubscribe)||isSubscribe.equals("false")){
           ckb_suscribe_news.setChecked(false);
        }else {
            ckb_suscribe_news.setChecked(true);
        }
        alertDialog.setView(layout_setting);

        alertDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //sett new home address
               if (ckb_suscribe_news.isChecked()){
                   FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                   //write value to paper.db
                   Paper.book().write("sub_new","true");
               }else {
                   FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                   //write value to paper.db
                   Paper.book().write("sub_new","false");
               }
            }

        });
        alertDialog.show();
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("SET HOME ADDRESS");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater=LayoutInflater.from(this);
        View layout_home= inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeAddress=(MaterialEditText)layout_home.findViewById(R.id.edtHomeAddress);
        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //sett new home address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());
                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                               // Toast.makeText(Home.this, "Address Saved", Toast.LENGTH_SHORT).show();
                                Toasty.success(Home.this,"Address saved",Toast.LENGTH_SHORT,true).show();

                            }
                        });

            }

        });
        alertDialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater=LayoutInflater.from(this);
        View layout_pwd= inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword=(MaterialEditText)layout_pwd.findViewById(R.id.edtRepeatPassword);
        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final android.app.AlertDialog waitingDialog= new SpotsDialog(Home.this);
                waitingDialog.show();

                if (edtPassword.getText().toString().equals(Common.currentUser.getPassword()))
                {
                    //check new and repeat if they are de same
                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                    {
                        Map<String,Object>passwordUpdate =new HashMap<>();
                        passwordUpdate.put("Password",edtNewPassword.getText().toString());

                        //Make an update
                        DatabaseReference user=FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                       // Toast.makeText(Home.this,"Password Changed",Toast.LENGTH_SHORT).show();
                                        Toasty.success(Home.this,"Password Changed",Toast.LENGTH_SHORT,true).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                       // Toast.makeText(Home.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        Toasty.error(Home.this,e.getMessage(),Toast.LENGTH_SHORT,true).show();
                                    }
                                });

                    }else {
                        waitingDialog.dismiss();
                       // Toast.makeText(Home.this,"Password doesnt match",Toast.LENGTH_SHORT).show();
                        Toasty.error(Home.this,"Password doesnt match",Toast.LENGTH_SHORT,true).show();
                    }

                }else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this,"wrong old password",Toast.LENGTH_SHORT).show();
                    Toasty.error(Home.this,"wrong old password",Toast.LENGTH_SHORT,true).show();
                }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }


}
