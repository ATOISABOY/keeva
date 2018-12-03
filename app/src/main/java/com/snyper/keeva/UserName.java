package com.snyper.keeva;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Service.PreferenceManager;
import com.snyper.keeva.model.User;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class UserName extends AppCompatActivity {

    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        prefManager = new PreferenceManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            Intent homeIntent = new Intent(UserName.this, Home.class);
            startActivity(homeIntent);
            finish();
        }

        final MaterialEditText edtUserName=(MaterialEditText)findViewById(R.id.edtHomeAddress);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mandem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){


                Map<String,Object> update_name= new HashMap<>();
                update_name.put("name",edtUserName.getText().toString());
                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                prefManager.setFirstTimeLaunch(false);
                                Toasty.success(UserName.this,"Name saved successfully",Toast.LENGTH_SHORT,true).show();
                                Intent homeIntent = new Intent(UserName.this, Home.class);
                                startActivity(homeIntent);
                                finish();
                            }
                        });


            }
        });

    }




}
