package com.thoughtworks.ivassistant

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrType
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsr
import com.thoughtworks.ivassistant.abilities.asr.baidu.BaiduAsr
import com.thoughtworks.ivassistant.abilities.asr.iflytek.IflytekAsr
import com.thoughtworks.ivassistant.abilities.chat.Chat
import com.thoughtworks.ivassistant.abilities.chat.ChatType
import com.thoughtworks.ivassistant.abilities.chat.chatgpt.ChatGpt
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsType
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTts
import com.thoughtworks.ivassistant.abilities.tts.google.GoogleTts
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpType
import com.thoughtworks.ivassistant.abilities.wakeup.baidu.BaiduWakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.picovoice.PicovoiceWakeUp
import java.util.concurrent.Executors

class IVAssistant(private val context: Context) {
    private val threadPool = Executors.newCachedThreadPool()

    @RequiresApi(Build.VERSION_CODES.M)
    fun createTts(ttsType: TtsType, params: Map<String, Any> = emptyMap()): Tts {
        return when (ttsType) {
            TtsType.Ali -> AliTts(context, params, threadPool)
            TtsType.Google -> GoogleTts(context, params)
            else -> throw IllegalArgumentException("Not supported type: ${ttsType.name}!")
        }
    }

    fun createAsr(asrType: AsrType, params: Map<String, Any> = emptyMap()): Asr {
        return when (asrType) {
            AsrType.Ali -> AliAsr(context, params, threadPool)
            AsrType.Baidu -> BaiduAsr(context, params)
            AsrType.Iflytek -> IflytekAsr(context, params)
            else -> throw IllegalArgumentException("Not supported type: ${asrType.name}!")
        }
    }

    fun createWakeUp(wakeUpType: WakeUpType, params: Map<String, Any> = emptyMap()): WakeUp {
        return when (wakeUpType) {
            WakeUpType.Baidu -> BaiduWakeUp(context, params)
            WakeUpType.Picovoice -> PicovoiceWakeUp(context, params)
            else -> throw IllegalArgumentException("Not supported type: ${wakeUpType.name}!")
        }
    }

    fun createChat(chatType: ChatType, params: Map<String, Any> = emptyMap()): Chat {
        return when (chatType) {
            ChatType.ChatGpt -> ChatGpt(context, params)
            else -> throw IllegalArgumentException("Not supported type: ${chatType.name}!")
        }
    }

    fun release() {
        threadPool.shutdown()
    }
}