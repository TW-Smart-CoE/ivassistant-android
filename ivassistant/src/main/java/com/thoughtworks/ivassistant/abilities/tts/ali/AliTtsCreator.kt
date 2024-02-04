package com.thoughtworks.ivassistant.abilities.tts.ali

import android.util.Log
import com.alibaba.idst.nui.Constants
import com.alibaba.idst.nui.INativeTtsCallback
import com.alibaba.idst.nui.NativeNui
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTtsConstant.TAG
import java.util.*


class AliTtsCreator(
    private val params: Map<String, Any>,
    private val ttsInitializer: AliTtsInitializer,
    private val callback: Callback
) {
    interface Callback {
        fun onTtsStart()
        fun onTtsDataArrived(ttsData: AliTtsData)
        fun onTtsEnd()
        fun onTtsCancel()
    }

    private var isInit = false
    private val ttsInstance = NativeNui(Constants.ModeType.MODE_TTS)
    private var currentTaskId = ""

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
                    Log.d(TAG, "onTtsEventCallback event:$event taskId:$taskId resultCode:$resultCode")
                    when (event) {
                        INativeTtsCallback.TtsEvent.TTS_EVENT_START -> {
                            callback.onTtsStart()
                        }

                        INativeTtsCallback.TtsEvent.TTS_EVENT_END -> {
                            callback.onTtsEnd()
                        }

                        INativeTtsCallback.TtsEvent.TTS_EVENT_CANCEL -> {
                            callback.onTtsCancel()
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

    private fun formatSSML(
        fontName: String,
        emotion: String,
        intensity: Float,
        text: String
    ): String {
        return "<speak voice=\"$fontName\">\n" +
                "    <emotion category=\"$emotion\" intensity=\"$intensity\">$text</emotion>\n" +
                "</speak>"
    }

    fun create(text: String, playParams: Map<String, Any>) {
        if (!isInit) {
            initTTSInstance()
        }

        if (text.isEmpty()) return

        val charNum = ttsInstance.getUtf8CharsNum(text)

        val ttsVersion = if (charNum > MAX_TEXT_NUM) 1 else 0
        ttsInstance.setparamTts("tts_version", ttsVersion.toString())
        if (playParams.isNotEmpty()) {
            val fontName = playParams["font_name"]
            fontName?.let {
                if (it.toString().endsWith("_emo")) {
                    val emotion = playParams["emotion"]?.toString() ?: "neutral"
                    val intensity = playParams["intensity"]?.toString()?.toFloat() ?: 1.0f
                    val ssml = formatSSML(it.toString(), emotion, intensity, text)
                    ttsInstance.startTts("1", generateTaskId(), ssml)
                }
            }
        } else {
            ttsInstance.startTts("1", generateTaskId(), text)
        }
    }

    @Synchronized
    fun generateTaskId(): String {
        currentTaskId = UUID.randomUUID().toString().replace("-", "")
        return currentTaskId
    }

    @Synchronized
    fun getCurrentTaskId(): String {
        return currentTaskId
    }

    @Synchronized
    fun clearCurrentTaskId() {
        currentTaskId = ""
    }

    fun stop() {
        getCurrentTaskId().let {
            if (it.isNotEmpty()) {
                ttsInstance.cancelTts(it)
            }
        }
        ttsInstance.stopDialog()
        clearCurrentTaskId()
    }

    fun release() {
        ttsInstance.release()
    }

    companion object {
        const val MAX_TEXT_NUM = 300
    }
}