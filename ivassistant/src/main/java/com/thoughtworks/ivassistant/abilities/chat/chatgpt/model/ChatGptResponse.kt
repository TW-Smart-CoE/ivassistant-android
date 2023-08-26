package com.thoughtworks.ivassistant.abilities.chat.chatgpt.model

import com.google.gson.annotations.SerializedName

data class ChatGptResponse(
    val id: String,
    @SerializedName("object") val objectType: String,
    val created: Long,
    val choices: List<ChatCompletionChoice>,
    val usage: ChatCompletionUsage,
    val model: String
)

data class ChatCompletionChoice(
    val index: Int,
    val message: GptMessage,
    @SerializedName("finish_reason") val finishReason: String
)

data class ChatCompletionUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)
