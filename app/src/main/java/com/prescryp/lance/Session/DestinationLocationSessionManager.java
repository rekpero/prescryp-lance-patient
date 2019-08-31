package com.prescryp.lance.Session;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class DestinationLocationSessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREFER_NAME = "AndroidDestinationLocationPref";
    private static final String IS_DESTINATION_LOGIN = "IsDestinationLocationLoggedIn";
    public static final String KEY_LOCATION_NAME = "LocationName";
    public static final String KEY_LOCATION_LATITUDE = "LocationLatitude";
    public static final String KEY_LOCATION_LONGITUDE = "LocationLongitude";

    public DestinationLocationSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createDestinationLocationSession(String locationName, String locationLat, String locationLng){
        editor.putBoolean(IS_DESTINATION_LOGIN, true);
        editor.putString(KEY_LOCATION_NAME, locationName);
        editor.putString(KEY_LOCATION_LATITUDE, locationLat);
        editor.putString(KEY_LOCATION_LONGITUDE, locationLng);
        editor.commit();
    }


    public HashMap<String, String> getDestinationLocationDetails(){
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_LOCATION_NAME, pref.getString(KEY_LOCATION_NAME, null));
        user.put(KEY_LOCATION_LATITUDE, pref.getString(KEY_LOCATION_LATITUDE, null));
        user.put(KEY_LOCATION_LONGITUDE, pref.getString(KEY_LOCATION_LONGITUDE, null));

        return user;
    }

    public void logoutDestinationLocation(){
        editor.clear();
        editor.commit();

    }

    public boolean isDestinationLocationLoggedIn(){
        return pref.getBoolean(IS_DESTINATION_LOGIN, false);
    }


}
