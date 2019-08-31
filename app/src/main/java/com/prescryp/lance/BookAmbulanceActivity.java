package com.prescryp.lance;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.maps.android.SphericalUtil;
import com.prescryp.lance.Session.DestinationLocationSessionManager;
import com.prescryp.lance.Session.MobileNumberSessionManager;
import com.prescryp.lance.Session.PickupLocationSessionManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookAmbulanceActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private ConstraintLayout request_ambulance_for_you, confirm_booking, request_ambulance_for_others, requestMobileNumberLayout;
    private String userId;
    private Location mLastLocation;
    private static String TAG = "BookAmbulanceActivity";

    private static final float DEFAULT_ZOOM = 16f;

    private LatLng pickupLocation, destinationLocation, pickup_location_latlng, destination_location_latlng;
    private TextView requestForYouText, requestForOthersText;
    private Boolean requestBool = false;
    private String destination_address, pickup_address;

    private LocationRequest mLocationRequest;

    PickupLocationSessionManager pickupLocationSessionManager;
    DestinationLocationSessionManager destinationLocationSessionManager;

    private ConstraintLayout select_ambulance_type;

    Geocoder geocoder;
    List<Address> addresses;

    private ConstraintLayout mRideStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String mName, mPhone, mProfileImageUrl, requestAmbulanceType, requestMobNumForOthers;
    private CircleImageView profileImage;
    private TextView fullName, phoneNumber, pickupLocationName;
    private CardView pickup_location, destination_location, myLocation;
    private Boolean showPickupMarker = true;
    private Boolean showDestinationMarker = false;
    private ImageView destination_pin, pickup_pin, destinationFilled, destinationNotFilled;
    private TextView enter_hospital_location, drop_at, destinationLocationName, mEstimatedPriceText, mDestinationTimeText;

    private RadioGroup ambulance_type;

    private FusedLocationProviderClient mFusedLocationClient;

    private LatLngBounds bounds;
    private int rideStatus = 1, rideStatusForOther = 1;
    private ConstraintLayout mSetupPaymentLayout;
    private ImageView mSetupPaymentImage;
    private TextView mSetupPaymentText;
    private EditText requestMobileNumberEditText;

    private Boolean isPaymentSetup = false;
    private String for_who;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ambulance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pickupLocationName = findViewById(R.id.pickupLocationName);
        pickup_location = findViewById(R.id.pickup_location);
        destination_location = findViewById(R.id.destination_location);

        destination_pin = findViewById(R.id.destination_pin);
        pickup_pin = findViewById(R.id.pickup_pin);
        pickup_pin.setVisibility(View.GONE);

        destinationNotFilled = findViewById(R.id.destinationNotFilled);
        destinationFilled = findViewById(R.id.destinationFilled);

        enter_hospital_location = findViewById(R.id.enter_hospital_location);
        drop_at = findViewById(R.id.drop_at);
        destinationLocationName = findViewById(R.id.destinationLocationName);

        requestForYouText = findViewById(R.id.requestForYouText);
        requestForOthersText = findViewById(R.id.requestForOthersText);
        request_ambulance_for_you = findViewById(R.id.request_ambulance_for_you);
        confirm_booking = findViewById(R.id.confirm_booking);
        request_ambulance_for_others = findViewById(R.id.request_ambulance_for_others);
        requestMobileNumberLayout = findViewById(R.id.requestMobileNumberLayout);

        requestMobileNumberEditText = findViewById(R.id.requestMobileNumberEditText);

        mRideStatus = findViewById(R.id.driverInfo);

        ambulance_type = findViewById(R.id.ambulance_type);
        ambulance_type.check(R.id.BLS);
        select_ambulance_type = findViewById(R.id.select_ambulance_type);

        myLocation = findViewById(R.id.myLocation);

        mEstimatedPriceText = findViewById(R.id.estimatedPriceText);
        mDestinationTimeText = findViewById(R.id.destinationTimeText);

        mSetupPaymentLayout = findViewById(R.id.setupPaymentLayout);
        mSetupPaymentImage = findViewById(R.id.setupPaymentImage);
        mSetupPaymentText = findViewById(R.id.setupPaymentText);

        polylines = new ArrayList<>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        pickupLocationSessionManager = new PickupLocationSessionManager(getApplicationContext());
        destinationLocationSessionManager = new DestinationLocationSessionManager(getApplicationContext());

        if (getIntent().getStringExtra("LOCATION_FOR") != null){
            if (getIntent().getStringExtra("LOCATION_FOR").equalsIgnoreCase("PICKUP")){
                showPickupMarker = true;
                showDestinationMarker = false;
            }else if (getIntent().getStringExtra("LOCATION_FOR").equalsIgnoreCase("DESTINATION")){
                showDestinationMarker = true;
                showPickupMarker = false;
            }
        }


        pickup_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showPickupMarker){
                    Intent intent = new Intent(BookAmbulanceActivity.this, SearchLocationActivity.class);
                    intent.putExtra("LOCATION_FOR", "PICKUP");
                    intent.putExtra("currentLatitude", String.valueOf(mLastLocation.getLatitude()));
                    intent.putExtra("currentLongitude", String.valueOf(mLastLocation.getLongitude()));
                    startActivity(intent);
                }else {
                    getPickupLocation();
                    if (!requestBool && rideStatus != 2){
                        myLocation.setVisibility(View.VISIBLE);
                    }

                }

            }
        });

        destination_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showDestinationMarker){
                    Intent intent = new Intent(BookAmbulanceActivity.this, SearchLocationActivity.class);
                    intent.putExtra("LOCATION_FOR", "DESTINATION");
                    intent.putExtra("currentLatitude", String.valueOf(mLastLocation.getLatitude()));
                    intent.putExtra("currentLongitude", String.valueOf(mLastLocation.getLongitude()));
                    startActivity(intent);
                }else {
                    getDestinationLocation();
                    myLocation.setVisibility(View.GONE);
                }

            }
        });

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(currentLocation)      // Sets the center of the map to Mountain View
                        .zoom(DEFAULT_ZOOM)             // Sets the orientation of the camera to east
                        /*.tilt(45) */                  // Sets the tilt of the camera to 30 degrees
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
                pickupPinChange = false;
            }
        });

        mSetupPaymentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSetupPaymentPopup();
            }
        });


        LatLng center = new LatLng(22.5726, 88.3639);

        LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
        LatLng westSide = SphericalUtil.computeOffset(center, 100000, 90);
        LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);
        LatLng eastSide = SphericalUtil.computeOffset(center, 100000, 270);

        bounds = LatLngBounds.builder().include(northSide).include(southSide).include(westSide).include(eastSide).build();

        rejectedDrivers = new ArrayList<>();

        request_ambulance_for_you.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!destinationLocationSessionManager.isDestinationLocationLoggedIn()){
                    Snackbar snackbar = Snackbar.make(v, "Select Destination", Snackbar.LENGTH_SHORT);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                    snackbar.show();
                }else {
                    if (bounds.contains(new LatLng(pickupLocation.latitude, pickupLocation.longitude))){
                        if (!requestBool){
                            switch (rideStatus){
                                case 1:
                                    rideStatus = 2;
                                    request_ambulance_for_others.setVisibility(View.GONE);
                                    select_ambulance_type.setVisibility(View.GONE);
                                    confirm_booking.setVisibility(View.VISIBLE);
                                    changeMapCameraForConfirming();
                                    requestForYouText.setText("Confirm Booking");
                                    break;
                                case 2:
                                    if (isPaymentSetup){
                                        for_who = "For_Me";
                                        requestAmbulance();
                                    }else {
                                        Snackbar snackbar = Snackbar.make(v, "Please set your payment method", Snackbar.LENGTH_SHORT);
                                        View sbView = snackbar.getView();
                                        sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                                        snackbar.show();
                                    }
                                    break;
                            }

                        }
                    }else {
                        Snackbar snackbar = Snackbar.make(v, "We are not providing service in this location.", Snackbar.LENGTH_SHORT);
                        View sbView = snackbar.getView();
                        sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                        snackbar.show();
                    }

                }


            }
        });

        request_ambulance_for_others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!destinationLocationSessionManager.isDestinationLocationLoggedIn()){
                    Snackbar snackbar = Snackbar.make(v, "Select Destination", Snackbar.LENGTH_SHORT);
                    View sbView = snackbar.getView();
                    sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                    snackbar.show();
                }else {
                    switch (rideStatusForOther){
                        case 1:
                            rideStatusForOther = 2;
                            request_ambulance_for_you.setVisibility(View.GONE);
                            requestMobileNumberLayout.setVisibility(View.VISIBLE);
                            break;
                        case 2:
                            if (bounds.contains(new LatLng(pickupLocation.latitude, pickupLocation.longitude))){
                                if (!TextUtils.isEmpty(requestMobileNumberEditText.getText().toString())){
                                    if (!requestBool){
                                        rideStatusForOther = 3;
                                        requestMobNumForOthers = "+91" + requestMobileNumberEditText.getText().toString();
                                        requestMobileNumberLayout.setVisibility(View.GONE);
                                        select_ambulance_type.setVisibility(View.GONE);
                                        confirm_booking.setVisibility(View.VISIBLE);
                                        changeMapCameraForConfirming();
                                        requestForOthersText.setText("Confirm Booking");
                                    }
                                }else {
                                    Snackbar snackbar = Snackbar.make(v, "Enter Mobile Number For Booking", Snackbar.LENGTH_SHORT);
                                    View sbView = snackbar.getView();
                                    sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                                    snackbar.show();
                                }

                            }else {
                                Snackbar snackbar = Snackbar.make(v, "We are not providing service in this location.", Snackbar.LENGTH_SHORT);
                                View sbView = snackbar.getView();
                                sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                                snackbar.show();
                            }
                            break;
                        case 3:
                            if (isPaymentSetup){
                                for_who = "For_Other";
                                requestAmbulance();
                            }else {
                                Snackbar snackbar = Snackbar.make(v, "Please set your payment method", Snackbar.LENGTH_SHORT);
                                View sbView = snackbar.getView();
                                sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                                snackbar.show();
                            }
                            break;
                    }
                }

            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ConstraintLayout header = headerView.findViewById(R.id.header);
        profileImage = headerView.findViewById(R.id.profileImage);
        fullName = headerView.findViewById(R.id.fullName);
        phoneNumber = headerView.findViewById(R.id.phoneNumber);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookAmbulanceActivity.this, EditAccountActivity.class);
                startActivity(intent);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);

        getUserInformation();

        getCurrentRideInformation();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( BookAmbulanceActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                sendTokenToDatabase(newToken);

            }
        });

    }

    private String paymentMethod, paid;
    private void getSetupPaymentPopup() {
        TextView wallet_price;
        ImageView closeGroupBtn;
        ConstraintLayout card_layout, cash_layout;
        final Dialog dialog = new Dialog(BookAmbulanceActivity.this);
        dialog.setContentView(R.layout.setup_payment_popup);
        wallet_price = dialog.findViewById(R.id.wallet_price);
        card_layout = dialog.findViewById(R.id.card_layout);
        cash_layout = dialog.findViewById(R.id.cash_layout);
        closeGroupBtn = dialog.findViewById(R.id.closeGroupBtn);


        Locale locale = new Locale("hi", "IN");
        final NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        double wallet_money = 0.00;
        String wallet_money_shown = nf.format(wallet_money);
        wallet_price.setText(wallet_money_shown);
        card_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetupPaymentText.setText("CARD");
                mSetupPaymentText.setTextColor(R.color.black);
                mSetupPaymentImage.setImageResource(R.drawable.credit_card_black);
                dialog.dismiss();
                isPaymentSetup = true;
                paymentMethod = "CARD";
                paid = "YES";
            }
        });
        cash_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetupPaymentText.setText("CASH");
                mSetupPaymentText.setTextColor(R.color.black);
                mSetupPaymentImage.setImageResource(R.drawable.money);
                dialog.dismiss();
                isPaymentSetup = true;
                paymentMethod = "CASH";
                paid = "NO";
            }
        });

        closeGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private Marker destinationMarker;
    private void changeMapCameraForConfirming() {
        if (pickup_pin != null){
            pickup_pin.setVisibility(View.GONE);
        }
        if (destination_pin != null){
            destination_pin.setVisibility(View.GONE);
        }

        destination_location.setCardBackgroundColor(getResources().getColor(R.color.white));
        pickup_location.setCardBackgroundColor(getResources().getColor(R.color.white));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickup_location_latlng);
        builder.include(destination_location_latlng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.43);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cameraUpdate);

        BitmapDrawable greenPinBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_green_pin_foreground);
        Bitmap gb = greenPinBitmap.getBitmap();
        Bitmap smallGreenPinMarker = Bitmap.createScaledBitmap(gb, marker_width, marker_height, false);

        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickup_location_latlng).title("Pickup Location").icon(BitmapDescriptorFactory.fromBitmap(smallGreenPinMarker)));

        BitmapDrawable redPinBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_pin_foreground);
        Bitmap rb = redPinBitmap.getBitmap();
        Bitmap smallRedPinMarker = Bitmap.createScaledBitmap(rb, marker_width, marker_height, false);

        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination_location_latlng).title("Drop Here").icon(BitmapDescriptorFactory.fromBitmap(smallRedPinMarker)));

        getRouteToMarker(pickup_location_latlng, destination_location_latlng);
    }

    private void changeToInitialMapCamera() {
        erasePolylines();
        if (pickupMarker != null){
            pickupMarker.remove();
        }
        if (destinationMarker != null){
            destinationMarker.remove();
        }
        if (showPickupMarker){
            getPickupLocation();
        }else if (showDestinationMarker){
            getDestinationLocation();
            myLocation.setVisibility(View.GONE);
        }
    }


    private void requestAmbulance() {


        int selectedType = ambulance_type.getCheckedRadioButtonId();

        final RadioButton radioButton = findViewById(selectedType);

        if (radioButton.getText() == null){
            return;
        }

        requestAmbulanceType = radioButton.getText().toString();

        requestBool = true;

        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (for_who.equalsIgnoreCase("For_Me")){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(userId, new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), new GeoFire.CompletionListener(){

                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        }else if (for_who.equalsIgnoreCase("For_Other")){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customerRequest").child(userId).child("for_other").child(requestMobNumForOthers);
            GeoFire geoFire = new GeoFire(reference);
            geoFire.setLocation(userId, new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), new GeoFire.CompletionListener(){

                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        }

        if (currentRideRefListener != null){
            currentRideRef.removeEventListener(currentRideRefListener);
        }

        destination_pin.setVisibility(View.GONE);
        pickup_pin.setVisibility(View.GONE);
        myLocation.setVisibility(View.GONE);
        select_ambulance_type.setVisibility(View.GONE);
        confirm_booking.setVisibility(View.GONE);

        requestForYouText.setText("Getting your ambulance");

        getClosestDrive(for_who);


    }

    private void sendTokenToDatabase(String newToken) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);

        Map userInfo = new HashMap();
        userInfo.put("token", newToken);

        tokenRef.updateChildren(userInfo);
    }


    private DatabaseReference currentRideRef;
    private ValueEventListener currentRideRefListener;
    private List<String> rejectedDrivers;
    private void getCurrentRideInformation() {
        currentRideRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest").child("for_me");
        currentRideRefListener = currentRideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("assignedDriverId") != null){
                        driverFoundId = map.get("assignedDriverId").toString();
                        if (map.get("rideStatus") != null){
                            if (map.get("rideStatus").toString().equalsIgnoreCase("Pending")){
                                mRideStatus.setVisibility(View.VISIBLE);

                                requestForYouText.setText("Getting your ambulance");
                                destination_pin.setVisibility(View.GONE);
                                pickup_pin.setVisibility(View.GONE);
                                myLocation.setVisibility(View.GONE);
                                select_ambulance_type.setVisibility(View.GONE);
                                getRequestedPickupLocation();
                            }else if (map.get("rideStatus").toString().equalsIgnoreCase("Confirmed")){
                                mRideStatus.setVisibility(View.GONE);
                                if (mapRipple != null){
                                    if (mapRipple.isAnimationRunning()){
                                        mapRipple.stopRippleMapAnimation();
                                    }
                                }
                                Intent newIntent = new Intent(BookAmbulanceActivity.this, RideStartedActivity.class);
                                startActivity(newIntent);
                                finish();
                            }
                        }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    int marker_height = 100;
    int marker_width = 100;
    private DatabaseReference requestedPickupLocationRef;
    private ValueEventListener requestedPickupLocationRefListener;
    private MapRipple mapRipple;
    Marker pickupMarker;
    private void getRequestedPickupLocation() {
        requestedPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(userId).child("l");
        requestedPickupLocationRefListener = requestedPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !userId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickup_location_latlng = new LatLng(locationLat, locationLng);


                    BitmapDrawable greenPinBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_green_pin_foreground);
                    Bitmap gb = greenPinBitmap.getBitmap();
                    Bitmap smallGreenPinMarker = Bitmap.createScaledBitmap(gb, marker_width, marker_height, false);

                    if (pickupMarker == null){
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickup_location_latlng).title("pickup location").icon(BitmapDescriptorFactory.fromBitmap(smallGreenPinMarker)));
                    }
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(pickup_location_latlng)      // Sets the center of the map to Mountain View
                            .zoom(DEFAULT_ZOOM)              // Sets the orientation of the camera to easy // Sets the tilt of the camera to 30 degrees
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);

                    //Animation
                    startRippleInMap(pickup_location_latlng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startRippleInMap(LatLng pickup_location_latlng) {
        mapRipple = new MapRipple(mMap, pickup_location_latlng, BookAmbulanceActivity.this);
        mapRipple.withNumberOfRipples(1);
        mapRipple.withDistance(500);
        mapRipple.withRippleDuration(1000);
        mapRipple.withStrokeColor(R.color.themeBlue);
        mapRipple.withTransparency(0.5f);

        mapRipple.startRippleMapAnimation();
    }


    private void getDestinationLocation() {
        destination_location.setCardBackgroundColor(getResources().getColor(R.color.white));
        pickup_location.setCardBackgroundColor(getResources().getColor(R.color.grey));

        if (destinationLocationSessionManager.isDestinationLocationLoggedIn()){
            HashMap<String, String> location = destinationLocationSessionManager.getDestinationLocationDetails();
            String latitude = location.get(DestinationLocationSessionManager.KEY_LOCATION_LATITUDE);
            String longitude = location.get(DestinationLocationSessionManager.KEY_LOCATION_LONGITUDE);
            destination_location_latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
            if (!requestBool && (rideStatus != 2 || rideStatusForOther != 3)){
                destination_pin.setVisibility(View.VISIBLE);
            }
        }else {
            destination_location_latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }

        if (!requestBool && (rideStatus != 2 || rideStatusForOther != 3)){
            pickup_pin.setVisibility(View.GONE);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(destination_location_latlng)      // Sets the center of the map to Mountain View
                    .zoom(DEFAULT_ZOOM)               // Sets the orientation of the camera to east
                    /*.tilt(45)  */                 // Sets the tilt of the camera to 30 degrees
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);

        }

        if (!requestBool && (rideStatus != 2 || rideStatusForOther != 3)){
            changeDestinationLocation();
        }

        showDestinationMarker = true;
        showPickupMarker = false;
    }

    private void changeDestinationLocation() {
        if (destinationLocationSessionManager.isDestinationLocationLoggedIn()){
            destinationNotFilled.setVisibility(View.GONE);
            destinationFilled.setVisibility(View.VISIBLE);
            enter_hospital_location.setVisibility(View.GONE);
            drop_at.setVisibility(View.VISIBLE);
            geocoder = new Geocoder(this, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(destination_location_latlng.latitude, destination_location_latlng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                destination_address = addresses.get(0).getAddressLine(0);
                destinationLocationName.setText(destination_address);
                destinationLocation = destination_location_latlng;
            } catch (IOException e) {
                e.printStackTrace();
            }
            destinationLocationName.setVisibility(View.VISIBLE);
        }else {
            destinationFilled.setVisibility(View.GONE);
            destinationNotFilled.setVisibility(View.VISIBLE);

            drop_at.setVisibility(View.GONE);
            destinationLocationName.setVisibility(View.GONE);
            enter_hospital_location.setVisibility(View.VISIBLE);
        }
    }

    private void getPickupLocation() {
        pickup_location.setCardBackgroundColor(getResources().getColor(R.color.white));
        destination_location.setCardBackgroundColor(getResources().getColor(R.color.grey));

        if (pickupLocationSessionManager.isPickupLocationLoggedIn()){
            HashMap<String, String> location = pickupLocationSessionManager.getPickupLocationDetails();
            String latitude = location.get(PickupLocationSessionManager.KEY_LOCATION_LATITUDE);
            String longitude = location.get(PickupLocationSessionManager.KEY_LOCATION_LONGITUDE);
            pickup_location_latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        }else {
            if (!pickupPinChange){
                pickup_location_latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            }

        }

        if (!requestBool && (rideStatus != 2 || rideStatusForOther != 3)){
            destination_pin.setVisibility(View.GONE);
            pickup_pin.setVisibility(View.VISIBLE);
        }

        if (!requestBool && (rideStatus != 2 || rideStatusForOther != 3)){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pickup_location_latlng)      // Sets the center of the map to Mountain View
                    .zoom(DEFAULT_ZOOM)               // Sets the orientation of the camera to east
                    /*.tilt(45) */                  // Sets the tilt of the camera to 30 degrees
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
        }


        if (!requestBool && (rideStatus != 2 || rideStatusForOther != 3)){
            changePickupLocation();
        }

        showPickupMarker = true;
        showDestinationMarker = false;
    }

    private void changePickupLocation() {
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(pickup_location_latlng.latitude, pickup_location_latlng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            pickup_address = addresses.get(0).getAddressLine(0);
            pickupLocationName.setText(pickup_address);
            pickupLocation = pickup_location_latlng;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUserInformation(){
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        mName = map.get("name").toString();
                        fullName.setText(mName);
                    }
                    if (map.get("profileImageUrl") != null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPhone = mAuth.getCurrentUser().getPhoneNumber();
        phoneNumber.setText(mPhone);
    }

    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundId;
    private DatabaseReference mDriverReference;
    GeoQuery geoQuery;
    private void getClosestDrive(final String for_who) {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBool){
                    mDriverReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mDriverReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0 && requestBool){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();

                                if (driverFound){
                                    return;
                                }

                                if (driverMap.get("ambulance_type").equals(requestAmbulanceType)){
                                    driverFoundId = dataSnapshot.getKey();

                                    if (!rejectedDrivers.contains(driverFoundId)){
                                        driverFound = true;
                                        DatabaseReference driveRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                                        HashMap driver_map = new HashMap();
                                        driver_map.put("customerRideId", userId);
                                        driver_map.put("destinationName", destination_address);
                                        driver_map.put("destinationLatitude", String.valueOf(destinationLocation.latitude));
                                        driver_map.put("destinationLongitude", String.valueOf(destinationLocation.longitude));
                                        driver_map.put("paymentMethod", paymentMethod);
                                        driver_map.put("paid", paid);
                                        driver_map.put("forWho", for_who);
                                        if (for_who.equalsIgnoreCase("For_Other")){
                                            driver_map.put("requestedMobNumOfOthers", requestMobNumForOthers);
                                        }
                                        driveRef.updateChildren(driver_map).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()){
                                                    DatabaseReference customerRef = null;
                                                    if (for_who.equalsIgnoreCase("For_Me")){
                                                        customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest").child("for_me");
                                                    }else if (for_who.equalsIgnoreCase("For_Other")){
                                                        customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest").child("for_other").child(requestMobNumForOthers);
                                                    }
                                                    HashMap customer_map = new HashMap();
                                                    customer_map.put("assignedDriverId", driverFoundId);
                                                    customer_map.put("destinationName", destination_address);
                                                    customer_map.put("destinationLatitude", String.valueOf(destinationLocation.latitude));
                                                    customer_map.put("destinationLongitude", String.valueOf(destinationLocation.longitude));
                                                    customer_map.put("rideStatus", "Pending");
                                                    customerRef.updateChildren(customer_map).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()){
                                                                sendRequestToDriver(driverFoundId);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });


                                    }


                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound){
                    if (radius <= 20){
                        radius++;
                        getClosestDrive(for_who);
                    }else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.full_layout), "No ambulance found", Snackbar.LENGTH_SHORT);
                        View sbView = snackbar.getView();
                        sbView.setBackgroundColor(Color.parseColor("#DC143C"));
                        snackbar.show();
                        cancelRideAutomatically();
                    }

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void cancelRideAutomatically() {
        radius = 1;
        requestBool = false;

        if (geoQuery != null){
            geoQuery.removeAllListeners();
        }

        erasePolylines();

        if (driverFoundId != null){
            DatabaseReference driveRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
            driveRef.removeValue();
            driverFoundId = null;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customerRequest").child(userId);
        reference.removeValue();

        DatabaseReference currentRequestRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest");
        currentRequestRef.removeValue();

        mRideStatus.setVisibility(View.GONE);

        destination_pin.setVisibility(View.VISIBLE);
        pickup_pin.setVisibility(View.VISIBLE);
        myLocation.setVisibility(View.VISIBLE);
        select_ambulance_type.setVisibility(View.VISIBLE);

        if (pickupMarker != null){
            pickupMarker.remove();
        }
        if (destinationMarker != null){
            destinationMarker.remove();
        }

        if (mapRipple != null){
            mapRipple.stopRippleMapAnimation();
        }

        rideStatus = 1;
        rideStatusForOther = 1;
        rejectedDrivers = new ArrayList<>();

        requestForYouText.setText("Request Ambulance");

    }


    private void sendRequestToDriver(String driverFoundId) {

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference().child("notifications");
        String requestId = notificationsRef.push().getKey();

        DatabaseReference driverNotificationRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("Notification");
        driverNotificationRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("from", userId);
        Location locationA = new Location("point A");
        locationA.setLatitude(pickup_location_latlng.latitude);
        locationA.setLongitude(pickup_location_latlng.longitude);

        Location locationB = new Location("point B");
        locationB.setLatitude(destination_location_latlng.latitude);
        locationB.setLongitude(destination_location_latlng.longitude);

        double distance = locationA.distanceTo(locationB);

        map.put("price", String.valueOf(distance));

        notificationsRef.child(requestId).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                mRideStatus.setVisibility(View.VISIBLE);


                BitmapDrawable greenPinBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_green_pin_foreground);
                Bitmap gb = greenPinBitmap.getBitmap();
                Bitmap smallGreenPinMarker = Bitmap.createScaledBitmap(gb, marker_width, marker_height, false);

                if (pickupMarker == null){
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickup_location_latlng).title("pickup location").icon(BitmapDescriptorFactory.fromBitmap(smallGreenPinMarker)));
                }
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(pickup_location_latlng)      // Sets the center of the map to Mountain View
                        .zoom(DEFAULT_ZOOM)              // Sets the orientation of the camera to easy // Sets the tilt of the camera to 30 degrees
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);


                startRippleInMap(pickup_location_latlng);

            }
        });

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (requestBool){
                super.onBackPressed();
            }else {
                if (rideStatus == 2){
                    confirm_booking.setVisibility(View.GONE);
                    select_ambulance_type.setVisibility(View.VISIBLE);
                    changeToInitialMapCamera();
                    requestForYouText.setText("Request Ambulance");
                    rideStatus = 1;
                }else if (rideStatus == 1){
                    if (rideStatusForOther == 3){
                        rideStatusForOther = 2;
                        confirm_booking.setVisibility(View.GONE);
                        select_ambulance_type.setVisibility(View.VISIBLE);
                        requestMobileNumberLayout.setVisibility(View.VISIBLE);
                        changeToInitialMapCamera();
                        requestForOthersText.setText("Request For Others");
                    }else if (rideStatusForOther == 2){
                        requestMobileNumberLayout.setVisibility(View.GONE);
                        request_ambulance_for_you.setVisibility(View.VISIBLE);
                        rideStatusForOther = 1;
                    }else if (rideStatusForOther == 1){
                        super.onBackPressed();
                    }
                }


            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.book_ambulance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logoutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_your_trip) {
            // Handle the camera action
            Intent intent = new Intent(BookAmbulanceActivity.this, RideHistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_trip_for_other) {

        } else if (id == R.id.nav_get_support) {

        } else if (id == R.id.nav_payment) {

        } else if (id == R.id.nav_promotion) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(BookAmbulanceActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void logoutDialog(){


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Log Out");
        builder.setMessage("Do you want to log out?");

        builder.setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LogOut();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        builder.show();
    }

    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        MobileNumberSessionManager sessionManager = new MobileNumberSessionManager(this);
        sessionManager.logoutUser();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else {
                checkLocationPermission();
            }
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
    }

    private Boolean firstLocation = false;
    private Boolean pickupPinChange = false;

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()){
                if (getApplicationContext() != null){

                    mLastLocation = location;

                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            if (!firstLocation){
                                LatLng currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(currentLocation)      // Sets the center of the map to Mountain View
                                        .zoom(DEFAULT_ZOOM)               // Sets the orientation of the camera to east
                                        /*.tilt(45) */                  // Sets the tilt of the camera to 30 degrees
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 100, new GoogleMap.CancelableCallback() {
                                    @Override
                                    public void onFinish() {
                                        pickup_pin.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                                mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                    @Override
                                    public void onCameraIdle() {
                                        if (!requestBool && rideStatus != 2){
                                            if (showPickupMarker){
                                                pickup_location_latlng = mMap.getCameraPosition().target;
                                                pickupLocationSessionManager.createPickupLocationSession(getLocationName(pickup_location_latlng), String.valueOf(pickup_location_latlng.latitude), String.valueOf(pickup_location_latlng.longitude));
                                                changePickupLocation();
                                                pickupPinChange = true;
                                            }else if (showDestinationMarker){
                                                destination_location_latlng = mMap.getCameraPosition().target;
                                                if (destinationLocationSessionManager.isDestinationLocationLoggedIn()){
                                                    destinationLocationSessionManager.createDestinationLocationSession(getLocationName(destination_location_latlng), String.valueOf(destination_location_latlng.latitude), String.valueOf(destination_location_latlng.longitude));
                                                }
                                                changeDestinationLocation();
                                            }
                                        }

                                        Log.e(TAG, "Final location: lat " + mMap.getCameraPosition().target.latitude + " lon " + mMap.getCameraPosition().target.longitude);
                                    }
                                });

                                firstLocation = true;
                            }

                            if (rideStatus == 1 && rideStatusForOther == 1){
                                if (showPickupMarker){
                                    getPickupLocation();
                                    if (destinationLocationSessionManager.isDestinationLocationLoggedIn()){
                                        HashMap<String, String> session_location = destinationLocationSessionManager.getDestinationLocationDetails();
                                        String latitude = session_location.get(DestinationLocationSessionManager.KEY_LOCATION_LATITUDE);
                                        String longitude = session_location.get(DestinationLocationSessionManager.KEY_LOCATION_LONGITUDE);
                                        destination_location_latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                                    }else {
                                        destination_location_latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                    }
                                    changeDestinationLocation();
                                    if (!requestBool && rideStatus != 2){
                                        myLocation.setVisibility(View.VISIBLE);
                                    }

                                }else if (showDestinationMarker){
                                    getDestinationLocation();
                                    if (pickupLocationSessionManager.isPickupLocationLoggedIn()){
                                        HashMap<String, String> session_location = pickupLocationSessionManager.getPickupLocationDetails();
                                        String latitude = session_location.get(PickupLocationSessionManager.KEY_LOCATION_LATITUDE);
                                        String longitude = session_location.get(PickupLocationSessionManager.KEY_LOCATION_LONGITUDE);
                                        pickup_location_latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                                    }else {
                                        if (!pickupPinChange){
                                            pickup_location_latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                        }

                                    }
                                    changePickupLocation();
                                    myLocation.setVisibility(View.GONE);
                                }
                            }

                        }
                    });



                }
            }
        }
    };

    String locationAddress;

    private String  getLocationName(LatLng locationLatLng) {
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(locationLatLng.latitude, locationLatLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            locationAddress = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locationAddress;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(BookAmbulanceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        }).create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(BookAmbulanceActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();
                }
        }
    }


    private BroadcastReceiver mReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.RIDECONFIRMED");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                String rideStatus = intent.getStringExtra("rideStatus");

                if (rideStatus.equals("Confirmed")) {
                    mRideStatus.setVisibility(View.GONE);
                    if (mapRipple.isAnimationRunning()){
                        mapRipple.stopRippleMapAnimation();
                    }
                    Intent newIntent = new Intent(BookAmbulanceActivity.this, RideStartedActivity.class);
                    if (for_who.equalsIgnoreCase("For_Other")){
                        intent.putExtra("forWho", "for_other");
                        intent.putExtra("requestedMobNumOfOthers", requestMobNumForOthers);
                    }else {
                        intent.putExtra("forWho", "for_me");
                    }
                    startActivity(newIntent);
                    finish();

                }if (rideStatus.equals("Declined")){
                    rejectedDrivers.add(driverFoundId);
                    driverFoundId = null;
                    driverFound = false;
                    getClosestDrive(for_who);
                }
            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (geoQuery != null){
            geoQuery.removeAllListeners();
        }


        unregisterReceiver(mReceiver);

    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    private void getRouteToMarker(LatLng pickupLatLng, LatLng destinationLatLng) {
        if (destinationLatLng != null && pickupLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .key("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                    .waypoints(pickupLatLng, destinationLatLng)
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
// The Routing request failed
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            String destinationTime = "Time To Reach Destination : " + route.get(i).getDurationText();
            mDestinationTimeText.setText(destinationTime);
            mEstimatedPriceText.setText(route.get(i).getDistanceText());
            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolylines(){
        if (polylines != null){
            for (Polyline line : polylines){
                line.remove();
            }
            polylines.clear();
        }

    }
}
