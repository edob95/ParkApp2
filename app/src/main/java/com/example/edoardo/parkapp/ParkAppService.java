package com.example.edoardo.parkapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class ParkAppService extends Service{

    public final static int INTERVAL = 15000;
    public final static long NOTIFICATION_TIME = 1800000;
    public final static long RANGE_DELAY = 7499;
    private ParkAppApplicationObject app;
    private Timer timer;
    private SharedPreferences sharedPreferences;
    private long duration;
    private long elapsedTime;


    @Override
    public void onCreate() {
        Log.d("Park App", "Service created");
        app = (ParkAppApplicationObject) getApplication();
        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
        duration = 0;
        elapsedTime = 0;
        startTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Park App", "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Park App", "Service bound - not used!");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("Park App", "Service destroyed");
        stopTimer();
    }

    private void startTimer() {
        duration = sharedPreferences.getLong("park_duration", 0);
        final long startTime = sharedPreferences.getLong("begin_time_millis", -1);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                elapsedTime = System.currentTimeMillis() - startTime;
                long range1 = NOTIFICATION_TIME - RANGE_DELAY;
                long range2 = NOTIFICATION_TIME + RANGE_DELAY;
                long diffTime = duration - elapsedTime;
                if( diffTime > range1 && diffTime < range2 ) {
                    sendNotification("TESTo DI ProVa");
                }

            }

        };

        timer = new Timer(true);
        int delay = 0;
        int interval = INTERVAL;
        timer.schedule(task, delay, interval);
    }

    private void stopTimer() {
        stopSelf();
        if (timer != null) {
            timer.cancel();
        }
    }

    private void sendNotification(String text) {
        // create the intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.ic_stat_name;
        CharSequence tickerText = "30 minutes to go";
        CharSequence contentTitle = getText(R.string.app_name);
        CharSequence contentText = text;

        // create the notification and set its data
        Notification notification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setTicker(tickerText)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        // display the notification
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        final int NOTIFICATION_ID = 1;
        manager.notify(NOTIFICATION_ID, notification);
    }
}
