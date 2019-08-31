package com.prescryp.lance;

import android.app.Activity;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.prescryp.lance.Adapters.PlaceAutocompleteAdapter;
import com.prescryp.lance.Session.DestinationLocationSessionManager;
import com.prescryp.lance.Session.PickupLocationSessionManager;

import java.util.ArrayList;
import java.util.Locale;

public class SearchLocationActivity extends AppCompatActivity implements PlaceAutocompleteAdapter.PlaceAutoCompleteInterface, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{


    GoogleApiClient mGoogleApiClient;
    private EditText auto_complete_location;
    private RecyclerView list_search;
    private PlaceAutocompleteAdapter mAdapter;

    PickupLocationSessionManager pickupLocationSessionManager;
    DestinationLocationSessionManager destinationLocationSessionManager;

    private LatLng mLastLocation;
    private LatLngBounds bounds;

    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        pickupLocationSessionManager = new PickupLocationSessionManager(getApplicationContext());
        destinationLocationSessionManager = new DestinationLocationSessionManager(getApplicationContext());

        if (getIntent() != null){
            if (getIntent().getStringExtra("LOCATION_FOR").equalsIgnoreCase("PICKUP")){
                toolbar.setTitle("Enter pickup location");
            }else if (getIntent().getStringExtra("LOCATION_FOR").equalsIgnoreCase("DESTINATION")){
                toolbar.setTitle("Enter drop location");
            }

            if (!TextUtils.isEmpty(getIntent().getStringExtra("currentLatitude")) && !TextUtils.isEmpty(getIntent().getStringExtra("currentLongitude"))){
                mLastLocation = new LatLng(Double.valueOf(getIntent().getStringExtra("currentLatitude")), Double.valueOf(getIntent().getStringExtra("currentLongitude")));
            }
        }


        if (mLastLocation != null){
            LatLng center = new LatLng(22.5726, 88.3639);

            LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
            LatLng westSide = SphericalUtil.computeOffset(center, 100000, 90);
            LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);
            LatLng eastSide = SphericalUtil.computeOffset(center, 100000, 270);

            bounds = LatLngBounds.builder().include(northSide).include(southSide).include(westSide).include(eastSide).build();
        }


        auto_complete_location = findViewById(R.id.auto_complete_location);
        list_search = findViewById(R.id.list_search);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();


        mAdapter = new PlaceAutocompleteAdapter(SearchLocationActivity.this, R.layout.view_placesearch,
                mGoogleApiClient, bounds, null);
        list_search.setAdapter(mAdapter);
        list_search.setHasFixedSize(true);
        list_search.setLayoutManager(new LinearLayoutManager(SearchLocationActivity.this));

        auto_complete_location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    //mClear.setVisibility(View.VISIBLE);
                    if (mAdapter != null) {
                        list_search.setAdapter(mAdapter);
                        if (list_search.getVisibility() == View.GONE)
                            list_search.setVisibility(View.VISIBLE);
                    }
                } else {
                    //mClear.setVisibility(View.GONE);
                    list_search.setVisibility(View.GONE);
                }
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Toast.makeText(getApplicationContext(), "API_NOT_CONNECTED", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteAdapter.PlaceAutocomplete> mResultList, int position) {
        if (mResultList != null) {
            try {
                final String placeId = String.valueOf(mResultList.get(position).placeId);
                        /*
                             Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (places.getCount() == 1) {
                            //Do the things here on Click.....
                            Intent intent = new Intent(SearchLocationActivity.this, BookAmbulanceActivity.class);
                            if (getIntent() != null){
                                if (getIntent().getStringExtra("LOCATION_FOR").equalsIgnoreCase("PICKUP")){
                                    /*intent.putExtra("PICKUP_LATITUDE", places.get(0).getLatLng().latitude);
                                    intent.putExtra("PICKUP_LONGITUDE", places.get(0).getLatLng().longitude);
                                    intent.putExtra("DESTINATION_LATITUDE", getIntent().getStringExtra("DESTINATION_LATITUDE"));
                                    intent.putExtra("DESTINATION_LONGITUDE", getIntent().getStringExtra("DESTINATION_LONGITUDE"));*/
                                    pickupLocationSessionManager.createPickupLocationSession(places.get(0).getName().toString(), String.valueOf(places.get(0).getLatLng().latitude),
                                            String.valueOf(places.get(0).getLatLng().longitude));
                                }else if (getIntent().getStringExtra("LOCATION_FOR").equalsIgnoreCase("DESTINATION")){
                                    /*intent.putExtra("PICKUP_LATITUDE", getIntent().getStringExtra("PICKUP_LATITUDE"));
                                    intent.putExtra("PICKUP_LONGITUDE", getIntent().getStringExtra("PICKUP_LONGITUDE"));
                                    intent.putExtra("DESTINATION_LATITUDE", places.get(0).getLatLng().latitude);
                                    intent.putExtra("DESTINATION_LONGITUDE", places.get(0).getLatLng().longitude);*/
                                    destinationLocationSessionManager.createDestinationLocationSession(places.get(0).getName().toString(), String.valueOf(places.get(0).getLatLng().latitude),
                                            String.valueOf(places.get(0).getLatLng().longitude));
                                }
                                intent.putExtra("LOCATION_FOR", getIntent().getStringExtra("LOCATION_FOR"));
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                            //Toast.makeText(getApplicationContext(), "Clickkkkkkkkkk", Toast.LENGTH_SHORT).show();
                            hideSoftInput(SearchLocationActivity.this);
                            finish();

                            //finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception ignored) {

            }

        }
    }

    public static void hideSoftInput(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
