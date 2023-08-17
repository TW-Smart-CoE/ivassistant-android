package com.mxspace.ivassistant.abilities.tts

interface TtsCallback {
    fun onPlayEnd() {}
    fun onTTSFileSaved(ttsFilePath: String)
}

interface Tts {
    fun initialize()
    fun release()
    fun play(text: String, ttsCallback: TtsCallback? = null)
    fun stopPlay()
}