package com.mxspace.ivassistant

import android.content.Context
import com.mxspace.ivassistant.abilities.tts.Tts
import com.mxspace.ivassistant.abilities.tts.TtsType
import com.mxspace.ivassistant.abilities.tts.ali.AliTts
import java.util.concurrent.Executors

class IVAssistant(private val context: Context) {
    private val threadPool = Executors.newCachedThreadPool()

    fun createTts(ttsType: TtsType, params: Map<String, Any> = emptyMap()): Tts {
        return when (ttsType) {
            TtsType.Ali -> AliTts(context, params, threadPool)
            else -> throw IllegalArgumentException("Not supported type: ${ttsType.name}!")
        }
    }

    fun release() {
        threadPool.shutdown()
    }
}