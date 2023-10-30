# 阿里 Asr

## 配置

[阿里智能语音配置](docs/zh/AliConfig.md)

## 示例代码

```kotlin
// initialize asr
val ivAssistant = IVAssistant(this)
asr = ivAssistant.createAsr(
    AsrType.Ali,
    mapOf(
        Pair("enable_voice_detection", true),
        Pair("max_start_silence", 10000),
        Pair("max_end_silence", 800),
    )
)
asr.initialize()

// use asr
asr.startListening(object : AsrCallback {
    override fun onResult(text: String) {
        Log.d(TAG, "asr onResult: $text")
        if (text.isEmpty()) {
            return
        }
        tts.play(text)
    }

    override fun onError(errorMessage: String) {
        Log.e(TAG, "onError: $errorMessage")
    }

    override fun onVolumeChanged(volume: Float) {
        Log.d(TAG, "onVolumeChanged: $volume")
    }
})


// release asr
asr.release()
```