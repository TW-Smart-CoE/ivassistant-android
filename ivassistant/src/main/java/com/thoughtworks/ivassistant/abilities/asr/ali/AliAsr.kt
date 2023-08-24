package com.thoughtworks.ivassistant.abilities.asr.ali

import android.content.Context
import com.thoughtworks.ivassistant.abilities.asr.Asr
import com.thoughtworks.ivassistant.abilities.asr.AsrCallback
import java.util.concurrent.ExecutorService

class AliAsr(
    private val context: Context,
    private val params: Map<String, Any>,
    private val threadPool: ExecutorService,
) : Asr {
    private val aliTtsInitializer = AliAsrInitializer()
    private val aliAsrCreator = AliAsrCreator(context, params, aliTtsInitializer)

    override fun initialize() {
        threadPool.execute {
            aliTtsInitializer.init(context, params)
        }
    }

    override fun startListening(asrCallback: AsrCallback?) {
        aliAsrCreator.create(asrCallback)
    }

    override fun stopListening() {
        aliAsrCreator.stop()
    }

    override fun release() {
        aliAsrCreator.release()
    }
}