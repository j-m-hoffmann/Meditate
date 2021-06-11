package com.gitlab.j_m_hoffmann.meditate.receiver

import android.content.BroadcastReceiver
import android.content.ContentResolver.SCHEME_ANDROID_RESOURCE
import android.content.Context
import android.content.Intent
import android.media.AsyncPlayer
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import androidx.core.net.toUri
import com.gitlab.j_m_hoffmann.meditate.R

class SessionEndedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        val asyncPlayer = AsyncPlayer("gong")
        val uri = "$SCHEME_ANDROID_RESOURCE://${context.packageName}/${R.raw.metal_gong_by_dianakc}".toUri()

        if (Build.VERSION.SDK_INT >= 23) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            asyncPlayer.play(context, uri, false, audioAttributes)
        } else {
            @Suppress("DEPRECATION")
            asyncPlayer.play(context, uri, false, AudioManager.STREAM_ALARM)
/*
            MediaPlayer.create(context, R.raw.metal_gong_by_dianakc).apply {
                setVolume(0.2f, 0.2f)
                setOnCompletionListener { release() }
            }.run {
                start()
            }
*/
        }
    }
}