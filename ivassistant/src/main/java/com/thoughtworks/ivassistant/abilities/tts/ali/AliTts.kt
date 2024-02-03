package com.thoughtworks.ivassistant.abilities.tts.ali

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsCallback
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTtsConstant.TAG
import java.util.*
import java.util.concurrent.ExecutorService

class AliTts(
    private val context: Context,
    private val ttsParams: Map<String, Any>,
    private val threadPool: ExecutorService,
) : Tts {
    private val ttsInitializer = AliTtsInitializer()
    private val ttsFileWriter = AliTtsFileWriter()
    private var ttsPlayer: AliTtsPcmPlayer? = null
    private var ttsCallback: TtsCallback? = null
    private val encodeType = ttsParams["encode_type"]?.toString() ?: "pcm"
    private val removeWavHeader = ttsParams["remove_wav_header"]?.toString()?.toBoolean() ?: true
    private val playSound = ttsParams["play_sound"]?.toString()?.toBoolean() ?: true
    private val stopAndStartDelay = ttsParams["stop_and_start_delay"]?.toString()?.toInt() ?: 100
    private var wavHeaderToBeRemove: Boolean = false

    init {
        ttsFileWriter.ttsFilePath = ttsParams["tts_file_path"]?.toString() ?: ""
    }

    private val aliTtsCreator = AliTtsCreator(
        ttsParams,
        ttsInitializer,
        object : AliTtsCreator.Callback {
            override fun onTtsStart() {
                wavHeaderToBeRemove = Build.VERSION.SDK_INT > Build.VERSION_CODES.Q
                if (ttsFileWriter.ttsFilePath.isNotEmpty()) {
                    ttsFileWriter.createFile()
                }
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTtsDataArrived(ttsData: AliTtsData) {
                val newAudioData: ByteArray =
                    if (encodeType == "wav" && removeWavHeader && wavHeaderToBeRemove) ttsData.data.copyOfRange(
                        44,
                        ttsData.data.size
                    ) else ttsData.data
                wavHeaderToBeRemove = false

                if (playSound && encodeType != "mp3") {
                    ttsPlayer?.writeData(newAudioData)
                }

                if (ttsFileWriter.ttsFilePath.isNotEmpty()) {
                    ttsFileWriter.writeData(ttsData.data)
                }
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTtsEnd() {
                ttsFileWriter.closeFile()
                if (ttsFileWriter.ttsFilePath.isNotEmpty()) {
                    ttsCallback?.onTTSFileSaved(ttsFileWriter.ttsFilePath)
                }

                wavHeaderToBeRemove = false

                ttsCallback?.onPlayEnd()
                stopPlay()
            }
        })

    override fun initialize() {
        threadPool.execute {
            ttsInitializer.init(context, ttsParams)
        }
    }

    override fun release() {
        aliTtsCreator.release()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun play(text: String, params: Map<String, Any>, ttsCallback: TtsCallback?) {
        if (!ttsInitializer.isInit) {
            Log.e(TAG, "TTS is not initialized!")
            return
        }

        if (playSound && encodeType != "mp3") {
            try {
                stopPlay()
                // Wait for the previous player to be released
                Thread.sleep(stopAndStartDelay.toLong())
                ttsPlayer = AliTtsPcmPlayer(ttsInitializer, encodeType)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create AliTtsPcmPlayer: ${t.message}")
                return
            }
        }

        this.ttsCallback = ttsCallback
        threadPool.execute {
            aliTtsCreator.create(text, params)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun stopPlay() {
        aliTtsCreator.stop()
        ttsPlayer?.release()
        ttsPlayer = null
        ttsCallback = null
    }
}