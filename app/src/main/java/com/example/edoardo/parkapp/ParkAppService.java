package com.example.edoardo.parkapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class ParkAppService extends NotificationListenerService{

    public final static int INTERVAL = 20000;
    public final static long MILLISECONDS_PER_MINUTE = 60000;
    public final static int NO_NOTIFICATION = 0;
    public final static int SINGLE_NOTIFICATION = 1;
    public final static int DOUBLE_NOTIFICATION = 2;

    private ParkAppApplicationObject app;
    private Timer timer;
    private SharedPreferences sharedPreferences;

    private long endTimeMillis;
    private boolean hasFirstNotificationHappened;
    private int notificationType;
    private long notificationTimeMillis;
    private boolean isRingingEnabled;
    private long duration;

    @Override
    public void onCreate() {
        Log.d("Park App", "Service created");
        app = (ParkAppApplicationObject) getApplication();
        sharedPreferences = this.getSharedPreferences("SAVED_VALUES", MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Park App", "Service started");
        startTimer();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Park App", "Service bound - not used!");
        return null;
    }

    @Override
    public void onDestroy() {
        /*lo stato è determinato da quanto tempo manca e se è già stata vista o meno la notifica
        * in base a quello decido cosa fare con le preferences*/
        Log.d("Park App", "Service destroyed");
        stopTimer();
        Toast.makeText(this, "service destroyed", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void startTimer() {

        endTimeMillis = sharedPreferences.getLong("endTimeMillis", -1);
        notificationType = sharedPreferences.getInt("pref_notification", 2);
        notificationTimeMillis = sharedPreferences.getLong("pref_period_notification", 30*MILLISECONDS_PER_MINUTE);
        isRingingEnabled = sharedPreferences.getBoolean("pref_ringing_notification", true);
        duration = sharedPreferences.getLong("duration", 0);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {


                if(notificationType != NO_NOTIFICATION) {
                    /*ricostruisco lo stato del servizio di notifiche prelevando il dato dalle shared prefereces*/
                    hasFirstNotificationHappened = sharedPreferences.getBoolean("hasFirstNotificationHappened", false);

                    long alarmTimeMillis = notificationTimeMillis /2;//- MILLISECONDS_PER_MINUTE;//per ora

                    if(duration <= notificationTimeMillis) {
                        alarmTimeMillis = duration / 2;
                    }

                    long remainingTimeToEndMillis = endTimeMillis - System.currentTimeMillis();
                    String debugNotification = "End " + remainingTimeToEndMillis/1000;
                    debugNotification += " NTs "+ notificationTimeMillis/1000;
                    debugNotification += " Rng "+ isRingingEnabled;
                    debugNotification += " Flg " + hasFirstNotificationHappened;
                    debugNotification += " nTY "+notificationType;
                    sendNotification(debugNotification, false);

                    if ( remainingTimeToEndMillis > alarmTimeMillis ||
                            ((remainingTimeToEndMillis <= alarmTimeMillis) && hasFirstNotificationHappened)) {

                        if (!hasFirstNotificationHappened && remainingTimeToEndMillis < notificationTimeMillis
                                && remainingTimeToEndMillis > alarmTimeMillis) {//notifica che manca tot tempo
                            /*
                            * inserisco nelle shared preferences il fatto che è avvenuta la notifica che segnala
                            * la mancanza di tot tempo alla fine, cosicche in caso di spegnimento del dispositivo
                            * e conseguente riaccensione si è in grado di ricostruire lo stato
                            * */
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("hasFirstNotificationHappened", true);
                            editor.commit();

                            if( duration > notificationTimeMillis ) {
                                sendNotification("Mancano " + remainingTimeToEndMillis + " ms", false);
                            }
                            if(notificationType == SINGLE_NOTIFICATION ) {
                                clearService();
                            }
                        } else if ((hasFirstNotificationHappened && remainingTimeToEndMillis <= alarmTimeMillis)
                                    && notificationType == DOUBLE_NOTIFICATION){//allarme
                            sendNotification("ALLARME, mancano "+remainingTimeToEndMillis+" ms", isRingingEnabled);
                            clearService();
                        } else {
                            //sendNotification("STATO NON PREVISTO "+remainingTimeToEndMillis+" ms", false);
                        }
                    } else {//invio una notifica di allarme scaduto (stato possibile se ho spento il telefono)

                        String notificationString = "";

                        if( remainingTimeToEndMillis <= 0 ) {
                            notificationString = "SCADUTO DA "+ (-1*remainingTimeToEndMillis/MILLISECONDS_PER_MINUTE)+" m";
                        } else {
                            notificationString = "MANCANO "+remainingTimeToEndMillis/MILLISECONDS_PER_MINUTE+" m";
                        }


                        sendNotification(notificationString, isRingingEnabled);
                        clearService();

                    }
                } else {
                    clearService();
                }
            }

        };

        timer = new Timer(true);
        int delay = 0;
        int interval = INTERVAL;
        timer.schedule(task, delay, interval);
    }

    private void removeFlag() {

        /*garantisce che ad un secondo riavvio, non ci sia uno stato iniziale con  hasFirstNotificationHappened == true
        * che impedirebbe l'avviso che manca tot tempo alla fine del parcheggio*/
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("hasFirstNotificationHappened");
        editor.commit();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        stopSelf();
    }

    private void clearService() {
        stopTimer();
        removeFlag();
    }

    private void sendNotification(String text, boolean isRingingEnabled) {
        // create the intent for the notification
        Intent notificationIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // create the pending intent
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, flags);

        // create the variables for the notification
        int icon = R.drawable.ic_stat_name;
        CharSequence tickerText = "Ticker Text";
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

        //
        if(isRingingEnabled) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            notification.sound = alarmSound;
        } else {
            notification.defaults |= Notification.DEFAULT_ALL;
        }


        // display the notification
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        final int NOTIFICATION_ID = 1;
        manager.notify(NOTIFICATION_ID, notification);

    }

    /*@Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+100, restartServicePendingIntent);
    }*/
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //// TODO: 22/06/2017
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //// TODO: 22/06/2017
    }

    @Override
    public void onLowMemory() {
        //// TODO: 22/06/2017
    }


}
