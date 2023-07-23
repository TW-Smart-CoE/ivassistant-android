package com.mxspace.ivassistant.abilities.wakeup.baidu

import android.content.Context
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import com.mxspace.ivassistant.abilities.wakeup.WakeUp
import com.mxspace.ivassistant.abilities.wakeup.WakeUpCallback
import com.mxspace.ivassistant.utils.Utils.getManifestMetaData
import org.json.JSONObject
import java.util.concurrent.ExecutorService

class BaiduWakeUp(
    private val context: Context,
    private val params: Map<String, Any> = emptyMap(),
    private val threadPool: ExecutorService,
) : WakeUp {
    private var isInited = false

    private var wakeUpCallback: WakeUpCallback? = null
    private val wp: EventManager = EventManagerFactory.create(context, "wp")
    private val eventListener: EventListener =
        EventListener { name, params, data, offset, length ->
            when (name) {
                SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS -> {
                    val result = WakeUpResult.parseJson(name, params)
                    if (result == null) {
                        this.wakeUpCallback?.onError(-1, "parse json error")
                    } else {
                        this.wakeUpCallback?.onSuccess()
                    }
                }

                SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR -> {
                    val errorCode = JSONObject(params).optInt("error")
                    val errorMessage = JSONObject(params).optString("desc")
                    this.wakeUpCallback?.onError(errorCode, errorMessage)
                }

                SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED -> {
                    this.wakeUpCallback?.onStop()
                }
            }
        }

    override fun initialize(wakeUpCallback: WakeUpCallback?) {
        this.wakeUpCallback = wakeUpCallback

        if (isInited) {
            return
        }

        wp.registerListener(eventListener)
        isInited = true
    }

    override fun start() {
        val wpParams: MutableMap<String, Any> = mutableMapOf()
        wpParams[SpeechConstant.APP_ID] =
            params["app_id"]?.toString()
                ?: context.getManifestMetaData(BaiduWakeUpConstant.META_DATA_APP_ID)
        wpParams[SpeechConstant.APP_KEY] =
            params["api_key"]?.toString()
                ?: context.getManifestMetaData(BaiduWakeUpConstant.META_DATA_API_KEY)
        wpParams[SpeechConstant.SECRET] =
            params["secret_key"]?.toString()
                ?: context.getManifestMetaData(BaiduWakeUpConstant.META_DATA_SECRET_KEY)

        wpParams[SpeechConstant.WP_WORDS_FILE] =
            params[SpeechConstant.WP_WORDS_FILE] ?: "assets:///WakeUp.bin"

        val json = (wpParams as Map<*, *>?)?.let { JSONObject(it).toString() }
        wp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0)
    }

    override fun stop() {
        wp.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0)
    }

    override fun release() {
        stop()
        wp.unregisterListener(eventListener)
        isInited = false
    }
}