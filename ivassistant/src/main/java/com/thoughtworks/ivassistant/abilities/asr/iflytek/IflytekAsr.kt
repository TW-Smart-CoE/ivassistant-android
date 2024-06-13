package com.thoughtworks.ivassistant.abilities.asr.iflytek

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.GrammarListener
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.RecognizerListener
import com.iflytek.cloud.RecognizerResult
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechRecognizer
import com.iflytek.cloud.SpeechUtility
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import com.thoughtworks.ivassistant.abilities.asr.iflytek.util.JsonParser
import com.thoughtworks.ivassistant.abilities.asr.iflytek.util.parseAsrResponse
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData

class IflytekAsr(
    val context: Context,
    val params: Map<String, Any> = mapOf(),
) : Asr {
    private val engineType = SpeechConstant.TYPE_CLOUD
    private lateinit var asr: SpeechRecognizer
    private val cloudGrammar = """
       #ABNF 1.0 UTF-8;
        language zh-CN;
        mode voice;

        root \${'$'}main;
        \${'$'}main = \${'$'}place1 到 \${'$'}place2;
        \${'$'}place1 = 北京|武汉|南京|天津|东京;
        \${'$'}place2 = 上海|合肥; 
    """.trimIndent()
    private var grammarID = ""

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
        asr = SpeechRecognizer.createRecognizer(context, initListener)
        setParams()
    }

    override fun startListening(asrCallback: AsrCallback?) {
        asr.stopListening()

        val ret = asr.startListening(IflyTekRecognizerListener(engineType, asrCallback))
        if (ret != ErrorCode.SUCCESS) {
            Log.e(
                TAG,
                "detect failed, error code: $ret, please visit https://www.xfyun.cn/document/error-code for help"
            )
        }
    }

    override fun stopListening() {
        asr.stopListening()
    }

    override fun release() {
        asr.cancel()
        asr.destroy()
    }

    class IflyTekRecognizerListener(
        private val engineType: String,
        private val asrCallback: AsrCallback?,
    ) : RecognizerListener {
        private var recognizedBuffer: StringBuffer = StringBuffer()
        override fun onVolumeChanged(volume: Int, data: ByteArray) {
//            Log.d(TAG, "speaking，volume:：$volume")
//            Log.d(TAG, "return volume data：" + data.size)
        }

        override fun onResult(result: RecognizerResult, isLast: Boolean) {
//            Log.d(TAG, "recognizer result：" + result.resultString + " $isLast")
            val text: String = if ("cloud".equals(engineType, ignoreCase = true)) {
                JsonParser.parseGrammarResult(result.resultString)
            } else {
                JsonParser.parseLocalGrammarResult(result.resultString)
            }

            val parseResult = parseAsrResponse(result.resultString)
            recognizedBuffer.append(parseResult)
            if (isLast) {
                Log.d(TAG, recognizedBuffer.toString())
                asrCallback?.onResult(recognizedBuffer.toString())
            }
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
        }

        override fun onBeginOfSpeech() {
            Log.d(TAG, "onBeginOfSpeech")
        }

        override fun onError(error: SpeechError) {
            Log.e(TAG, "onError Code：" + error.errorCode)
            asrCallback?.onError(error.errorDescription)
        }

        override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {
        }
    }

    private val cloudGrammarListener: GrammarListener =
        GrammarListener { grammarId, error ->
            if (error == null) {
                grammarID = grammarId
            } else {
                Log.e(
                    TAG,
                    "build grammar failed, error code: " + error.errorCode + ", Please visit https://www.xfyun.cn/document/error-code for help"
                )
            }
        }

    private fun setParams() {
        asr.setParameter(SpeechConstant.CLOUD_GRAMMAR, null)
        asr.setParameter(SpeechConstant.SUBJECT, null)
        asr.setParameter(SpeechConstant.RESULT_TYPE, "json")
        asr.setParameter(SpeechConstant.ENGINE_TYPE, engineType)
        asr.setParameter(SpeechConstant.LANGUAGE, params["language"]?.toString() ?: "zh_cn")
        asr.setParameter(SpeechConstant.ACCENT, params["accent"]?.toString() ?: "mandarin")
        asr.setParameter(SpeechConstant.VAD_BOS, params["vad_bos"]?.toString() ?: "4000")
        asr.setParameter(SpeechConstant.VAD_EOS, params["vad_eos"]?.toString() ?: "1000")
        asr.setParameter(SpeechConstant.ASR_PTT, params["asr_ptt"]?.toString() ?: "1")
    }

    companion object {
        private const val TAG = "IV.IflytekAsr"
    }
}