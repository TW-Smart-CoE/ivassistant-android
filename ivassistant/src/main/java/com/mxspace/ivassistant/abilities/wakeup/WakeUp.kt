package com.mxspace.ivassistant.abilities.wakeup

interface WakeUpCallback {
    fun onSuccess()
    fun onError(errorCode: Int, errorMessage: String)
    fun onStop()
}

interface WakeUp {
    fun initialize(wakeUpCallback: WakeUpCallback? = null)
    fun start()
    fun stop()
    fun release()
}