package com.gitlab.j_m_hoffmann.meditate.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gitlab.j_m_hoffmann.meditate.MainActivity
import com.gitlab.j_m_hoffmann.meditate.R
import com.gitlab.j_m_hoffmann.meditate.R.raw.metal_gong_by_dianakc
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_channel_id
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_message
import com.gitlab.j_m_hoffmann.meditate.R.string.notification_title
import com.gitlab.j_m_hoffmann.meditate.ui.util.NOTIFICATION_ID

class SessionEndedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        val contentIntent = Intent(context, MainActivity::class.java)

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        val builder = NotificationCompat.Builder(
            context,
            context.getString(notification_channel_id)
        )

        val sound = Uri.parse("$SCHEME_ANDROID_RESOURCE://${context.packageName}/$metal_gong_by_dianakc")

        builder
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setContentText(context.getString(notification_message))
            .setContentTitle(context.getString(notification_title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_timer_black_24dp)
            .setSound(sound)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}