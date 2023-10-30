# 百度 Asr

## 配置

[百度智能语音配置](BaiduConfig.md)

## 示例代码

```kotlin
// initialize asr
val asr = ivAssistant.createAsr(AsrType.Baidu, mapOf())
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