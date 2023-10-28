package com.thoughtworks.ivassistant.abilities.wakeup

interface WakeUpCallback {
    fun onSuccess(keywordIndex: Int) {}
    fun onError(errorCode: Int, errorMessage: String) {}
    fun onStop() {}
}

interface WakeUp {
    fun initialize()
    fun start(wakeUpCallback: WakeUpCallback? = null)
    fun stop()
    fun release()
}