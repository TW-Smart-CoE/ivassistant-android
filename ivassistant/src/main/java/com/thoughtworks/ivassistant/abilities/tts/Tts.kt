package com.thoughtworks.ivassistant.abilities.tts

interface TtsCallback {
    fun onPlayEnd() {}
    fun onPlayCancel() {}
    fun onTTSFileSaved(ttsFilePath: String) {}
}

interface Tts {
    fun initialize()
    fun release()
    fun play(text: String, params: Map<String, Any> = emptyMap(), ttsCallback: TtsCallback? = null)
    fun stopPlay()
}