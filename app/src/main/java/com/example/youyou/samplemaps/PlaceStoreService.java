package com.example.youyou.samplemaps;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.places.Place;

import java.util.List;

/**
 * Created by youyou on 2018/08/28.
 */

public class PlaceStoreService extends IntentService{

    private static final String TAG = "PlaceStoreService";

    public PlaceStoreService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if(LocationAvailability.hasLocationAvailability(intent) ){
            LocationAvailability availability = LocationAvailability.extractLocationAvailability(intent);

            if( !availability.isLocationAvailable() ){
                return;
            }
        }

        if(LocationResult.hasResult(intent)){
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            ((SampleMapApplication)getApplication()).setPresentLocation(location);
            sendMessage(getResources().getInteger(R.integer.message_get_location));
        }
    }

    protected void sendMessage(int message){
        Intent broadcast = new Intent();
        broadcast.putExtra("message", message);
        broadcast.setAction("GET_LOCATION");
        getBaseContext().sendBroadcast(broadcast);
    }

}
