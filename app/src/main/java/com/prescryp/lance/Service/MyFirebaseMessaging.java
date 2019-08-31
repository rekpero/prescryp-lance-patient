package com.prescryp.lance.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.prescryp.lance.Misc.NotificationHelper;
import com.prescryp.lance.R;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBode = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();

        String dataDriverId = remoteMessage.getData().get("from_driver_id");
        String dataRideStatus = remoteMessage.getData().get("ride_status");
        String dataRideId = remoteMessage.getData().get("ride_id");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            showNotificationAPI26(messageTitle, messageBode, click_action, dataRideId, dataRideStatus);
        }else {
            showNotification(messageTitle, messageBode, click_action, dataRideId, dataRideStatus);
        }


        if (dataRideStatus.equals("Dropped")){
            Intent i = new Intent("android.intent.action.RIDERATING");
            i.putExtra("rideId", dataRideId);
            this.sendBroadcast(i);
        }else if (dataRideStatus.equals("Confirmed")){
            Intent i = new Intent("android.intent.action.RIDECONFIRMED");
            i.putExtra("rideStatus", dataRideStatus);
            this.sendBroadcast(i);
        }else if (dataRideStatus.equals("Declined")){
            Intent i = new Intent("android.intent.action.RIDECONFIRMED");
            i.putExtra("rideStatus", dataRideStatus);
            this.sendBroadcast(i);
        }




    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationAPI26(String messageTitle, String messageBode, String click_action, String dataRideId, String dataRideStatus) {
        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("rideId", dataRideId);
        resultIntent.putExtra("rideStatus", dataRideStatus);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper((getBaseContext()));
        Notification.Builder builder = notificationHelper.getCustomerNotification(messageTitle, messageBode, contentIntent, defaultSound);

        int mNotificationId = (int) System.currentTimeMillis();
        notificationHelper.getManager().notify(mNotificationId, builder.build());
    }

    private void showNotification(String messageTitle, String messageBode, String click_action, String dataRideId, String dataRideStatus) {
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                        .setContentText(messageBode)
                        .setContentTitle(messageTitle)
                        .setAutoCancel(true)
                        .setSound(defaultSound)
                        .setSmallIcon(R.drawable.logo_lance)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo_lance));
        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("rideId", dataRideId);
        resultIntent.putExtra("rideStatus", dataRideStatus);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotificationManager.notify(mNotificationId, mBuilder.build());

    }
}
