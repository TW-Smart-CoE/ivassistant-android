package com.mxspace.ivassistant.abilities.asr

interface AsrCallback {
    fun onResult(text: String)
    fun onError(errorMessage: String)
    fun onVolumeChanged(volume: Float)
}

interface Asr {
    fun initialize(asrCallback: AsrCallback? = null)
    fun startListening()
    fun stopListening()
    fun release()
}