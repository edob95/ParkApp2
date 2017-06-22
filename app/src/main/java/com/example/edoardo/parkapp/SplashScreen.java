package com.example.edoardo.parkapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import static com.example.edoardo.parkapp.R.id.start;

/**
 * Created by Niko on 22/06/2017.
 */
public class SplashScreen extends AppCompatActivity
{
    int timeout = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, timeout);
    }
}