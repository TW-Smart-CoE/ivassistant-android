package com.thoughtworks.ivassistant.abilities.asr.baidu

import android.content.Context
import android.util.Log
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import com.thoughtworks.ivassistant.abilities.asr.baidu.BaiduAsrConstant.FINAL_RESULT
import com.thoughtworks.ivassistant.abilities.asr.baidu.BaiduAsrConstant.NLU_RESULT
import com.thoughtworks.ivassistant.abilities.asr.baidu.BaiduAsrConstant.PARTIAL_RESULT
import com.thoughtworks.ivassistant.abilities.asr.baidu.BaiduAsrConstant.TAG
import org.json.JSONObject
import kotlin.math.log

class BaiduAsrManager {
    private var eventManager: EventManager? = null
    private var asrCallback: AsrCallback? = null

    private val eventListener = EventListener { name, params, data, offset, length ->
        var logTxt = ""
        when (name) {
            SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL -> {
                if (params == null || params.isEmpty()) {
                    return@EventListener
                }
                if (params.contains(NLU_RESULT)) {
                    if (length > 0 && data.isNotEmpty()) {
                        logTxt += ", semantic parsing results\n：" + String(data, offset, length)
                    }
                } else if (params.contains(PARTIAL_RESULT)) {
                    logTxt += ", temporary recognition results\n：$params"
                } else if (params.contains(FINAL_RESULT)) {
                    val bestResult =
                        JSONObject(params).optString(BaiduAsrConstant.BEST_RESULT)
                    logTxt += "final recognition result：$bestResult"
                    asrCallback?.onResult(bestResult)
                    asrCallback = null
                } else {
                    logTxt += " params :$params"
                    if (data != null) {
                        logTxt += " data length=" + data.size
                    }
                }
            }

            SpeechConstant.CALLBACK_EVENT_ASR_ERROR -> {
                asrCallback?.onError(params)
                asrCallback = null
            }

            SpeechConstant.CALLBACK_EVENT_ASR_FINISH -> {
                if (logTxt.isEmpty()) {
                    asrCallback?.onResult("")
                    asrCallback = null
                }
            }

            SpeechConstant.CALLBACK_EVENT_ASR_VOLUME -> {
                Log.d(TAG, "asr volume: $params")
                val volume = JSONObject(params).optInt("volume")
                asrCallback?.onVolumeChanged(volume.toFloat())
            }

            else -> {
                logTxt += "name: $name"
                if (params != null && params.isNotEmpty()) {
                    logTxt += " ;params :$params"
                }
                if (data != null) {
                    logTxt += " ;data length=" + data.size
                }
            }
        }
        Log.d(TAG, logTxt)
    }

    fun create(context: Context, params: Map<String, Any>, asrCallback: AsrCallback?) {
        this.asrCallback = asrCallback

        eventManager = EventManagerFactory.create(context, "asr").also {
            it.registerListener(eventListener)
            it.send(SpeechConstant.ASR_START, JSONObject(params).toString(), null, 0, 0)
        }
    }

    fun stop() {
        asrCallback = null
        eventManager?.send(SpeechConstant.ASR_STOP, null, null, 0, 0)
        eventManager?.unregisterListener(eventListener)
        eventManager = null
    }

    fun release() {
        Log.d(TAG, "release")
    }
}