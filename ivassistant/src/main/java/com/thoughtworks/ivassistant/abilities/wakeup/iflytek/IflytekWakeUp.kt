package com.thoughtworks.ivassistant.abilities.wakeup.iflytek

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechUtility
import com.iflytek.cloud.VoiceWakeuper
import com.iflytek.cloud.WakeuperListener
import com.iflytek.cloud.WakeuperResult
import com.thoughtworks.ivassistant.abilities.asr.iflytek.IflyTekConstant
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpCallback
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE
import com.iflytek.cloud.util.ResourceUtil



class IflytekWakeUp(
    val context: Context,
    val params: Map<String, Any> = mapOf(),
) : WakeUp {
    private var gson = Gson()
    private lateinit var ivw: VoiceWakeuper
    private val initListener = InitListener { code ->
        Log.d(
            TAG,
            "SpeechRecognizer init() code = $code"
        )
        if (code != ErrorCode.SUCCESS) {
            Log.d(
                TAG,
                "init failed, error code：$code, please visit https://www.xfyun.cn/document/error-code for help"
            )
        }
    }

    override fun initialize() {
        val appId =
            params["app_id"]?.toString()
                ?: context.getManifestMetaData(IflyTekConstant.META_IFLYTEK_IVS_APP_ID)
        SpeechUtility.createUtility(context, "appid=$appId")
        ivw = VoiceWakeuper.createWakeuper(context, initListener)
    }

    override fun start(wakeUpCallback: WakeUpCallback?) {
        ivw.stopListening()
        ivw.setParameter(SpeechConstant.IVW_SST, "wakeup")
        ivw.setParameter(SpeechConstant.IVW_RES_PATH, getResource())
        ivw.setParameter(SpeechConstant.IVW_NET_MODE, "1")
        ivw.startListening(object : WakeuperListener {
            override fun onBeginOfSpeech() {
            }

            override fun onResult(result: WakeuperResult) {
                Log.d(TAG, "onResult: ${result.resultString}")
                gson.fromJson(result.resultString, WakeUpEvent::class.java).let {
                    wakeUpCallback?.onSuccess(it.id)
                }
            }

            override fun onError(error: SpeechError) {
                Log.e(TAG, "onError: ${error.errorCode}, ${error.errorDescription}")
            }

            override fun onEvent(eventType: Int, isLast: Int, arg2: Int, obj: Bundle?) {
            }

            override fun onVolumeChanged(volume: Int) {
            }
        })
    }

    override fun stop() {
        ivw.stopListening()
    }

    override fun release() {
        ivw.destroy()
    }

    private fun getResource(): String {
        val resPath = ResourceUtil.generateResourcePath(context , RESOURCE_TYPE.assets, params["keywords"].toString())
        Log.d(TAG, "resPath: $resPath")
        return resPath
    }

    companion object {
        private const val TAG = "IflytekWakeUp"
    }
}