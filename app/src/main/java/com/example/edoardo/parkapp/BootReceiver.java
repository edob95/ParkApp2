package com.example.edoardo.parkapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent service = new Intent(context, ParkAppService.class);
        context.startService(service);
        Toast.makeText(context, "REBOOTED", Toast.LENGTH_SHORT).show();

    }


}
