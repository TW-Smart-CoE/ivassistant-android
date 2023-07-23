package com.mxspace.ivassistant

import android.content.Context
import com.mxspace.ivassistant.abilities.asr.Asr
import com.mxspace.ivassistant.abilities.asr.ali.AliAsr
import com.mxspace.ivassistant.abilities.tts.Tts
import com.mxspace.ivassistant.abilities.tts.TtsType
import com.mxspace.ivassistant.abilities.tts.ali.AliTts
import com.thoughtworks.assistant.abilities.asr.AsrType
import java.util.concurrent.Executors

class IVAssistant(private val context: Context) {
    private val threadPool = Executors.newCachedThreadPool()

    fun createTts(ttsType: TtsType, params: Map<String, Any> = emptyMap()): Tts {
        return when (ttsType) {
            TtsType.Ali -> AliTts(context, params, threadPool)
            else -> throw IllegalArgumentException("Not supported type: ${ttsType.name}!")
        }
    }

    fun createAsr(asrType: AsrType, params: Map<String, Any> = emptyMap()): Asr {
        return when (asrType) {
            AsrType.Ali -> AliAsr(context, params, threadPool)
            else -> throw IllegalArgumentException("Not supported type: ${asrType.name}!")
        }
    }

    fun release() {
        threadPool.shutdown()
    }
}