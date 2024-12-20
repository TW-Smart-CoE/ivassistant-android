package com.thoughtworks.ivassistant.abilities.chat.openai

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.thoughtworks.ivassistant.abilities.chat.Chat
import com.thoughtworks.ivassistant.abilities.chat.ChatCallback
import com.thoughtworks.ivassistant.abilities.chat.openai.model.ChatGptRequest
import com.thoughtworks.ivassistant.abilities.chat.openai.model.ChatGptResponse
import com.thoughtworks.ivassistant.abilities.chat.openai.model.GptMessage
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAIChat(
    private val context: Context,
    private val params: Map<String, Any>,
) : Chat {
    interface ChatGptService {
        @POST("chat/completions")
        fun chat(@Body request: RequestBody): Call<ChatGptResponse>
    }

    private var baseUrl: String = (params["base_url"] ?: "https://api.openai.com/v1") as String
    private var maxHistoryLen: Int = (params["max_history_len"] ?: 50) as Int
    private var temperature: Float = (params["temperature"] ?: 1f) as Float
    private var model: String = (params["model"] ?: DEFAULT_MODEL) as String
    private var maxTokens: Int = (params["max_tokens"] ?: 2048) as Int
    private var readTimeout: Long = (params["read_timeout"] ?: 20_000L) as Long
    private var writeTimeout: Long = (params["write_timeout"] ?: 5_000L) as Long
    private var systemPromptList = mutableListOf<GptMessage>()
    private var conversionList = mutableListOf<GptMessage>()

    private val gson: Gson = GsonBuilder().create()

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
        .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
        .addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            builder.header(
                "Authorization",
                "Bearer ${
                    params["api_key"]?.toString() ?: context.getManifestMetaData(
                        META_OPENAI_API_KEY
                    )
                }"
            )
            builder.header("content-type", "application/json")
            return@addInterceptor chain.proceed(builder.build())
        }
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())

            if (!response.isSuccessful) {
                // Throw non-exception-typed error manually based on response code
                Log.e(TAG, "ERROR - code: ${response.code} with message: ${response.message}")
                when (response.code) {
                    INTERNAL_SERVER_ERROR -> throw InternalServerException(response.message)
                }
            }
            return@addInterceptor response
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val chatGptService = retrofit.create(ChatGptService::class.java)

    init {
        params["system_prompt"]?.let {
            if (it is List<*>) {
                systemPromptList = it.map { prompt ->
                    GptMessage(ROLE_SYSTEM, prompt as String)
                }.toMutableList()
            } else {
                systemPromptList.add(GptMessage(ROLE_SYSTEM, it as String))
            }
        }
    }

    override fun initialize() {
        params["base_url"]?.let {
            baseUrl = it as String
        }

        params["max_history_len"]?.let {
            maxHistoryLen = it as Int
        }

        params["temperature"]?.let {
            temperature = it as Float
        }

        params["model"]?.let {
            model = it as String
        }

        params["max_tokens"]?.let {
            maxTokens = it as Int
        }

        params["read_timeout"]?.let {
            readTimeout = it as Long
        }

        params["write_timeout"]?.let {
            writeTimeout = it as Long
        }

        params["system_prompt"]?.let {
            if (it is List<*>) {
                systemPromptList = it.map { prompt ->
                    GptMessage(ROLE_SYSTEM, prompt as String)
                }.toMutableList()
            } else {
                systemPromptList.clear()
                systemPromptList.add(GptMessage(ROLE_SYSTEM, it as String))
            }
        }
    }

    override fun chat(content: String, chatCallback: ChatCallback?) {
        val reqMessage = GptMessage(ROLE_USER, content)

        val messages = systemPromptList + conversionList + listOf(reqMessage)
        Log.d(TAG, "model: $model, temperature: $temperature, maxTokens: $maxTokens")
        Log.d(TAG, "messages: $messages")
        val chatRequest = ChatGptRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            maxTokens = maxTokens,
        )
        val requestBody = gson.toJson(chatRequest).toRequestBody()
        chatGptService
            .chat(requestBody)
            .enqueue(object : Callback<ChatGptResponse> {
                override fun onResponse(
                    call: Call<ChatGptResponse>,
                    response: Response<ChatGptResponse>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val chatResponse = response.body()
                            val resMessage = chatResponse!!.choices[0].message

                            if (conversionList.size > maxHistoryLen) {
                                conversionList.removeFirst()
                                conversionList.removeFirst()
                            }

                            conversionList.add(reqMessage)
                            conversionList.add(resMessage)

                            chatCallback?.onResult(resMessage.content)
                        } catch (e: Exception) {
                            e.message?.let { Log.e(TAG, it) }
                            chatCallback?.onError(e.message ?: "Unknown error")
                        }
                    } else {
                        Log.e(TAG, "onResponse: ${response.message()}")
                        chatCallback?.onError(response.message())
                    }
                }

                override fun onFailure(call: Call<ChatGptResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                    chatCallback?.onError(t.message ?: "Unknown error")
                }
            })
    }

    override fun clearConversationHistory() {
        conversionList.clear()
    }

    override fun release() {
        Log.d(TAG, "release")
    }

    internal class InternalServerException(message: String) : IOException(message)

    companion object {
        private const val TAG = "IV.OpenAIChat"
        private const val ROLE_SYSTEM = "system"
        private const val ROLE_USER = "user"
        private const val ROLE_ASSISTANT = "assistant"
        private const val DEFAULT_MODEL = "gpt-3.5-turbo"
        private const val META_OPENAI_API_KEY = "OPENAI_API_KEY"

        const val INTERNAL_SERVER_ERROR = 500
    }
}
