package com.snyper.keeva;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.snyper.keeva.Common.Common;
import com.snyper.keeva.Service.PreferenceManager;
import com.snyper.keeva.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_COODE =7171;
    Button btnContinue;
    TextView txtSlogan;

    FirebaseDatabase database;
    DatabaseReference users;


    private PreferenceManager prefManager;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(this);

        setContentView(R.layout.activity_main);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        
        
        

        btnContinue=(Button) findViewById(R.id.btn_continue);

        txtSlogan=(TextView) findViewById(R.id.txtSlogan);
       // printKeyHash();


        //init firebase
        database=FirebaseDatabase.getInstance();
        users=database.getReference("User");

        Typeface face= Typeface.createFromAsset(getAssets(),"fonts/RobotoCondensed-Regular.ttf");
        txtSlogan.setTypeface(face);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginSystem();
            }
        });

        if (AccountKit.getCurrentAccessToken()!=null){
            //show dialog
            final AlertDialog waitingDialog= new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("Please Wait");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User localUser=dataSnapshot.getValue(User.class);
                                    Intent homeIntent = new Intent(MainActivity.this,Home.class);
                                    Common.currentUser = localUser;

                                    startActivity(homeIntent);
                                    //dismiss dialog
                                    waitingDialog.dismiss();
                                    finish();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }

    }



    private void startLoginSystem() {
        Intent intent=new Intent(MainActivity.this,AccountKitActivity.class);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder= new
                AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_COODE);
    }

    private void printKeyHash() {
        try{
            PackageInfo info=getPackageManager().getPackageInfo("com.snyper.keeva",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature:info.signatures)
            {
                MessageDigest md=MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_COODE){
            AccountKitLoginResult result=data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError()!=null){
               // Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                Toasty.error(this,""+result.getError().getErrorType().getMessage(),Toast.LENGTH_SHORT,true).show();
                return;
            }
            else if(result.wasCancelled()){
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                Toasty.warning(this,"Cancelled",Toast.LENGTH_SHORT,true).show();
            }
            else {
                if (result.getAccessToken()!=null){
                    //show dialog
                    final AlertDialog waitingDialog= new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Please Wait");
                    waitingDialog.setCancelable(false);

                    //get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone=account.getPhoneNumber().toString();
                            //checling if user exist in firebase
                            users.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userPhone).exists()){
                                                //creating new user nd login
                                                User newUser= new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName("");
                                                newUser.setBalance(String.valueOf(0.0));
                                                //add to firebase
                                                users.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                   // Toast.makeText(MainActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                                                    Toasty.success(MainActivity.this,"User registered succesfully",Toast.LENGTH_SHORT).show();
                                                                    //Do login
                                                                    users.child(userPhone)
                                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    User localUser=dataSnapshot.getValue(User.class);
                                                                                    Intent homeIntent = new Intent(MainActivity.this, UserName.class);
                                                                                    Common.currentUser =localUser;
                                                                                    startActivity(homeIntent);
                                                                                    //dismiss dialog
                                                                                    waitingDialog.dismiss();
                                                                                    finish();


                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });


                                            }
                                            else {//do login
                                                users.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                User localUser=dataSnapshot.getValue(User.class);
                                                                Intent homeIntent = new Intent(MainActivity.this, Home.class);
                                                                Common.currentUser = localUser;

                                                                startActivity(homeIntent);
                                                                //dismiss dialog
                                                                waitingDialog.dismiss();
                                                                finish();


                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

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
                        public void onError(AccountKitError accountKitError) {
                           // Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                            Toasty.error(MainActivity.this,""+accountKitError.getErrorType().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}
