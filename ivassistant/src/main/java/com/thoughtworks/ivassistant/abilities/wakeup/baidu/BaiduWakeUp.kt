package com.thoughtworks.ivassistant.abilities.wakeup.baidu

import android.content.Context
import android.util.Log
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpCallback
import com.thoughtworks.ivassistant.abilities.wakeup.baidu.BaiduWakeUpConstant.TAG
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData
import org.json.JSONObject

class BaiduWakeUp(
    private val context: Context,
    private val params: Map<String, Any> = emptyMap(),
) : WakeUp {
    private var isInited = false
    private var keywords: List<String>? = null

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
                        if (keywords?.contains(result.word) == true) {
                            this.wakeUpCallback?.onSuccess(keywords?.indexOf(result.word) ?: -1)
                        } else {
                            this.wakeUpCallback?.onSuccess(-1)
                        }
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

    override fun initialize() {
        if (isInited) {
            return
        }

        wp.registerListener(eventListener)
        isInited = true
    }

    override fun start(wakeUpCallback: WakeUpCallback?) {
        this.wakeUpCallback = wakeUpCallback

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

        Log.d(TAG, "app_id: ${wpParams[SpeechConstant.APP_ID]}")
        Log.d(TAG, "api_key: ${wpParams[SpeechConstant.APP_KEY]}")
        Log.d(TAG, "secret_key: ${wpParams[SpeechConstant.SECRET]}")

        wpParams[SpeechConstant.WP_WORDS_FILE] =
            params[SpeechConstant.WP_WORDS_FILE] ?: "assets:///WakeUp.bin"

        this.keywords = params["keywords"] as List<String>?

        Log.d(TAG, "wp_words_file: ${wpParams[SpeechConstant.WP_WORDS_FILE]}")

        val json = (wpParams as Map<*, *>?)?.let { JSONObject(it).toString() }
        wp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0)
    }

    override fun stop() {
        wp.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0)
        this.wakeUpCallback = null
        this.keywords = null
    }

    override fun release() {
        stop()
        wp.unregisterListener(eventListener)
        isInited = false
    }
}