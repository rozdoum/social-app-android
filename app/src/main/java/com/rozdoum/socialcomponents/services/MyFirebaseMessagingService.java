/*
 *
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.rozdoum.socialcomponents.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.activities.PostDetailsActivity;
import com.rozdoum.socialcomponents.utils.LogUtil;

/**
 * Created by alexey on 13.04.17.
 */


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private static final String POST_ID_KEY = "postId";
    private static final String ACTION_TYPE_KEY = "actionType";
    private static final String ACTION_TYPE_NEW_LIKE = "new_like";
    private static final String ACTION_TYPE_NEW_COMMENT = "new_comment";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null && remoteMessage.getData().get(ACTION_TYPE_KEY) != null) {
            handleRemoteMessage(remoteMessage);
        } else {
            LogUtil.logError(TAG, "onMessageReceived()", new RuntimeException("FCM remoteMessage doesn't contains Action Type"));
        }
    }

    private void handleRemoteMessage(RemoteMessage remoteMessage) {
        String receivedActionType = remoteMessage.getData().get(ACTION_TYPE_KEY);
        LogUtil.logDebug(TAG, "Message Notification Action Type: " + receivedActionType);

        switch (receivedActionType) {
            case ACTION_TYPE_NEW_LIKE:
                parseCommentOrLike(remoteMessage);
                break;
            case ACTION_TYPE_NEW_COMMENT:
                parseCommentOrLike(remoteMessage);
                break;
        }
    }

    private void parseCommentOrLike(RemoteMessage remoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            LogUtil.logDebug(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String notificationTitle = remoteMessage.getNotification().getTitle();
            String notificationBody = remoteMessage.getNotification().getBody();
            String notificationImageUrl = remoteMessage.getNotification().getIcon();
            String postId = remoteMessage.getData().get(POST_ID_KEY);

            Intent intent = new Intent(this, PostDetailsActivity.class);
            intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, postId);

            Bitmap bitmap = getBitmapFromUrl(notificationImageUrl);

            sendNotification(notificationTitle, notificationBody, bitmap, intent);
        } else {
            LogUtil.logError(TAG, "parseCommentOrLike()", new RuntimeException("FCM remoteMessage doesn't contains Notification"));
        }
    }

    public Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            return Glide.with(this)
                    .load(imageUrl)
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(Constants.PushNotification.LARGE_ICONE_SIZE, Constants.PushNotification.LARGE_ICONE_SIZE)
                    .get();

        } catch (Exception e) {
            LogUtil.logError(TAG, "getBitmapfromUrl", e);
            return null;

        }
    }

    private void sendNotification(String notificationTitle, String notificationBody, Bitmap bitmap, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setAutoCancel(true)   //Automatically delete the notification
                .setSmallIcon(R.drawable.ic_push_notification_small) //Notification icon
                .setContentIntent(pendingIntent)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setLargeIcon(bitmap)
                .setSound(defaultSoundUri);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
