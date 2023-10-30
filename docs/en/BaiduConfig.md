# Baidu Intelligent Voice Interaction Services

## Backend Configuration

- Please head to [Baidu AI Wakeup](https://ai.baidu.com/tech/speech/wake). Set up the wake word and download the `WakeUp.bin` file. Place the `WakeUp.bin` file in the `_project/app/src/main/assets` directory (it should only be placed in the root directory of assets, do not create subdirectories, otherwise, the SDK will not be able to locate this file).
- Please go to the [Baidu AI Console](https://console.bce.baidu.com/ai/?_=1684837854400#/ai/speech/app/list) to create an application. Ensure that the package name matches the `applicationId` exactly. After creating the application, you will obtain `APP_ID`, `API_KEY`, and `SECRET_KEY`.

## SDK/API Key Configuration
In `AndroidManifest.xml`, configure under the `application` tag (it can also be configured in code):
```xml
<meta-data
    android:name="BAIDU_IVS_APP_ID"
    android:value="\${BAIDU_IVS_APP_ID}" />
<meta-data
    android:name="BAIDU_IVS_API_KEY"
    android:value="${BAIDU_IVS_API_KEY}" />
<meta-data
    android:name="BAIDU_IVS_SECRET_KEY"
    android:value="${BAIDU_IVS_SECRET_KEY}" />
```

## Precautions
- Dynamically request android.Manifest.permission.RECORD_AUDIO permission.
- Ensure minSdk <= 22, otherwise, you will receive the following error: com.baidu.speech.recognizerdemo I/WakeupEventAdapter: wakeup name:wp.error; params:{"error":11,"desc":"Wakeup engine model file invalid","sub_error":11005}
- If you need to call wakeup start as soon as the program starts, you may frequently receive errorCode: 3, errorMessage: Open Recorder failed error. It is advisable to set a delay time (3~5 seconds) before startup, and check for this error. If this error occurs, you can delay again before starting up.