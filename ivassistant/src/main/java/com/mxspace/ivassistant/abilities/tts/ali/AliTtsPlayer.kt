package com.mxspace.ivassistant.abilities.tts.ali

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class AliTtsPlayer(
    ttsInitializer: AliTtsInitializer,
    encode: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    var audioTrack: AudioTrack? = null

    private val sampleRate = ttsInitializer.ttsParams.sampleRate
    private val minBufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        encode
    ) * 2

    init {
        initAudioTrack()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initAudioTrack() {
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    fun writeData(data: ByteArray) {
        audioTrack?.write(data, 0, data.size)
    }

    fun release() {
        audioTrack?.apply {
            flush()
            pause()
            stop()
        }
        audioTrack = null
    }
}