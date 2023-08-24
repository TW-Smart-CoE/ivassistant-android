package com.thoughtworks.ivassistant.abilities.tts.ali

import android.util.Log
import com.alibaba.idst.nui.Constants
import com.alibaba.idst.nui.INativeTtsCallback
import com.alibaba.idst.nui.NativeNui
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTtsConstant.TAG


class AliTtsCreator(
    private val params: Map<String, Any>,
    private val ttsInitializer: AliTtsInitializer,
    private val callback: Callback
) {
    interface Callback {
        fun onTtsStart()
        fun onTtsDataArrived(data: AliTtsData)
        fun onTtsEnd()
    }

    private var isInit = false
    private val ttsInstance = NativeNui(Constants.ModeType.MODE_TTS)

    private fun initTTSInstance() {
        if (isInit) {
            Log.d(TAG, "TTS instance has been initialized")
            return
        }

        val ticket = ttsInitializer.ttsConfig.toTicket()
        val initResult = ttsInstance.tts_initialize(
            object : INativeTtsCallback {
                override fun onTtsEventCallback(
                    event: INativeTtsCallback.TtsEvent,
                    taskId: String,
                    resultCode: Int
                ) {
                    when (event) {
                        INativeTtsCallback.TtsEvent.TTS_EVENT_START -> {
                            callback.onTtsStart()
                        }

                        INativeTtsCallback.TtsEvent.TTS_EVENT_END -> {
                            callback.onTtsEnd()
                        }

                        INativeTtsCallback.TtsEvent.TTS_EVENT_ERROR -> {
                            val errorMsg = ttsInstance.getparamTts("error_msg")
                            Log.e(TAG, "TTS_EVENT_ERROR error_code:$resultCode err_msg:$errorMsg")
                        }

                        else -> {}
                    }
                }

                override fun onTtsDataCallback(info: String, infoLen: Int, data: ByteArray) {
                    if (data.isNotEmpty()) {
                        callback.onTtsDataArrived(AliTtsData(info, infoLen, data))
                    }
                }

                override fun onTtsVolCallback(vol: Int) {
                    // do nothing
                }
            },
            ticket,
            Constants.LogLevel.LOG_LEVEL_VERBOSE,
            true
        )

        if (initResult == Constants.NuiResultCode.SUCCESS) {
            val ttsParams = ttsInitializer.ttsParams.toParams(params)
            ttsParams.forEach {
                ttsInstance.setparamTts(it.key, it.value.toString())
            }
            isInit = true
        }
    }

    fun create(text: String) {
        if (!isInit) {
            initTTSInstance()
        }

        if (text.isEmpty()) return

        val charNum = ttsInstance.getUtf8CharsNum(text)

        val ttsVersion = if (charNum > MAX_TEXT_NUM) 1 else 0
        ttsInstance.setparamTts("tts_version", ttsVersion.toString())
        ttsInstance.startTts("1", "", text)
    }

    fun release() {
        ttsInstance.release()
    }

    companion object {
        const val MAX_TEXT_NUM = 300
    }
}