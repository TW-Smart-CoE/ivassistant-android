package com.mxspace.ivassistant.abilities.tts

interface TtsCallback {
    fun onPlayEnd()
}

interface Tts {
    fun initialize(ttsCallback: TtsCallback? = null)
    fun release()
    fun play(text: String)
    fun stopPlay()
}