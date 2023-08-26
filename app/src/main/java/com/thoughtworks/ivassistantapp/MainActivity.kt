package com.thoughtworks.ivassistantapp

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.thoughtworks.ivassistant.IVAssistant
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import com.thoughtworks.ivassistant.abilities.asr.AsrType
import com.thoughtworks.ivassistant.abilities.tts.Tts
import com.thoughtworks.ivassistant.abilities.tts.TtsCallback
import com.thoughtworks.ivassistant.abilities.tts.TtsType
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUp
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpCallback
import com.thoughtworks.ivassistant.abilities.wakeup.WakeUpType
import com.thoughtworks.ivassistantapp.ui.theme.IvassistantandroidTheme
import com.thoughtworks.ivassistantapp.utils.MultiplePermissions

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val ivAssistant = IVAssistant(this)
    private lateinit var tts: Tts
    private lateinit var asr: Asr
    private lateinit var wakeUp: WakeUp

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
        ivAssistant.release()
        super.onDestroy()
    }

    private fun initialize() {
        createTts()
        createAsr()
        createWakeUp()
    }

    private fun createWakeUp() {
        wakeUp = ivAssistant.createWakeUp(
            WakeUpType.Baidu,
            mapOf(
                Pair("kws-file", "assets:///WakeUp.bin"),
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
        asr = ivAssistant.createAsr(AsrType.Baidu, mapOf())
        asr.initialize()
    }

    private fun createTts() {
        tts = ivAssistant.createTts(
            TtsType.Ali,
            mapOf(
                Pair("font_name", "aitong"),
                Pair("enable_subtitle", "1"),
                Pair("sample_rate", 16000),
                Pair("encode_type", "wav"),
                Pair("tts_file_path", "${externalCacheDir?.absolutePath}/tts.wav"),
            ),
        )
        tts.initialize()
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current

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
                    tts.play("你好，我是智能助理", object : TtsCallback {
                        override fun onPlayEnd() {
                            Log.d(TAG, "onPlayEnd")
                        }

                        override fun onTTSFileSaved(ttsFilePath: String) {
                            Log.d(TAG, "onTTSFileSaved: $ttsFilePath")
                        }
                    })
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
                        override fun onSuccess() {
                            Log.d(TAG, "wakeUp onSuccess")
                            tts.play("我在", object : TtsCallback {
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
        }
    }

    fun listenAndSay() {
        SystemClock.sleep(800L)
        Log.d(TAG, "startListening")
        asr.startListening(object : AsrCallback {
            override fun onResult(text: String) {
                Log.d(TAG, "onResult: $text")
                if (text.isNotEmpty()) {
                    tts.play(text, object : TtsCallback {
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