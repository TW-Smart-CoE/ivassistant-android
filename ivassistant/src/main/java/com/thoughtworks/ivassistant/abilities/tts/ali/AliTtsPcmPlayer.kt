package com.thoughtworks.ivassistant.abilities.tts.ali

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class AliTtsPcmPlayer(
    ttsInitializer: AliTtsInitializer
) {
    private fun getEncodeType(): Int {
        return AudioFormat.ENCODING_PCM_16BIT
    }

    private val encode = getEncodeType()
    private val audioTrack: AudioTrack
    private val sampleRate = ttsInitializer.ttsParams.sampleRate
    private val minBufferSize: Int = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        encode
    ) * 2

    init {
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
                    .setEncoding(encode)
                    .setSampleRate(sampleRate)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun start() {
        audioTrack.play()
        Log.d(AliTtsConstant.TAG, "AliTtsPcmPlayer start")
    }

    fun writeData(data: ByteArray) {
        audioTrack.write(data, 0, data.size)
    }

    fun stop() {
        audioTrack.apply {
            flush()
            pause()
            stop()
            Log.d(AliTtsConstant.TAG, "AliTtsPcmPlayer stop")
        }
    }
}