package com.prescryp.lance;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RideRatingActivity extends AppCompatActivity {

    private RatingBar mRatingBar;
    private EditText mRideRatingComments;
    private String driverId, rideId;
    private DatabaseReference historyRideInfoDb;
    private CircleImageView mDriverProfileImage;
    private TextView mDriverName, mAmbulanceType;
    private Float ratingGiven;
    private CardView mSubmit;
    private Toolbar toolbar;
    private String startedFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_rating);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        mRatingBar = findViewById(R.id.rideRating);
        mRideRatingComments = findViewById(R.id.rideRatingComments);
        mDriverProfileImage = findViewById(R.id.driverProfileImage);
        mDriverName = findViewById(R.id.driverName);
        mAmbulanceType = findViewById(R.id.ambulanceType);
        mSubmit = findViewById(R.id.submit);

        if (getIntent() != null){
            rideId = getIntent().getStringExtra("rideId");
            startedFrom = getIntent().getStringExtra("startedFrom");

        }
        if (startedFrom.equalsIgnoreCase("RideHistorySingleActivity")){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
        ratingGiven = 0F;

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyRideInfoDb.child("rating").setValue(ratingGiven);
                if (!TextUtils.isEmpty(mRideRatingComments.getText().toString())){
                    historyRideInfoDb.child("ratingComment").setValue(mRideRatingComments.getText().toString());
                }else {
                    historyRideInfoDb.child("ratingComment").setValue("No Comment");
                }
                DatabaseReference mDriverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                mDriverRatingDb.child(rideId).setValue(ratingGiven).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(RideRatingActivity.this, BookAmbulanceActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }

    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        if (child.getKey().equals("driver")){
                            driverId = child.getValue().toString();
                            getDriverInformation(driverId);
                            getCustomerRelatedObject();
                        }if (child.getKey().equals("timestamp")){
                            toolbar.setSubtitle(getDate(Long.valueOf(child.getValue().toString())));
                        }if (child.getKey().equals("rating")){
                            mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd/MM/yyyy hh:mm", calendar).toString();
        return date;
    }


    private void getDriverInformation(String driverId) {
        DatabaseReference driverUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
        driverUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        String driver_name_text = map.get("name").toString();
                        mDriverName.setText(driver_name_text);
                    }
                    if (map.get("ambulance_type") != null){
                        mAmbulanceType.setText(map.get("ambulance_type").toString());
                    }
                    if (map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mDriverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerRelatedObject() {
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingGiven = rating;
            }
        });
    }

}
