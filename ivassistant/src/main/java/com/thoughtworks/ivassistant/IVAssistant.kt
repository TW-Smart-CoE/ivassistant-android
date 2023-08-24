package com.thoughtworks.ivassistant

import android.content.Context
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrType
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsr
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsType
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTts
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpType
import com.thoughtworks.ivassistant.abilities.wakeup.baidu.BaiduWakeUp
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

    fun createWakeUp(wakeUpType: WakeUpType, params: Map<String, Any> = emptyMap()): WakeUp {
        return when (wakeUpType) {
            WakeUpType.Baidu -> BaiduWakeUp(context, params, threadPool)
            else -> throw IllegalArgumentException("Not supported type: ${wakeUpType.name}!")
        }
    }

    fun release() {
        threadPool.shutdown()
    }
}