# Baidu WakeUp

## Configure

[Baidu intelligent voice configure](docs/zh/BaiduConfig.md)

## 示例代码

```kotlin
// initialize wakeup
val wakeUp = ivAssistant.createWakeUp(
    WakeUpType.Baidu,
    mapOf(
        Pair("kws-file", "assets:///WakeUp.bin"),
        Pair("keywords", listOf("Hi Joey"))
    )
)
wakeUp.initialize()

// use wakeUp
wakeUp.start(object : WakeUpCallback {
    override fun onSuccess(keywordIndex: Int) {
        Log.d(TAG, "wakeUp onSuccess $keywordIndex")
    }

    override fun onError(errorCode: Int, errorMessage: String) {
        Log.e(TAG, "errorCode: $errorCode, errorMessage: $errorMessage")
    }

    override fun onStop() {
        Log.d(TAG, "wakeUp onStop")
    }
})

// stop & release wakeUp
wakeUp.stop()
wakeUp.release()

```