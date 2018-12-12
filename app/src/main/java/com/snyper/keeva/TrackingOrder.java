package com.snyper.keeva;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Helper.DirectionJSONParser;
import com.snyper.keeva.Remote.IGoogleService;
import com.snyper.keeva.model.Request;
import com.snyper.keeva.model.ShippingInformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,ValueEventListener {

    private GoogleMap mMap;

    FirebaseDatabase database;
    DatabaseReference requests,shippingOrder;

    Request currentOrder;
    IGoogleService mService;
    Marker shippingMarker;

    Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");
        shippingOrder=database.getReference("ShippingOrders");

        shippingOrder.addValueEventListener(this);

        mService=Common.getGoogleMapAPI();
    }

    @Override
    protected void onStop() {
        shippingOrder.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        trackingLocation();
    }

    private void trackingLocation() {
        requests.child(Common.currentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentOrder=dataSnapshot.getValue(Request.class);
                        if (currentOrder.getAddress()!=null&&!currentOrder.getAddress().isEmpty()){
                            mService.getLocationFromAddress("https://maps.googleapis.com/maps/api/geocode/json?address="+currentOrder.getAddress()+"&key=AIzaSyDFLR4ek5jvRhSPOLfPB4EGM_iTHq4XChA")
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                JSONObject jsonObject= new JSONObject(response.body());
                                                Toast.makeText(TrackingOrder.this, response.toString(), Toast.LENGTH_SHORT).show();
                                                String lat =((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();


                                                String lng =((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();
                                                Toast.makeText(TrackingOrder.this, lng, Toast.LENGTH_SHORT).show();

                                                final LatLng  location = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                                                mMap.addMarker(new MarkerOptions().position(location) .title("Order Destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                shippingOrder.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ShippingInformation shippingInformation=dataSnapshot.getValue(ShippingInformation.class);
                                                                LatLng shipperLocation= new LatLng(shippingInformation.getLat(),shippingInformation.getLng());
                                                                if (shippingMarker==null)
                                                                {
                                                                    shippingMarker=mMap.addMarker(
                                                                            new MarkerOptions()
                                                                                    .position(shipperLocation)
                                                                                    .title("Shipper #"+shippingInformation.getOrderId())
                                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

                                                                    );
                                                                }else {
                                                                    shippingMarker.setPosition(shipperLocation);
                                                                }
                                                                CameraPosition cameraPosition= new CameraPosition.Builder()
                                                                        .target(shipperLocation)
                                                                        .zoom(16)
                                                                        .bearing(0)
                                                                        .tilt(45)
                                                                        .build();
                                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                if (polyline!=null)
                                                                    polyline.remove();
                                                                mService.getDirections("https://maps.googleapis.com/maps/api/directions/json?origin="+shipperLocation.latitude+","+shipperLocation.longitude+"&destination="+currentOrder.getAddress()+"&key=AIzaSyDFLR4ek5jvRhSPOLfPB4EGM_iTHq4XChA")
                                                                        .enqueue(new Callback<String>() {
                                                                            @Override
                                                                            public void onResponse(Call<String> call, Response<String> response) {
                                                                                new ParserTask().execute(response.body().toString());
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<String> call, Throwable t) {

                                                                            }
                                                                        });
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });

                        }else if (currentOrder.getLatLng()!=null&&!currentOrder.getLatLng().isEmpty()){

                            mService.getLocationFromAddress(new StringBuilder("https://maps.googleapis.com/maps/api/geocode/json?latlng=&key=AIzaSyDFLR4ek5jvRhSPOLfPB4EGM_iTHq4XChA")
                                    .append(currentOrder.getLatLng()).toString())
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try {
                                                JSONObject jsonObject= new JSONObject(response.body());
                                                Toast.makeText(TrackingOrder.this, response.toString(), Toast.LENGTH_SHORT).show();
                                                String lat =((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();


                                                String lng =((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();
                                                Toast.makeText(TrackingOrder.this, lng, Toast.LENGTH_SHORT).show();

                                                final LatLng  location = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                                                mMap.addMarker(new MarkerOptions().position(location) .title("Order Destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                shippingOrder.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ShippingInformation shippingInformation=dataSnapshot.getValue(ShippingInformation.class);
                                                                LatLng shipperLocation= new LatLng(shippingInformation.getLat(),shippingInformation.getLng());
                                                                if (shippingMarker==null)
                                                                {
                                                                    shippingMarker=mMap.addMarker(
                                                                            new MarkerOptions()
                                                                                    .position(shipperLocation)
                                                                                    .title("Shipper #"+shippingInformation.getOrderId())
                                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

                                                                    );
                                                                }else {
                                                                    shippingMarker.setPosition(shipperLocation);
                                                                }
                                                                CameraPosition cameraPosition= new CameraPosition.Builder()
                                                                        .target(shipperLocation)
                                                                        .zoom(16)
                                                                        .bearing(0)
                                                                        .tilt(45)
                                                                        .build();
                                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                if (polyline!=null)
                                                                    polyline.remove();
                                                                mService.getDirections("https://maps.googleapis.com/maps/api/direction/json?origin="+shipperLocation.latitude+","+shipperLocation.longitude+"&destination="+currentOrder.getLatLng()+"&key=AIzaSyDFLR4ek5jvRhSPOLfPB4EGM_iTHq4XChA")
                                                                        .enqueue(new Callback<String>() {
                                                                            @Override
                                                                            public void onResponse(Call<String> call, Response<String> response) {
                                                                                new ParserTask().execute(response.body().toString());
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<String> call, Throwable t) {

                                                                            }
                                                                        });
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        trackingLocation();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


    public class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {

       AlertDialog mDialog= new SpotsDialog(TrackingOrder.this);

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            mDialog.show();
            mDialog.setMessage("Please Wait..");
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {


            JSONObject jObject;
            List<List<HashMap<String,String>>> routes=null;

            try {
                jObject= new JSONObject(strings[0]);

                DirectionJSONParser parser= new DirectionJSONParser();

                routes= parser.parse(jObject);

            }catch (JSONException e){

                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>>lists){
            mDialog.dismiss();

            ArrayList<LatLng> points;
            PolylineOptions lineOptions= null;
            LatLng position = null;
            for (int i=0;i<lists.size();i++){
                points= new ArrayList<>();
                lineOptions= new PolylineOptions();

                List<HashMap<String,String>> path= lists.get(i);

                for (int j=0;j<path.size();j++){

                    HashMap<String,String> point= path.get(j);


                    double lat =Double.parseDouble(point.get("lat"));
                    double lng =Double.parseDouble(point.get("lng"));

                    position= new LatLng(lat,lng);

                    points.add(position);
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
           polyline=mMap.addPolyline(lineOptions);
        }
    }
}
