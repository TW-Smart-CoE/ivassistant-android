# ivassistant-assistant

## Introduction

ivassistant-android is an intelligent voice library middleware, encapsulating ASR (Automatic Speech Recognition), TTS (Text-to-Speech), WakeUp (Wake Word Detection), and ChatGPT smart voice interaction services SDK provided by various cloud service providers. This middleware aims to offer developers a simple and convenient interface, enabling them to effortlessly leverage these advanced voice interaction technologies without delving into the complexities of SDK integration and adaptation. With IVAssistant-Android, developers can focus more on crafting applications, unburdened by the intricacies of underlying technologies.

Currently supported capabilities:
- ASR Speech Recognition: Alibaba, Baidu, Iflytek
- TTS Text-to-Speech: Alibaba (Chinese), Google (International)
- WakeUp Voice Activation: Baidu (Chinese), Picovoice (International), Iflytek
- Chat Intelligent Chatting: ChatGPT

## How to integrate

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

## Features

## Voice Synthesis
- [Ali Tts](AliTts.md)
- [Google Tts](GoogleTts.md)

## Voice Recognition
- [Ali Asr](AliAsr.md)
- [Baidu Asr](BaiduAsr.md)
- [Iflytek Asr](IflytekAsr.md)

## Voice WakeUp
- [Baidu WakeUp](BaiduWakeUp.md)
- [Picovoice WakeUp](PicovoiceWakeUp.md)
- [Iflytek WakeUp](IflytekWakeUp.md)

## Intelligent Chatting
- [OpenAI ChatGPT](ChatGPT.md)
