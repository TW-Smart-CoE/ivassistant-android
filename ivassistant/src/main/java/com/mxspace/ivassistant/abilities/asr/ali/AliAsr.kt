package com.mxspace.ivassistant.abilities.asr.ali

import android.content.Context
import com.mxspace.ivassistant.abilities.asr.Asr
import com.mxspace.ivassistant.abilities.asr.AsrCallback
import java.util.concurrent.ExecutorService

class AliAsr(
    private val context: Context,
    private val params: Map<String, Any>,
    private val threadPool: ExecutorService,
) : Asr {
    private val aliTtsInitializer = AliAsrInitializer()
    private val aliAsrCreator = AliAsrCreator(context, params, aliTtsInitializer)

    override fun initialize(asrCallback: AsrCallback?) {
        aliAsrCreator.asrCallback = asrCallback

        threadPool.execute {
            aliTtsInitializer.init(context, params)
        }
    }

    override fun startListening() {
        aliAsrCreator.create()
    }

    override fun stopListening() {
        aliAsrCreator.stop()
    }

    override fun release() {
        aliAsrCreator.release()
    }
}