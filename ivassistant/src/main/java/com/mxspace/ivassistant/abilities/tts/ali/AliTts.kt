package com.mxspace.ivassistant.abilities.tts.ali

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.mxspace.ivassistant.abilities.tts.Tts
import com.mxspace.ivassistant.abilities.tts.TtsCallback
import com.mxspace.ivassistant.abilities.tts.ali.AliTtsConstant.TAG
import java.util.concurrent.ExecutorService

class AliTts(
    private val context: Context,
    private val params: Map<String, Any>,
    private val threadPool: ExecutorService,
) : Tts {
    private val ttsInitializer = AliTtsInitializer()
    private var ttsPlayer: AliTtsPlayer? = null
    private var ttsCallback: TtsCallback? = null

    private val aliTtsCreator = AliTtsCreator(params,
        ttsInitializer,
        object : AliTtsCreator.Callback {
            override fun onTtsStart() {
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTtsDataArrived(data: AliTtsData) {
                ttsPlayer?.writeData(data.data)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onTtsEnd() {
                ttsCallback?.onPlayEnd()
                stopPlay()
            }
        })

    override fun initialize(ttsCallback: TtsCallback?) {
        this.ttsCallback = ttsCallback

        threadPool.execute {
            ttsInitializer.init(context, params)
        }
    }

    override fun release() {
        aliTtsCreator.release()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun play(text: String) {
        if (!ttsInitializer.isInit) {
            Log.e(TAG, "TTS is not initialized!")
            return
        }

        stopPlay()
        ttsPlayer = AliTtsPlayer(ttsInitializer)
        threadPool.execute {
            aliTtsCreator.create(text)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun stopPlay() {
        ttsPlayer?.release()
        ttsPlayer = null
    }
}