package com.example.tansanmusicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1="channel1";
    public static final String CHANNEL_ID_2="channel2";
    public static final String ACTION_PREVIOUS="ACTION_PREVIOUS";
    public static final String ACTION_NEXT_="ACTION_NEXT_";
    public static final String ACTION_PLAY="ACTION_PLAY";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel1=new NotificationChannel(CHANNEL_ID_1,"Channel(1)", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("channel 1 desc..");
            NotificationChannel channel2=new NotificationChannel(CHANNEL_ID_2,"Channel(2)",NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("chanell 2 desc...");
            NotificationManager notificationManager=getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}
