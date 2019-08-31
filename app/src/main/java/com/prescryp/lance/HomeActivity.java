package com.prescryp.lance;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.prescryp.lance.Misc.RunTimePermission;
import com.prescryp.lance.Session.DestinationLocationSessionManager;
import com.prescryp.lance.Session.PickupLocationSessionManager;

public class HomeActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    RunTimePermission photoRunTimePermission;

    PickupLocationSessionManager pickupLocationSessionManager;
    DestinationLocationSessionManager destinationLocationSessionManager;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (photoRunTimePermission != null) {
            photoRunTimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        pickupLocationSessionManager = new PickupLocationSessionManager(getApplicationContext());
        destinationLocationSessionManager = new DestinationLocationSessionManager(getApplicationContext());


        photoRunTimePermission = new RunTimePermission(this);
        photoRunTimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE
        }, new RunTimePermission.RunTimePermissionListener() {

            @Override
            public void permissionGranted() {
                new Handler().postDelayed(new Runnable() {

                    /*
                     * Showing splash screen with a timer. This will be useful when you
                     * want to show case your app logo / company
                     */

                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        // Start your app main activity
                        if (pickupLocationSessionManager.isPickupLocationLoggedIn()){
                            pickupLocationSessionManager.logoutPickupLocation();
                        }if (destinationLocationSessionManager.isDestinationLocationLoggedIn()){
                            destinationLocationSessionManager.logoutDestinationLocation();
                        }
                        Intent i = new Intent(HomeActivity.this, SignInActivity.class);
                        startActivity(i);

                        // close this activity
                        finish();
                    }
                }, SPLASH_TIME_OUT);
            }

            @Override
            public void permissionDenied() {
            }
        });



    }
}
