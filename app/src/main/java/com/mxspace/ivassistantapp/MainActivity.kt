package com.mxspace.ivassistantapp

import android.os.Bundle
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
import com.mxspace.ivassistant.IVAssistant
import com.mxspace.ivassistant.abilities.tts.Tts
import com.mxspace.ivassistant.abilities.tts.TtsCallback
import com.mxspace.ivassistant.abilities.tts.TtsType
import com.mxspace.ivassistantapp.ui.theme.IvassistantandroidTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val ivAssistant = IVAssistant(this)
    private lateinit var tts: Tts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()

        setContent {
            IvassistantandroidTheme {
                // A surface container using the 'background' color from the theme
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
        tts.release()
        super.onDestroy()
    }

    private fun initialize() {
        tts = ivAssistant.createTts(
            TtsType.Ali,
            mapOf(
                Pair("font_name", "siqi"),
                Pair("enable_subtitle", "1"),
                Pair("sample_rate", 16000),
                Pair("encode_type", "pcm"),
            ),
        )
        tts.initialize(object : TtsCallback {
            override fun onPlayEnd() {
                Log.d(TAG, "onPlayEnd")
            }
        })
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
                    tts.play("你好，我是智能助理")
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
                }
            ) {
                Text(text = stringResource(id = R.string.wake_up))
            }
        }
    }
}