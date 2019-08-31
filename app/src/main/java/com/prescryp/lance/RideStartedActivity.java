package com.prescryp.lance;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxrelay2.PublishRelay;
import com.prescryp.lance.Misc.RunTimePermission;
import com.prescryp.lance.Misc.WorkaroundMapFragment;
import com.prescryp.lance.Session.DestinationLocationSessionManager;
import com.prescryp.lance.Session.PickupLocationSessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RideStartedActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private String userId;
    private Location mLastLocation;
    private static String TAG = "BookAmbulanceActivity";

    private static final float DEFAULT_ZOOM = 16f;

    private LatLng pickup_location_latlng, destination_location_latlng, driver_last_latlng;
    private Boolean requestBool = false;
    private Marker pickupMarker, destinationMarker;
    int height = 100;
    int width = 100;

    PickupLocationSessionManager pickupLocationSessionManager;
    DestinationLocationSessionManager destinationLocationSessionManager;

    private CircleImageView mDriverProfileImage;
    private TextView mDriverName, mAmbulanceType, mAmbulanceNumber;

    private CardView mCancelRide;

    private FirebaseAuth mAuth;
    private TextView mPickupLocationName;
    private CardView mMyLocation;
    private ImageView mDistanceImg, mEtaImg;
    private TextView mDropLocationName;

    private ScrollView mScrollView;

    private FusedLocationProviderClient mFusedLocationClient;

    private PublishRelay<LatLng> latLngPublishRelay = PublishRelay.create();
    private Disposable latLngDisposable;
    private float v;
    private int emission = 0;

    private String driverFoundId;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    private LocationRequest mLocationRequest;

    private TextView mDistanceLocation, mEtaLocation;

    private Toolbar toolbar;
    private ImageView mCallDriver, mCallHelp;
    private String driverPhoneNumber, helpPhoneNumber = "+917679009722";
    private RunTimePermission photoRunTimePermission;
    private String forWho, requestedMobNumOfOthers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_started);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mScrollView = findViewById(R.id.scrollView);

        mPickupLocationName = findViewById(R.id.pickupLocationName);
        mDropLocationName = findViewById(R.id.dropLocationName);

        mDriverProfileImage = findViewById(R.id.driverProfileImage);
        mDriverName = findViewById(R.id.driverName);
        mAmbulanceType = findViewById(R.id.ambulanceType);
        mAmbulanceNumber = findViewById(R.id.ambulanceNumber);

        mCancelRide = findViewById(R.id.cancelRide);

        mMyLocation = findViewById(R.id.myLocation);

        mCallDriver = findViewById(R.id.call_driver);
        mCallHelp = findViewById(R.id.call_help);

        polylines = new ArrayList<>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDistanceImg = findViewById(R.id.distance_img);
        mEtaImg = findViewById(R.id.eta_img);

        mDistanceLocation = findViewById(R.id.distanceLocation);
        mEtaLocation = findViewById(R.id.etaLocation);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        pickupLocationSessionManager = new PickupLocationSessionManager(getApplicationContext());
        destinationLocationSessionManager = new DestinationLocationSessionManager(getApplicationContext());

        SupportMapFragment mapFragment = (WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        if (getIntent() != null){
            forWho = getIntent().getStringExtra("forWho");
            requestedMobNumOfOthers = getIntent().getStringExtra("requestedMobNumOfOthers");
        }

        rideStatus = "On the way";

        getRequestedPickupLocation();



        getRideStatus();

        mMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(driver_last_latlng);
                builder.include(pickup_location_latlng);
                builder.include(destination_location_latlng);
                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width*0.43);

                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(mCameraUpdate, 1000, null);
            }
        });

        mCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endRide();
            }
        });

        mCallDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + driverPhoneNumber));
                if (ActivityCompat.checkSelfPermission(RideStartedActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    photoRunTimePermission = new RunTimePermission(RideStartedActivity.this);
                    photoRunTimePermission.requestPermission(new String[]{
                            Manifest.permission.CALL_PHONE
                    }, new RunTimePermission.RunTimePermissionListener() {

                        @Override
                        public void permissionGranted() {
                            if (ActivityCompat.checkSelfPermission(RideStartedActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                startActivity(intent);
                            }

                        }

                        @Override
                        public void permissionDenied() {
                        }
                    });
                }
            }
        });

        mCallHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_CALL);

                intent.setData(Uri.parse("tel:" + helpPhoneNumber));
                if (ActivityCompat.checkSelfPermission(RideStartedActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    photoRunTimePermission = new RunTimePermission(RideStartedActivity.this);
                    photoRunTimePermission.requestPermission(new String[]{
                            Manifest.permission.CALL_PHONE
                    }, new RunTimePermission.RunTimePermissionListener() {

                        @Override
                        public void permissionGranted() {
                            if (ActivityCompat.checkSelfPermission(RideStartedActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                startActivity(intent);
                            }

                        }

                        @Override
                        public void permissionDenied() {
                        }
                    });
                }
            }
        });
    }



    private DatabaseReference rideStatusRef;
    private ValueEventListener rideStatusRefListener;
    private Boolean pickedUpCustomer = false;
    private String rideStatus;
    private void getRideStatus() {
        if (forWho.equalsIgnoreCase("for_other")){
            rideStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest").child("for_other").child(requestedMobNumOfOthers).child("rideStatus");
        }else if (forWho.equalsIgnoreCase("for_me")){
            rideStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest").child("rideStatus");
        }
        rideStatusRefListener = rideStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !userId.equals("")){
                    if (dataSnapshot.getValue() != null){
                        rideStatus = dataSnapshot.getValue().toString();
                        if (rideStatus.equals("Picked Up")){
                            mDistanceImg.setImageResource(R.drawable.red_distance);
                            mEtaImg.setImageResource(R.drawable.red_eta);

                            firstRouting = true;
                            pickedUpCustomer = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference requestedPickupLocationRef, currentRideRef;
    private ValueEventListener requestedPickupLocationRefListener, currentRideRefListener;
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

                    mPickupLocationName.setText(getLocationName(pickup_location_latlng));

                    BitmapDrawable greenPinBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_green_pin_foreground);
                    Bitmap gb = greenPinBitmap.getBitmap();
                    Bitmap smallGreenPinMarker = Bitmap.createScaledBitmap(gb, width, height, false);

                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickup_location_latlng).title("pickup location").icon(BitmapDescriptorFactory.fromBitmap(smallGreenPinMarker)));
                    /*CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(pickup_location_latlng)      // Sets the center of the map to Mountain View
                            .zoom(DEFAULT_ZOOM)              // Sets the orientation of the camera to east
                            *//*.tilt(45) *//*                  // Sets the tilt of the camera to 30 degrees
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
*/
                    getCurrentRideInformation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    Geocoder geocoder;
    List<Address> addresses;
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
    private void getCurrentRideInformation() {
        currentRideRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("currentRequest");
        currentRideRefListener = currentRideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("assignedDriverId") != null){
                        driverFoundId = map.get("assignedDriverId").toString();
                        requestBool = true;
                        getDriverLocation();
                        getDriverInfo();/*
                        getHasRideEnded();*/
                    }
                    if (map.get("destinationLatitude") != null && map.get("destinationLongitude") != null){

                        destination_location_latlng = new LatLng(Double.valueOf(map.get("destinationLatitude").toString()), Double.valueOf(map.get("destinationLongitude").toString()));

                        mDropLocationName.setText(getLocationName(destination_location_latlng));
                        BitmapDrawable redPinBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_pin_foreground);
                        Bitmap rb = redPinBitmap.getBitmap();
                        Bitmap smallRedPinMarker = Bitmap.createScaledBitmap(rb, width, height, false);

                        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination_location_latlng).title("Drop Here").icon(BitmapDescriptorFactory.fromBitmap(smallRedPinMarker)));


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Marker mDriverMarker;
    private float driverDistanceTraveled = 0;
    private LatLng driverLatLng = new LatLng(0, 0);
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private Boolean firstRouting = true;
    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("diverWorking").child(driverFoundId).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBool){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    if (driverLatLng != new LatLng(0, 0)){
                        Location prevDriver = new Location("New Driver Location");
                        prevDriver.setLatitude(locationLat);
                        prevDriver.setLongitude(locationLng);

                        Location newDriver = new Location("Previous Driver Location");
                        newDriver.setLatitude(driverLatLng.latitude);
                        newDriver.setLongitude(driverLatLng.longitude);

                        driverDistanceTraveled += prevDriver.distanceTo(newDriver);
                    }

                    driverLatLng = new LatLng(locationLat, locationLng);


                    latLngPublishRelay.accept(driverLatLng);

                    if (driverDistanceTraveled > 100 || firstRouting){
                        if (!pickedUpCustomer){
                            getRouteToMarker(pickup_location_latlng, driverLatLng);
                        }else {
                            getRouteToMarker(destination_location_latlng, driverLatLng);
                        }
                        if (!firstRouting){
                            driverDistanceTraveled = 0;
                        }
                        firstRouting = false;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfo(){
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        mDriverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null){
                        driverPhoneNumber = map.get("phone").toString();
                    }
                    if (map.get("ambulance_type") != null){
                        mAmbulanceType.setText(map.get("ambulance_type").toString());
                    }
                    if (map.get("ambulance_number") != null){
                        mAmbulanceNumber.setText(map.get("ambulance_number").toString());
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

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
   /* private void getHasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
*/
    private void endRide() {
        requestBool = false;

        if (driverLocationRefListener != null){
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }
        if (driveHasEndedRefListener != null){
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        }

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



        if (destinationMarker != null){
            destinationMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }

        if (pickupMarker != null){
            pickupMarker.remove();
        }

        if (requestedPickupLocationRefListener != null){
            requestedPickupLocationRef.removeEventListener(requestedPickupLocationRefListener);
        }
        if (currentRideRefListener != null){
            currentRideRef.removeEventListener(currentRideRefListener);
        }
        if (rideStatusRefListener != null){
            rideStatusRef.removeEventListener(rideStatusRefListener);
        }


        erasePolylines();

        mDriverName.setText("");
        mAmbulanceType.setText("");
        mAmbulanceNumber.setText("");
        mDriverProfileImage.setImageResource(R.drawable.user_black);

        if (pickupLocationSessionManager.isPickupLocationLoggedIn()){
            pickupLocationSessionManager.logoutPickupLocation();
        }
        if (destinationLocationSessionManager.isDestinationLocationLoggedIn()){
            destinationLocationSessionManager.logoutDestinationLocation();
        }

        Intent intent = new Intent(RideStartedActivity.this, BookAmbulanceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }


    private void getRouteToMarker(LatLng pickupLatLng, LatLng driverLatLng) {
        if (driverLatLng != null && pickupLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .key("AIzaSyA6WTOATbfP_G36XKBzRyYVRM0D9PHNGb0")
                    .waypoints(pickupLatLng, driverLatLng)
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

            mDistanceLocation.setText(route.get(i).getDistanceText());
            mEtaLocation.setText(route.get(i).getDurationText());
            String rideTitle;
            if (route.get(i).getDistanceValue() < 100){
                toolbar.setTitle("Your ambulance is here");
            }else {
                if (rideStatus.equals("Picked Up")){
                    rideTitle = "You are " + route.get(i).getDurationText() + " away from destination";
                }else {
                    rideTitle = "The ambulance is " + route.get(i).getDurationText() + " away from you";
                }
                toolbar.setSubtitle(rideTitle);
            }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mScrollView = findViewById(R.id.scrollView); //parent scrollview in xml, give your scrollview id value
        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch()
                    {
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });


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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()){
                if (getApplicationContext() != null){

                    mLastLocation = location;


                }
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(RideStartedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        }).create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(RideStartedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
        latLngDisposable = latLngPublishRelay
                .buffer(2)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<LatLng>>() {

                    @Override
                    public void accept(List<LatLng> latLngs) throws Exception {
                        emission++;
                        animateCarOnMap(latLngs);
                    }
                });

        IntentFilter intentFilter = new IntentFilter("android.intent.action.RIDERATING");
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                String rideId = intent.getStringExtra("rideId");

                Intent newIntent = new Intent(RideStartedActivity.this, RideRatingActivity.class);
                newIntent.putExtra("startedFrom", "RideStartedActivity");
                newIntent.putExtra("rideId", rideId);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newIntent);
                finish();

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!latLngDisposable.isDisposed()) {
            latLngDisposable.dispose();
        }


        unregisterReceiver(mReceiver);
    }


    int car_width = 92;
    int car_height = 92;
    private void animateCarOnMap(final List<LatLng> latLngs) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            builder.include(latLng);
        }
        builder.include(pickup_location_latlng);
        builder.include(destination_location_latlng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.43);

        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(mCameraUpdate);
        BitmapDrawable carBitmap = (BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher_car_foreground);
        Bitmap b = carBitmap.getBitmap();
        Bitmap smallCarMarker = Bitmap.createScaledBitmap(b, car_width, car_height, false);
        if (emission == 1) {
            mDriverMarker = mMap.addMarker(new MarkerOptions().position(latLngs.get(0)).flat(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(smallCarMarker)));
        }
        mDriverMarker.setPosition(latLngs.get(0));
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v = valueAnimator.getAnimatedFraction();
                double lng = v * latLngs.get(1).longitude + (1 - v)
                        * latLngs.get(0).longitude;
                double lat = v * latLngs.get(1).latitude + (1 - v)
                        * latLngs.get(0).latitude;
                LatLng newPos = new LatLng(lat, lng);
                mDriverMarker.setPosition(newPos);
                mDriverMarker.setAnchor(0.5f, 0.5f);
                mDriverMarker.setRotation(getBearing(latLngs.get(0), newPos));
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(newPos);
                builder.include(pickup_location_latlng);
                builder.include(destination_location_latlng);
                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width*0.43);

                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(mCameraUpdate);

                driver_last_latlng = newPos;
            }
        });
        valueAnimator.start();
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

}
