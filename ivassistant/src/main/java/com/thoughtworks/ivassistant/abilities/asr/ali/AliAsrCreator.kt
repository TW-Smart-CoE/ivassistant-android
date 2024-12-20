package com.thoughtworks.ivassistant.abilities.asr.ali

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.alibaba.idst.nui.AsrResult
import com.alibaba.idst.nui.Constants
import com.alibaba.idst.nui.INativeNuiCallback
import com.alibaba.idst.nui.KwsResult
import com.alibaba.idst.nui.NativeNui
import com.google.gson.Gson
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.SAMPLE_RATE
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.TAG
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.WAVE_FRAM_SIZE
import com.thoughtworks.ivassistant.abilities.asr.ali.models.AliASRResult
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

class AliAsrCreator(
    private val context: Context,
    private val params: Map<String, Any>,
    private val initializer: AliAsrInitializer
) {
    private var isInit = false
    private val nuiInstance = NativeNui()
    private lateinit var audioRecorder: AudioRecord
    private val vadMode: AtomicBoolean = AtomicBoolean(false)
    private val gson = Gson()
    var asrCallback: AsrCallback? = null

    private val nuiCallback = object : INativeNuiCallback {
        override fun onNuiEventCallback(
            event: Constants.NuiEvent,
            resultCode: Int,
            arg2: Int,
            kwsResult: KwsResult?,
            asrResult: AsrResult?
        ) {
            Log.d(TAG, "event=$event")
            when (event) {
                Constants.NuiEvent.EVENT_ASR_RESULT -> {
                    asrResult?.let {
                        val result = gson.fromJson(it.asrResult, AliASRResult::class.java)
                        result.payload?.result?.let { payload ->
                            Log.d(TAG, "RESULT: $payload")
                            asrCallback?.onResult(payload)
                        }
                    }
                }

                Constants.NuiEvent.EVENT_ASR_PARTIAL_RESULT -> {
//                    Log.d(TAG, "PARTIAL_RESULT")
                }

                Constants.NuiEvent.EVENT_ASR_ERROR -> {
                    Log.e(TAG, "ERROR: $resultCode")
                    asrCallback?.onError("ERROR: $resultCode")
                }

                Constants.NuiEvent.EVENT_VAD_START -> {
                    Log.d(TAG, "onStartListening")
                }

                Constants.NuiEvent.EVENT_VAD_END -> {
                    Log.d(TAG, "onStopListening")
                }

                Constants.NuiEvent.EVENT_VAD_TIMEOUT -> {
                    Log.d(TAG, "onTimeout")
                }

                Constants.NuiEvent.EVENT_DIALOG_EX -> {
                    Log.d(TAG, "onDialogEx")
                    asrCallback?.onResult("")
                }

                else -> {
                    Log.d(TAG, event.name)
                }
            }
        }

        override fun onNuiNeedAudioData(buffer: ByteArray, len: Int): Int {
            var ret = 0
            if (audioRecorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "audio recorder not init")
                return -1
            }
            ret = audioRecorder.read(buffer, 0, len)
            return ret
        }

        override fun onNuiAudioStateChanged(state: Constants.AudioState?) {
//            Log.d(TAG, "onNuiAudioStateChanged");
            when (state) {
                Constants.AudioState.STATE_OPEN -> {
                    audioRecorder.startRecording()
                }

                Constants.AudioState.STATE_CLOSE -> {
                    audioRecorder.release()
                }

                Constants.AudioState.STATE_PAUSE -> {
                    audioRecorder.stop()
                }

                else -> {}
            }
        }

        override fun onNuiAudioRMSChanged(p0: Float) {
            asrCallback?.onVolumeChanged(p0)
        }

        override fun onNuiVprEventCallback(p0: Constants.NuiVprEvent?) {
            Log.d(TAG, "onNuiVprEventCallback event $p0")
        }
    }

    init {
        initAsrInstance()
    }

    private fun initAsrInstance() {
        val ticket = initializer.asrConfig.toTicket()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Manifest.permission.RECORD_AUDIO does not granted")
            return
        }

        //录音初始化，录音参数中格式只支持16bit/单通道，采样率支持8K/16K
        //使用者请根据实际情况选择Android设备的MediaRecorder.AudioSource
        //录音麦克风如何选择,可查看https://developer.android.google.cn/reference/android/media/MediaRecorder.AudioSource
        //录音初始化，录音参数中格式只支持16bit/单通道，采样率支持8K/16K
        //使用者请根据实际情况选择Android设备的MediaRecorder.AudioSource
        //录音麦克风如何选择,可查看https://developer.android.google.cn/reference/android/media/MediaRecorder.AudioSource
        audioRecorder = AudioRecord(
//            MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, WAVE_FRAM_SIZE * 4
        )

        //初始化SDK，注意用户需要在Auth.getAliYunTicket中填入相关ID信息才可以使用。
        val ret: Int = nuiInstance.initialize(
            nuiCallback,
            ticket,
            Constants.LogLevel.LOG_LEVEL_VERBOSE,
            true
        )

        Log.i(TAG, "result = $ret")
        if (ret != Constants.NuiResultCode.SUCCESS) {
            Log.e(TAG, "AliASR nuiInstance initialize failed")
            return
        }

        //设置相关识别参数，具体参考API文档
        nuiInstance.setParams(genParams())
        isInit = true

        Log.d(TAG, "AliASR initialize success")
    }

    private fun genParams(): String {
        var genParams = ""
        try {
            val nlsConfig = JSONObject()
            nlsConfig.put("enable_intermediate_result", true)
            //参数可根据实际业务进行配置
            //接口说明可见: https://help.aliyun.com/document_detail/173298.html
            //nls_config.put("enable_punctuation_prediction", true);
            //nls_config.put("enable_inverse_text_normalization", true);
            //nls_config.put("customization_id", "test_id");
            //nls_config.put("vocabulary_id", "test_id");
            nlsConfig.put("enable_voice_detection", true)
            nlsConfig.put("max_start_silence", 10000)
            nlsConfig.put("max_end_silence", 800)

            // https://help.aliyun.com/document_detail/316816.html
            nlsConfig.put("speech_noise_threshold", params["speech_noise_threshold"] ?: 0.8)
            //nls_config.put("sample_rate", 16000);
            //nls_config.put("sr_format", "opus");
            val parameters = JSONObject()
            parameters.put("nls_config", nlsConfig)
            parameters.put("service_type", Constants.kServiceTypeASR)

            //如果有HttpDns则可进行设置
            //parameters.put("direct_ip", Utils.getDirectIp());
            genParams = parameters.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return genParams
    }

    private fun genDialogParams(): String {
        var params = ""
        try {
            val dialogParam = JSONObject()
            params = dialogParam.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.d(TAG, "dialog params: $params")
        return params
    }

    fun create(asrCallback: AsrCallback?) {
        this.asrCallback = asrCallback
        if (!isInit) {
            initAsrInstance()
        }

        val vadMode1: Constants.VadMode = if (vadMode.get()) {
            Constants.VadMode.TYPE_VAD
        } else {
            Constants.VadMode.TYPE_P2T
        }
        val ret: Int = nuiInstance.startDialog(
            vadMode1,
            genDialogParams()
        )

        Log.d(TAG, "start done with $ret")
        if (ret != 0) {
            Log.e(TAG, "start dialog failed")
        }
    }

    fun stop() {
        asrCallback = null
        nuiInstance.stopDialog()
    }

    fun release() {
        nuiInstance.release()
    }
}