# 阿里 Tts

## 配置

[阿里智能语音配置](AliConfig.md)

## 示例代码

```kotlin
// initialize tts
val ivAssistant = IVAssistant(this)
val tts = ivAssistant.createTts(
    TtsType.Ali,
    mapOf(
        Pair("font_name", "aitong"),
        Pair("enable_subtitle", "1"),
        Pair("sample_rate", 16000),
        Pair("encode_type", "wav"),
        Pair("tts_file_path", "${externalCacheDir?.absolutePath}/tts.wav"),
    ),
)
tts.initialize()

// use tts
tts.play(text, object : TtsCallback {
    override fun onPlayEnd() {
        Log.d(TAG, "onPlayEnd")
    }

    override fun onTTSFileSaved(ttsFilePath: String) {
        Log.d(TAG, "onTTSFileSaved: $ttsFilePath")
    }
})

// release tts
tts.release()
```