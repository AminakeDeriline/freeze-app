package com.example.freeze;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class AppCheckService extends Service {
    private Handler handler = new Handler();
    private DatabaseHelper db;

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            String topPackage = getForegroundPackage();
            if (db != null && db.isAppFrozen(topPackage)) {

                // Intent for the Main List (The Parent)
                Intent mainIntent = new Intent(AppCheckService.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Intent for the Lock Screen (The Top)
                Intent lockIntent = new Intent(AppCheckService.this, Active_Freeze.class);
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Start them together so Main is behind Active_Freeze
                Intent[] intents = {mainIntent, lockIntent};
                startActivities(intents);
            }
            handler.postDelayed(this, 80);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHelper(this);

        String channelId = "freeze_service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(channelId, "Freeze Guard", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(chan);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Freeze Shield Active")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);
        handler.post(checkRunnable);
    }

    private String getForegroundPackage() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        UsageEvents events = usm.queryEvents(now - 2000, now);
        UsageEvents.Event event = new UsageEvents.Event();
        String lastPkg = "";
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPkg = event.getPackageName();
            }
        }
        return lastPkg;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() { handler.removeCallbacks(checkRunnable); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}