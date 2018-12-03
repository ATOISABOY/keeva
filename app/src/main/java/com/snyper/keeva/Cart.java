package com.snyper.keeva;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Database.Database;
import com.snyper.keeva.Helper.RecyclerItemTouchHelper;
import com.snyper.keeva.Interface.RecyclerItemTouchHelperListener;
import com.snyper.keeva.Remote.APIService;
import com.snyper.keeva.Remote.IGoogleService;
import com.snyper.keeva.ViewHolder.CartAdapter;
import com.snyper.keeva.ViewHolder.CartViewHolder;
import com.snyper.keeva.model.DataMessage;
import com.snyper.keeva.model.MyResponse;
import com.snyper.keeva.model.Order;
import com.snyper.keeva.model.Request;
import com.snyper.keeva.model.Token;
import com.snyper.keeva.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener, RecyclerItemTouchHelperListener {


    ShimmerRecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;


    FirebaseDatabase database;
    DatabaseReference requests;

     public  TextView txtTotalPrice;
      FButton   btnPlace;

      List<Order> cart= new ArrayList<>();
      CartAdapter adapter;

      APIService mService;

      Place shippingAddress;
      String address,comment;


      //location
    private LocationRequest mLoacationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    final static int REQUEST_LOCATION = 199;

    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=3000;
    private static final int DISPLACEMENT=10;

    private static final int LOCATION_REGUEST_CODE=9999;

    private static final int PLAY_SERVICES_REQUEST=9997;
    IGoogleService mGoogleMapService;

    RelativeLayout rootLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        //Init service
        mService= Common.getFCMService();
        rootLayout=(RelativeLayout)findViewById(R.id.rootsLayout);

        //swipe to delete

        //initialize googleservice
        mGoogleMapService=Common.getGoogleMapAPI();


        //runtime location permission
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this,new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REGUEST_CODE
                    );
        }
        else {
            //check if user has play services on fone
            if (checkPlayServices()){

                buildGoogleApiClient();
                createLocationRequest();

            }

        }



        recyclerView=(ShimmerRecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.showShimmerAdapter();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback= new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice=(TextView)findViewById(R.id.total);
        btnPlace=(FButton)findViewById(R.id.btnPlaceOrder);

        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadListFood();
            }
        }, 3000);




             btnPlace.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if (cart.size()>0)
                         showAlertDialog();
                     else
                        // Toast.makeText(Cart.this,"Cart is empty",Toast.LENGTH_SHORT).show();
                     Toasty.warning(Cart.this,"Cart is empty",Toast.LENGTH_SHORT,true).show();

                 }
             });





    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("onActivityResult()", Integer.toString(resultCode));

        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    {
                        // All required changes were successfully made
                       // Toast.makeText(Cart.this, "Location enabled by user!", Toast.LENGTH_LONG).show();
                        Toasty.info(Cart.this ,"Location enabled by user",Toast.LENGTH_LONG,true).show();
                        break;
                    }
                    case Activity.RESULT_CANCELED:
                    {
                        // The user was asked to change settings, but chose not to
                       // Toast.makeText(Cart.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        Toasty.warning(Cart.this ,"Location not enabled, user cancelled.",Toast.LENGTH_LONG,true).show();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                break;
        }
    }

    private void createLocationRequest() {
        mLoacationRequest= new LocationRequest();
        mLoacationRequest.setInterval(UPDATE_INTERVAL);
        mLoacationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLoacationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLoacationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder()
                .addLocationRequest(mLoacationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(Cart.this,REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });



    }


    private  synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
            mGoogleApiClient.connect();

        }
    }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode!= ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            else {
               // Toast.makeText(this,"Your device is not supported",Toast.LENGTH_SHORT).show();
                Toasty.error(this,"Your device is not supported",Toast.LENGTH_SHORT,true).show();

                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlertDialog() {


        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more Step");
        alertDialog.setMessage("Enter Address");

        LayoutInflater inflater= this.getLayoutInflater();
        View order_address_comment= inflater.inflate(R.layout.order_address_comment,null);


        //final MaterialEditText adtComment= (MaterialEditText)order_address_comment.findViewById(R.id.adtComment);
        final PlaceAutocompleteFragment edtAddress=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        //hide search icon before fragment
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

        //set hint for Autocomplete edit text
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");

        //set size of text
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        //getting address from place autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shippingAddress=place;
            }

            @Override
            public void onError(Status status) {
                Log.e("ERROR",status.getStatusMessage());
            }
        });


        final MaterialEditText adtComment= (MaterialEditText)order_address_comment.findViewById(R.id.adtComment);
        //radio
        final RadioButton rdiShipToAddress=(RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress=(RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD=(RadioButton) order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiKeevaWallet=(RadioButton) order_address_comment.findViewById(R.id.rdiKeevaWallet);

        rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                   if (Common.currentUser.getHomeAddress()!=null ||!TextUtils.isEmpty(Common.currentUser.getHomeAddress())){
                       address=Common.currentUser.getHomeAddress();
                       ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                               .setText(address);
                       Toast.makeText(Cart.this, "address beside this text"+address, Toast.LENGTH_SHORT).show();
                   }else {
                      // Toast.makeText(Cart.this, "Please set home Address", Toast.LENGTH_SHORT).show();
                       Toasty.error(Cart.this,"Please set home Address",Toast.LENGTH_SHORT,true).show();
                   }
                }
            }
        });

        rdiShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (b){
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&key=AIzaSyDFLR4ek5jvRhSPOLfPB4EGM_iTHq4XChA",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try{
                                        JSONObject jsonObject=new JSONObject(response.body().toString());
                                        JSONArray resultsArray= jsonObject.getJSONArray("results");
                                       // Log.d("ADDRESS*************","Your Location: "+resultsArray);
                                        //Toast.makeText(Cart.this, "address beside this text"+resultsArray.toString(), Toast.LENGTH_SHORT).show();
                                        JSONObject firstobject= resultsArray.getJSONObject(0);
                                        address=firstobject.getString("formatted_address");

                                        //set this address to edtAddress

                                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);
                                        Toast.makeText(Cart.this, "address beside this text"+address, Toast.LENGTH_SHORT).show();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                    Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //check condition is here
                //if user select address frm placefragment, just use it or if user select ship to address ,get address from location and use it
                //if user select home address get home address from profile and use it for orders
                if (!rdiShipToAddress.isChecked()&&!rdiHomeAddress.isChecked()){
                    //Log.d("ADDRESS","Your Location: "+address);
                    if (shippingAddress!=null){
                        address=shippingAddress.getAddress().toString();
                       // Log.d("ADDRESS","Your Location: "+address);
                    }else {
                        Toast.makeText(Cart.this, "shippingAdress is null", Toast.LENGTH_SHORT).show();
                       // Log.d("ADDRESS","NO ADDRESS FOUND ");
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                .commit();
                        return;
                    }
                }
                if(TextUtils.isEmpty(address)){
                    Toast.makeText(Cart.this, "Textutils is empty", Toast.LENGTH_SHORT).show();
                   // Log.d("ADDRESS","NO ADDRESS FOUND ");
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }

                if(!rdiCOD.isChecked()&& !rdiKeevaWallet.isChecked()){
                    Toast.makeText(Cart.this, "Payment method is empty", Toast.LENGTH_SHORT).show();
                    Toasty.error(Cart.this,"Payment method is empty",Toast.LENGTH_SHORT,true).show();

                    // Log.d("ADDRESS","NO ADDRESS FOUND ");
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                            .commit();
                    return;
                }else if (rdiCOD.isChecked()){
                    Request request = new Request(

                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            address,
                            txtTotalPrice.getText().toString(),
                            "0",
                            cart,
                            adtComment.getText().toString(),"COD"
                            ,"Unpaid",
                            String.format("%s,%s", mLastLocation.getLatitude(),mLastLocation.getLongitude())

                    );
                    //submit to fire base
                    //  using system.Currentmili to key
                    String order_number=String.valueOf(System.currentTimeMillis());
                    requests.child(order_number).setValue(request);
                    //Delete cart
                    new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());
                    sendNotificationOrder(order_number);


                   // Toast.makeText(Cart.this, "Thank you, Order Placed", Toast.LENGTH_SHORT).show();
                    Toasty.success(Cart.this,"Thank you, Order Placed",Toast.LENGTH_SHORT,true).show();
                    finish();

                }else if (rdiKeevaWallet.isChecked()){
                    double amount= 0;
                    //get total price from txtTotalprice
                    try {
                        amount=Common.formatCurrency(txtTotalPrice.getText().toString(),Locale.US).doubleValue();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (Double.parseDouble(Common.currentUser.getBalance().toString())>=amount)
                    {
                        Request request = new Request(

                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                cart,
                                adtComment.getText().toString(),"Keeva Wallet"
                                ,"Paid",
                                String.format("%s,%s", mLastLocation.getLatitude(),mLastLocation.getLongitude())

                        );
                        //submit to fire base
                        //  using system.Currentmili to key
                        final String order_number=String.valueOf(System.currentTimeMillis());
                        requests.child(order_number).setValue(request);
                        //Delete cart
                        new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                        //update balance
                        double balance=Double.parseDouble(Common.currentUser.getBalance().toString())-amount;
                        Map<String,Object> update_balance=new HashMap<>();
                        update_balance.put("balance",balance);

                        FirebaseDatabase.getInstance()
                                .getReference("User")
                                .child(Common.currentUser.getPhone())
                                .updateChildren(update_balance)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful()){
                                       //refresh user
                                       FirebaseDatabase.getInstance()
                                               .getReference("User")
                                               .child(Common.currentUser.getPhone())
                                               .addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                       Common.currentUser=dataSnapshot.getValue(User.class);
                                                       //send orders to enterprise
                                                       sendNotificationOrder(order_number);
                                                   }

                                                   @Override
                                                   public void onCancelled(DatabaseError databaseError) {

                                                   }
                                               });
                                   }
                                    }
                                });



                      //  Toast.makeText(Cart.this, "Thank you, Order Placed", Toast.LENGTH_SHORT).show();
                        Toasty.success(Cart.this,"Thank you, Order Placed",Toast.LENGTH_SHORT,true).show();
                        finish();
                    }else {
                       // Toast.makeText(Cart.this, "Sorry, You dont have enough balance", Toast.LENGTH_SHORT).show();
                        Toasty.error(Cart.this,"Sorry, You dont have enough balance",Toast.LENGTH_SHORT,true).show();
                    }

                }






            }
        });


        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                 dialogInterface.dismiss();
                 //remove fragment
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                         .commit();
            }
        });



        alertDialog.show();
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference("Tokens");
        Query data=tokens.orderByChild("isServerToken").equalTo(true);///get ll node with isservertoken is true
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Token  serverToken=postSnapShot.getValue(Token.class);

                 //   Notification notification= new Notification("Keeva","You have a new order"+order_number);
                   // Sender content= new Sender(serverToken.getToken(),notification);

                    Map<String,String> dataSend= new HashMap<>();
                    dataSend.put("title","Keeva");
                    dataSend.put("message","You have a new order"+order_number);
                    DataMessage dataMessage=new DataMessage(serverToken.getToken(),dataSend);

                    String test=new Gson().toJson(dataMessage);
                    Log.d("Content",test);

                    mService.sendNotification(dataMessage)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.code()==200) {//avoid crash so only run if get result
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Thank you, Order Placed", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Failed", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Log.e("Error",t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {

        cart= new Database(this).getCarts(Common.currentUser.getPhone());
        adapter=new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculation total price
        float total =0;
        for (Order order:cart)
            total+=(Float.parseFloat(order.getPrice()))*(Float.parseFloat(order.getQuality()));
        Locale locale= new Locale("en","US");
        NumberFormat fmt= NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));
        recyclerView.hideShimmerAdapter();
    }
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder){
            String name=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem=((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex=viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(),Common.currentUser.getPhone());

            //update the cart
            //calculation total price
            float total =0;
            List<Order> orders= new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for (Order item:orders)
                total+=(Float.parseFloat(item.getPrice()))*(Float.parseFloat(item.getQuality()));
            Locale locale= new Locale("en","US");
            NumberFormat fmt= NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));

            //make snackbar
            Snackbar snackbar=Snackbar.make(rootLayout,name+"removed from cart!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);
                    //update the cart
                    //calculation total price
                    float total =0;
                    List<Order> orders= new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for (Order item:orders)
                        total+=(Float.parseFloat(item.getPrice()))*(Float.parseFloat(item.getQuality()));
                    Locale locale= new Locale("en","US");
                    NumberFormat fmt= NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REGUEST_CODE:
            {
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices()){

                        buildGoogleApiClient();
                        createLocationRequest();


                    }
                }
            }
        }
    }

    private void deleteCart(int position) {
        cart.remove(position);

        new Database(this).cleanCart(Common.currentUser.getPhone());
        //update new data from list<> to sqlite
        for (Order item:cart)
            new Database(this).addToCart(item);

        //refresh
        loadListFood();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLoacationRequest,this);
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation!=null)
        {
            Log.d("LOCATION","Your Location: "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }else
        {
            Log.d("LOCATION","cant get your location" );
        }
        
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation= location;
        displayLocation();

    }


}
