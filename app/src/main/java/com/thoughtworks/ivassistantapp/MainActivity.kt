package com.thoughtworks.ivassistantapp

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.thoughtworks.ivassistant.IVAssistant
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import com.thoughtworks.ivassistant.abilities.asr.AsrType
import com.thoughtworks.ivassistant.abilities.chat.Chat
import com.thoughtworks.ivassistant.abilities.chat.ChatCallback
import com.thoughtworks.ivassistant.abilities.chat.ChatType
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsCallback
import com.thoughtworks.ivassistant.abilities.tts.TtsType
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpCallback
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpType
import com.thoughtworks.ivassistantapp.ui.theme.IvassistantandroidTheme
import com.thoughtworks.ivassistantapp.utils.MediaPlayerController
import com.thoughtworks.ivassistantapp.utils.MultiplePermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val ivAssistant = IVAssistant(this)
    private lateinit var tts: Tts
    private lateinit var asr: Asr
    private lateinit var wakeUp: WakeUp
    private lateinit var chat: Chat
    private val mediaPlayerController = MediaPlayerController(this)

    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()

        setContent {
            IvassistantandroidTheme {
                // A surface container using the 'background' color from the theme
                MultiplePermissions()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        wakeUp.stop()
        wakeUp.release()
        tts.release()
        asr.release()
        chat.release()
        ivAssistant.release()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initialize() {
        lifecycleScope.launch(Dispatchers.IO) {
            createTts()
            createAsr()
            createWakeUp()
            createChat()
        }
    }

    private fun createChat() {
        chat = ivAssistant.createChat(
            ChatType.ChatGpt,
            mapOf(
                Pair("base_url", "https://api.openai.com"),
                Pair("model", "gpt-3.5-turbo"),
                Pair("temperature", 1.0f),
                Pair("max_history_len", 20),
            )
        )
        chat.initialize()
    }

    private fun createWakeUp() {
//        wakeUp = ivAssistant.createWakeUp(
//            WakeUpType.Baidu,
//            mapOf(
//                Pair("kws-file", "assets:///WakeUp.bin"),
//                Pair("keywords", listOf("你好小智"))
//            )
//        )
        wakeUp = ivAssistant.createWakeUp(
            WakeUpType.Picovoice,
            mapOf(
                Pair(
                    "keyword_paths", listOf(
//                        "wakeup/picovoice/Hi-Joey_en_android_v2_2_0.ppn", // 注意这里的文件路径不要加 assets:/// 前缀
//                        "wakeup/picovoice/Hello-Joey_en_android_v2_2_0.ppn",
                        "wakeup/picovoice/丽芙丽芙_zh_android_v3_0_0.ppn",
                    )
                ),
                Pair("model_path", "wakeup/picovoice/models/porcupine_params_zh.pv"),
            )
        )
        wakeUp.initialize()
    }

    private fun createAsr() {
        //        asr = ivAssistant.createAsr(
//            AsrType.Ali,
//            mapOf(
//                Pair("enable_voice_detection", true),
//                Pair("max_start_silence", 10000),
//                Pair("max_end_silence", 800),
//            )
//        )
//        asr = ivAssistant.createAsr(AsrType.Baidu, mapOf())
        asr = ivAssistant.createAsr(AsrType.Iflytek, mapOf())
        asr.initialize()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createTts() {
        tts = ivAssistant.createTts(
            TtsType.Ali,
            mapOf(
                Pair("font_name", "aitong"),
                Pair("enable_subtitle", "1"),
                Pair("sample_rate", 16000),
                Pair("encode_type", "wav"),
                Pair("play_sound", true),
                Pair("tts_file_path", "${externalCacheDir?.absolutePath}/tts.wav"),
            ),
        )
//        tts = ivAssistant.createTts(
//            TtsType.Google,
//            mapOf(
//                Pair(
//                    "credentials", ByteArrayInputStream(
//                        """
//                            {{Google TTS credentials JSON string}}
//                        """.toByteArray()
//                    )
//                ),
//                Pair("language_code", "en-US"),
//                Pair("name", "en-US-Wavenet-F"),
//                Pair("speaking_rate", 1.0), // Speech speed. Default is 1.0. Range is 0.25 to 4.0.
//                Pair(
//                    "volume_gain_db",
//                    0.0
//                ), //Volume gain (in dB) of the normal native volume, supported by the specific voice, in the range [-96.0, 16.0]
//            )
//        )
        tts.initialize()
    }

    private fun playTts(coroutineScope: CoroutineScope, text: String) {
        coroutineScope.launch(Dispatchers.IO) {
            tts.play(text, mapOf(
                "font_name" to "zhimi_emo",
                "emotion" to "fear",
                "intensity" to 1.0f
            ), object : TtsCallback {
                override fun onPlayEnd() {
                    Log.d(TAG, "onPlayEnd")
                }

                override fun onPlayCancel() {
                    Log.d(TAG, "onPlayCancel")
                }

                override fun onTTSFileSaved(ttsFilePath: String) {
                    Log.d(TAG, "onTTSFileSaved: $ttsFilePath")
//                    mediaPlayerController.play(File(ttsFilePath))
                }
            })
        }
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        val composableScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                modifier = Modifier
                    .padding(top = 100.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .width(200.dp)
                    .wrapContentHeight(),
                onClick = {
                    playTts(
                        composableScope,
                        "你好，我是智能助理，我的名字叫小智，请问你找我有什么事吗?"
                    )
                }
            ) {
                Text(text = stringResource(id = R.string.tts))
            }
            Button(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .width(200.dp)
                    .wrapContentHeight(),
                onClick = {
//                    listenAndSay()
                    Log.d(TAG, "startListening")
                    asr.startListening(object : AsrCallback {
                        override fun onResult(text: String) {
                            Log.d(TAG, "asr onResult: $text")
                            if (text.isEmpty()) {
                                return
                            }
                            tts.play(text)
                        }

                        override fun onError(errorMessage: String) {
                            Log.e(TAG, "onError: $errorMessage")
                        }

                        override fun onVolumeChanged(volume: Float) {
                            Log.d(TAG, "onVolumeChanged: $volume")
                        }
                    })
                }
            ) {
                Text(text = stringResource(id = R.string.asr))
            }
            Button(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .width(200.dp)
                    .wrapContentHeight(),
                onClick = {
                    wakeUp.start(object : WakeUpCallback {
                        override fun onSuccess(keywordIndex: Int) {
                            Log.d(TAG, "wakeUp onSuccess $keywordIndex")
                            tts.play("我在", emptyMap(), object : TtsCallback {
                                override fun onPlayEnd() {
                                    asr.startListening(object : AsrCallback {
                                        override fun onResult(text: String) {
                                            Log.d(TAG, "onResult: $text")
                                            tts.play(text)
                                        }

                                        override fun onError(errorMessage: String) {
                                            Log.e(TAG, "onError: $errorMessage")
                                        }
                                    })
                                }

                                override fun onTTSFileSaved(ttsFilePath: String) {
                                    Log.d(TAG, "onTTSFileSaved: $ttsFilePath")
                                }

                                override fun onPlayError(errorMessage: String) {
                                    Log.e(TAG, "onPlayError: $errorMessage")
                                }

                                override fun onPlayCancel() {
                                    Log.w(TAG, "onPlayCancel")
                                }
                            })
                        }

                        override fun onError(errorCode: Int, errorMessage: String) {
                            Log.e(TAG, "errorCode: $errorCode, errorMessage: $errorMessage")
                        }

                        override fun onStop() {
                            Log.d(TAG, "wakeUp onStop")
                        }
                    })
                }
            ) {
                Text(text = stringResource(id = R.string.wake_up))
            }
            Button(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .width(200.dp)
                    .wrapContentHeight(),
                onClick = {
                    Log.d(TAG, "chat")
                    chat.chat(getString(R.string.hello), object : ChatCallback {
                        override fun onResult(text: String) {
                            tts.play(text)
                        }

                        override fun onError(errorMessage: String) {
                            Log.e(TAG, "chat onError: $errorMessage")
                        }
                    })
                }
            ) {
                Text(text = stringResource(id = R.string.chat))
            }
        }
    }

    fun listenAndSay() {
        SystemClock.sleep(800L)
        Log.d(TAG, "startListening")
        asr.startListening(object : AsrCallback {
            override fun onResult(text: String) {
                Log.d(TAG, "onResult: $text")
                if (text.isNotEmpty()) {
                    tts.play(text, emptyMap(), object : TtsCallback {
                        override fun onPlayEnd() {
                            Log.d(TAG, "onPlayEnd")
                            listenAndSay()
                        }

                        override fun onTTSFileSaved(ttsFilePath: String) {
                        }
                    })
                } else {
                    tts.play(getString(R.string.goodbye))
                }
            }

            override fun onError(errorMessage: String) {
                Log.e(TAG, "onError: $errorMessage")
            }
        })
    }
}