# ivassistant-assistant

## 介绍

ivassistant-android 是一个智能语音库中间件，它封装了多家云服务厂商提供的 ASR（自动语音识别）、TTS（文本到语音）、WakeUp（唤醒词识别）以及 ChatGPT 等智能语音交互服务 SDK。该中间件旨在为开发者提供一个简单便捷的接口，让他们能够轻松利用这些语音交互技术，而无需深入了解复杂的 SDK 集成和适配问题。借助 ivassistant-android，开发者可以更加专注于创建应用，而不会被底层技术的复杂性所困扰。

目前支持的能力：
- ASR 语音识别：阿里，百度，科大讯飞
- TTS 语音转文字：阿里（中文），Google（海外）
- WakeUp 语音唤醒：百度（中文），Picovoice（海外），科大讯飞
- Chat 智能聊天：ChatGpt

## 依赖配置

Add jitpack.io to your root build.gradle at the end of repositories:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
// build.gradle.kts
implementation("com.github.TW-Smart-CoE:ivassistant-android:$iv_assistant_version")
```

## 功能

## 语音合成
- [阿里 Tts](AliTts.md)
- [Google Tts](GoogleTts.md)

## 语音识别
- [阿里 Asr](AliAsr.md)
- [百度 Asr](BaiduAsr.md)
- [科大讯飞 Asr](IflytekAsr.md)

## 语音唤醒
- [百度 WakeUp](BaiduWakeUp.md)
- [Picovoice WakeUp](PicovoiceWakeUp.md)
- [科大讯飞 WakeUp](IflytekWakeUp.md)

## 智能聊天
- [OpenAI ChatGPT](ChatGPT.md)