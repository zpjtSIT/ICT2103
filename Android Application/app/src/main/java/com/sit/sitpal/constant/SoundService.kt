package com.sit.sitpal.constant

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.sit.sitpal.R


class SoundService: Service() {

    var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
//        player = MediaPlayer.create(this, R.raw.music)
        player = MediaPlayer.create(this, R.raw.music)
        player?.isLooping = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player?.start()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.start()
        player?.release()
        stopSelf()
        super.onDestroy()
    }

}