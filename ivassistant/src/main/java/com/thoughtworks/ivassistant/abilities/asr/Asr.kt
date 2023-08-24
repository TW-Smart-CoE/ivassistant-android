package com.thoughtworks.ivassistant.abilities.asr

interface AsrCallback {
    fun onResult(text: String) {}
    fun onError(errorMessage: String) {}
    fun onVolumeChanged(volume: Float) {}
}

interface Asr {
    fun initialize()
    fun startListening(asrCallback: AsrCallback? = null)
    fun stopListening()
    fun release()
}