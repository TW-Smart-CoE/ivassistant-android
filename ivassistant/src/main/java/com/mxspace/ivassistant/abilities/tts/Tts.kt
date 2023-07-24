package com.mxspace.ivassistant.abilities.tts

interface TtsCallback {
    fun onPlayEnd() {}
}

interface Tts {
    fun initialize()
    fun release()
    fun play(text: String, ttsCallback: TtsCallback? = null)
    fun stopPlay()
}