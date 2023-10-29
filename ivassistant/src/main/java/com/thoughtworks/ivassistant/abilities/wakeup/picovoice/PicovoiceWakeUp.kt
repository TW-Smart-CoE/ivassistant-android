package com.thoughtworks.ivassistant.abilities.wakeup.picovoice

import ai.picovoice.porcupine.PorcupineManager
import android.content.Context
import android.util.Log
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpCallback
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData

class PicovoiceWakeUp(
    private val context: Context,
    private val params: Map<String, Any> = emptyMap(),
) : WakeUp {
    private var keywordCount = 0
    private var wakeUpCallback: WakeUpCallback? = null
    private var porcupineManager: PorcupineManager? = null

    override fun initialize() {
        val keywordList = (params["keyword_paths"] as List<*>)
        keywordCount = keywordList.size
        val keywordArray =
            Array(keywordList.size) { i -> keywordList[i].toString() }

        try {
            val builder = PorcupineManager.Builder()
                .setAccessKey(
                    params["access_key"]?.toString() ?: context.getManifestMetaData(
                        META_DATA_ACCESS_KEY
                    )
                )
//                .setKeywords(arrayOf(Porcupine.BuiltInKeyword.PORCUPINE, Porcupine.BuiltInKeyword.BUMBLEBEE))
                .setKeywordPaths(keywordArray)

            params["model_path"]?.let {
                builder.setModelPath(it.toString())
            }

            porcupineManager = builder.build(context) { keywordIndex ->
                if (keywordIndex < keywordCount) {
                    wakeUpCallback?.onSuccess(keywordIndex)
                } else {
                    Log.e(TAG, "keywordIndex out of range")
                    wakeUpCallback?.onError(-2, "keywordIndex out of range")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "unknown error")
            wakeUpCallback?.onError(-1, e.message ?: "unknown error")
        }
    }

    override fun start(wakeUpCallback: WakeUpCallback?) {
        this.wakeUpCallback = wakeUpCallback
        porcupineManager?.start()
    }

    override fun stop() {
        porcupineManager?.stop()
        this.wakeUpCallback = null
    }

    override fun release() {
        this.wakeUpCallback = null
        porcupineManager?.delete()
    }

    companion object {
        private const val TAG = "IV.PicovoiceWakeUp"
        private const val META_DATA_ACCESS_KEY = "PICOVOICE_ACCESS_KEY"
    }
}