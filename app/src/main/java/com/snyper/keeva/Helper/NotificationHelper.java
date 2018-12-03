package com.snyper.keeva.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.snyper.keeva.R;

/**
 * Created by stephen snyper on 10/8/2018.
 */

public class NotificationHelper extends ContextWrapper {
    private static final String KEEVA_CHANEL_ID="com.snyper.keeva";
    private static final String KEEVA_CHANEL_NAME="keeva";

    private NotificationManager manager;
    public NotificationHelper(Context base) {
        super(base);

            createChannel();
        //only working this function if API is 26 or higher
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel keevaChannel = new NotificationChannel(KEEVA_CHANEL_ID,KEEVA_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT

        );
        keevaChannel.enableLights(false);
        keevaChannel.enableVibration(true);
        keevaChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(keevaChannel);

    }

    public NotificationManager getManager() {
        if (manager==null)
            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getKeeveChannelNotification(String title, String body, PendingIntent contenIntent,
                                                                        Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(),KEEVA_CHANEL_ID)
                .setContentIntent(contenIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getKeeveChannelNotification(String title, String body,
                                                                        Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(),KEEVA_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_shopping_cart_black_24dp)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

}
