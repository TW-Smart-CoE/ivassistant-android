package com.thoughtworks.ivassistant.abilities.tts.ali

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsCallback
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTtsConstant.TAG
import java.util.*
import java.util.concurrent.ExecutorService

@RequiresApi(Build.VERSION_CODES.M)
class AliTts(
    private val context: Context,
    private val ttsParams: Map<String, Any>,
    private val threadPool: ExecutorService,
) : Tts {
    private val ttsInitializer = AliTtsInitializer()
    private val ttsFileWriter = AliTtsFileWriter()
    private val pcmPlayer: AliTtsPcmPlayer
    private var ttsCallback: TtsCallback? = null
    private val encodeType = ttsParams["encode_type"]?.toString() ?: "pcm"
    private val removeWavHeader = ttsParams["remove_wav_header"]?.toString()?.toBoolean() ?: true
    private val playSound = ttsParams["play_sound"]?.toString()?.toBoolean() ?: true
    private val stopAndStartDelay = ttsParams["stop_and_start_delay"]?.toString()?.toInt() ?: 50
    private var wavHeaderToBeRemove: Boolean = false

    init {
        ttsFileWriter.ttsFilePath = ttsParams["tts_file_path"]?.toString() ?: ""
        pcmPlayer = AliTtsPcmPlayer(ttsInitializer)
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
                    pcmPlayer.writeData(newAudioData)
                }

                if (ttsFileWriter.ttsFilePath.isNotEmpty()) {
                    ttsFileWriter.writeData(ttsData.data)
                }
            }

            override fun onTtsEnd() {
                finishFileWrite()
                wavHeaderToBeRemove = false
                ttsCallback?.onPlayEnd()

                stopPlay()
            }

            override fun onTtsCancel() {
                finishFileWrite()
                wavHeaderToBeRemove = false
                ttsCallback?.onPlayCancel()
            }
        })

    private fun finishFileWrite() {
        ttsFileWriter.closeFile()
        if (ttsFileWriter.ttsFilePath.isNotEmpty()) {
            ttsCallback?.onTTSFileSaved(ttsFileWriter.ttsFilePath)
        }
    }

    override fun initialize() {
        threadPool.execute {
            ttsInitializer.init(context, ttsParams)
        }
    }

    override fun release() {
        pcmPlayer.stop()
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
                pcmPlayer.start()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create AliTtsPcmPlayer: ${t.message}")
                return
            }
        }

        this.ttsCallback = ttsCallback
        threadPool.execute {
            // Wait for the previous tts play clear
            SystemClock.sleep(stopAndStartDelay.toLong())
            aliTtsCreator.create(text, params)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun stopPlay() {
        aliTtsCreator.stop()
        pcmPlayer.stop()
        ttsCallback = null
    }
}