package com.thoughtworks.ivassistant.abilities.chat.chatgpt.model

import com.google.gson.annotations.SerializedName

data class ChatGptRequest(
    val model: String,
    val messages: List<GptMessage>,
    val temperature: Float,
    @SerializedName("max_tokens") val maxTokens: Int,
    val stream: Boolean = false
)

data class GptMessage(
    val role: String,
    val content: String
)
