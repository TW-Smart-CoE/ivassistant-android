package com.thoughtworks.ivassistant.abilities.chat

interface ChatCallback {
    fun onResult(text: String) {}
    fun onError(errorMessage: String) {}
}

interface Chat {
    fun initialize()
    fun chat(content: String, chatCallback: ChatCallback? = null)
    fun clearConversationHistory()
    fun release()
}