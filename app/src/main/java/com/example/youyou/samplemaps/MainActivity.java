package com.example.youyou.samplemaps;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.nearby.messages.internal.Update;

public class MainActivity extends AppCompatActivity {

    private GoogleMap mMap;

    MapFragment mf;
    private Location mPresentLocation;
    private TextView mTextView;

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SuppprtMapFragment


        mTextView = (TextView)findViewById(R.id.textview_main_from);
        GetPresentLocation getPresentLocation = new GetPresentLocation(this); // 現在地を取得する

        UpdateReceiver updateReceiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("GET_LOCATION");
        registerReceiver(updateReceiver, filter);
    }

    private void setPresentLocation(Location location){
        this.mPresentLocation = location;
    }

    private void getRouteToStation(){
        // 駅までの道を探す

    }

    public class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            Bundle extras = intent.getExtras();
            int message = extras.getInt("message");

            if( message == getResources().getInteger(R.integer.message_get_location) ){
                Location tLocation = (((SampleMapApplication)getApplication()).getPresentLocation());
                setPresentLocation(tLocation);

                // ここから map 探索につなげる
                getRouteToStation();
            }
        }
    }
}