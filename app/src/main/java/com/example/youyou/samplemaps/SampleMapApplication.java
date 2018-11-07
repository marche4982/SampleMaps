package com.example.youyou.samplemaps;

import android.app.Application;
import android.location.Location;

/**
 * Created by youyou on 2018/08/30.
 */

public class SampleMapApplication extends Application {
    private Location mPresentLocation;

    public Location getPresentLocation(){
        return this.mPresentLocation;
    }
    public void setPresentLocation(Location location){
        this.mPresentLocation = location;
    }


}
