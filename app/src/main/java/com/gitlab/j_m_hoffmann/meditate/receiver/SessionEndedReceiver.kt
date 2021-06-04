package com.gitlab.j_m_hoffmann.meditate.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.gitlab.j_m_hoffmann.meditate.R

class SessionEndedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        MediaPlayer.create(context, R.raw.metal_gong_by_dianakc).apply {
            setVolume(0.2f, 0.2f)
            setOnCompletionListener { release() }
        }.run {
            start()
        }
    }
}