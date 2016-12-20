package com.guillaume_hermet.www.grooveairlineradio.asynctasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Guillaume on 11/24/16.
 */

public class currentTrackNotification {
    private MainActivity context;


    public currentTrackNotification(MainActivity context) {
        this.context = context;
    }

    public void buildNotification() {
        new buildNotificationAsync().execute();
    }

    private class buildNotificationAsync extends AsyncTask<String, Void, String> {
        private final String TAG = this.getClass().getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            buildNotification();
            return null;
        }

        private void buildNotification() {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.

// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
           /* PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            */
            // Sets an ID for the notification, so it can be updated
            if (context.getCurrentTrack() != null) {
                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_music_notification)
                                .setLargeIcon(getBitmapFromURL(context.getCurrentTrack().getCover()))
                                .setContentTitle(context.getCurrentTrack().getTitle())
                                .setContentText(context.getCurrentTrack().getArtist())
                                .setPriority(Notification.PRIORITY_MAX)
                                .setAutoCancel(true)
                                .setOngoing(true);

                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.

                context.setNotification(mBuilder.build());
                int notifyID = 1;
                mNotificationManager.notify(notifyID, context.getNotification());
            }
        }


        private Bitmap getBitmapFromURL(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                // Log exception
                Log.e(TAG, "failed to load large Icon for notification ");
                return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            }
        }
    }

}
