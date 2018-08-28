package com.example.youyou.samplemaps;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentContainer;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by youyou on 2018/08/26.
 */

public class GetPresentLocation {

    private static final int REQUEST_INSTALL_OR_UPDATE = 1;
    private static final int REQUEST_RESOLVE_PROBLEMS = 2;
    private static final int REQUEST_CHANGE_SETTINGS = 3;
    private static final int REQUEST_PERMISSION = 4;

    private static final int PENDING_INTENT_LOCATION = 1;

    private static final long MINUTE = 60 * 1000;
    private static final long INTERVAL_MS = 10 * MINUTE;
    private static final long FASTEST_INTERVAL_MS = 20;
    private static final long MAX_WAIT_TIME_MS = 60 * MINUTE;

    private Activity mActivity; // コンストラクタでとってくる
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    // PlaceStoreService で位置情報の取得できる　：やった
    // TODO :取得した現在地をマップに表示させる
    // →　それを使って、現在地と最寄り駅を調べる
    // →　最寄り駅のルートを探る


    // これをつかって、現在位置の取得、最寄り駅のサーチ、そこまでのルート検索をする

    // Google Play サービスが利用可能か調べる
    private void checkServicesAvaliable(){
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ){
            return ;
        }

        GoogleApiAvailability checker = GoogleApiAvailability.getInstance();
        int result = checker.isGooglePlayServicesAvailable(mActivity);
        if( result == ConnectionResult.SUCCESS ){
            onGoogleServicesAvailable();
        } else {
            checker.showErrorDialogFragment(mActivity, result, REQUEST_INSTALL_OR_UPDATE);
        }

    }

    // Google Play Service が利用可能だった場合
    private void onGoogleServicesAvailable(){
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mGoogleApiCallback)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mGoogleApiClient.connect();
    }

    private GoogleApiClient.ConnectionCallbacks mGoogleApiCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            // 位置情報取得できるか確認
            checkUserLocationAvailability();
        }

        @Override
        public void onConnectionSuspended(int i) {
            // 接続が停止された
        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if( connectionResult.hasResolution() ){
                // 何らかの方法で解決可能な場合
                try{
                    connectionResult.startResolutionForResult(mActivity, REQUEST_RESOLVE_PROBLEMS);
                }
                catch (IntentSender.SendIntentException e ){
                    e.printStackTrace();
                }
            }

            handleError();
        }
    };

    private void checkUserLocationAvailability(){
        // 位置情報取得用のオブジェクトをつくる
        mLocationRequest = new LocationRequest()
                .setInterval(INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS)
                .setMaxWaitTime(MAX_WAIT_TIME_MS)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        // この位置情報は可能かのリクエスト
        LocationSettingsRequest checkRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .build();

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, checkRequest);

        result.setResultCallback(mResultCallback);

    }

    private ResultCallback<LocationSettingsResult> mResultCallback = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
            final Status status = locationSettingsResult.getStatus();

            switch(status.getStatusCode()){
                case LocationSettingsStatusCodes.SUCCESS:
                    onUserLocationAvailable();
                    break;

                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try{
                        status.startResolutionForResult(mActivity, REQUEST_CHANGE_SETTINGS);
                    }
                    catch(IntentSender.SendIntentException e){
                        e.printStackTrace();
                        handleError();
                    }
                    break;
                default:
                    // その他
                    handleError();
            }

        }
    };

    private void onUserLocationAvailable(){

        if( mGoogleApiClient.isConnected() ){
            // マニフェストのパーミッション取得
            if( ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                {
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_PERMISSION);
                }
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient,
                    mLocationRequest,
                    getLocationPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT));

        }
    }

    private PendingIntent getLocationPendingIntent(int flag){
        Intent intent = new Intent(mActivity, GetPresentLocation.class);
        return PendingIntent.getService(mActivity, PENDING_INTENT_LOCATION, intent, flag);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if( requestCode == REQUEST_CHANGE_SETTINGS){
            boolean isSomethingGranted = false;
            for(int grantResult : grantResults){
                if( grantResult == PackageManager.PERMISSION_GRANTED ){
                    isSomethingGranted = true;
                    break;
                }
            }
            if( isSomethingGranted ){
                onUserLocationAvailable();
            }
            else{
                handleError();
            }
        }
    }

    // 位置情報を取れないときの処理
    private void handleError(){
    }



}
