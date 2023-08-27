package com.thoughtworks.ivassistant.abilities.asr.baidu

import android.content.Context
import com.baidu.speech.asr.SpeechConstant
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import com.thoughtworks.ivassistant.abilities.wakeup.baidu.BaiduWakeUpConstant
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData

class BaiduAsr(
    val context: Context,
    val params: Map<String, Any> = mapOf(),
) : Asr {
    private val baiduAsrManager = BaiduAsrManager()
    private val bdAsrParams: MutableMap<String, Any> = mutableMapOf()

    override fun initialize() {
        params.forEach {
            bdAsrParams[it.key] = it.value
        }

        bdAsrParams[SpeechConstant.APP_ID] =
            params["app_id"]?.toString()
                ?: context.getManifestMetaData(BaiduWakeUpConstant.META_DATA_APP_ID)
        bdAsrParams[SpeechConstant.APP_KEY] =
            params["api_key"]?.toString()
                ?: context.getManifestMetaData(BaiduWakeUpConstant.META_DATA_API_KEY)
        bdAsrParams[SpeechConstant.SECRET] =
            params["secret_key"]?.toString()
                ?: context.getManifestMetaData(BaiduWakeUpConstant.META_DATA_SECRET_KEY)
    }

    override fun startListening(asrCallback: AsrCallback?) {
        stopListening()
        baiduAsrManager.create(context, bdAsrParams, asrCallback)
    }

    override fun stopListening() {
        baiduAsrManager.stop()
    }

    override fun release() {
        baiduAsrManager.release()
    }
}