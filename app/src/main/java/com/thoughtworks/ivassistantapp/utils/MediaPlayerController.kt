package com.thoughtworks.ivassistantapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

class MediaPlayerController(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(soundFile: File, volume: Float = 1f, onCompletionListener: (() -> Unit)? = null) {
        stop()
        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(soundFile))
        mediaPlayer?.apply {
            setOnCompletionListener {
                onCompletionListener?.invoke()
            }
            setVolume(volume, volume)
            start()
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
