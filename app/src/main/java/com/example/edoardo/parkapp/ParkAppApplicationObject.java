package com.example.edoardo.parkapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;


public class ParkAppApplicationObject extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Park App", "App started");

        // start service
        /*Intent service = new Intent(this, ParkAppService.class);
        startService(service);*/
    }
}
