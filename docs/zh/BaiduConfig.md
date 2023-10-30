# 百度智能语音交互服务

## 后台配置

- 请前往[百度AI唤醒](https://ai.baidu.com/tech/speech/wake)。 设置唤醒词并下载 WakeUp.bin 文件。 将 WakeUp.bin 文件放在 _project/app/src/main/assets_ 目录下（只能放到 assets 根目录下，不能创建子目录，否则 SDK 会找不到这个文件）。
- 请前往[百度AI控制台](https://console.bce.baidu.com/ai/?_=1684837854400#/ai/speech/app/list)创建一个应用程序。确保包名称与 applicationId 完全相同。创建应用程序后，您将获得 APP_ID, API_KEY 和 SECRET_KEY。

## SDK/API Key 配置
AndroidManifest.xml 中 application 标签下配置（也可以在代码中配置）：
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
这里注意，因为 BAIDU_IVS_APP_ID 是个 Int 类型的数字，所以需要在前面加一个 "\\" 符号，否则在代码中取出 value 时会因数据类型不对而报错。

## 注意事项
- 动态请求 android.Manifest.permission.RECORD_AUDIO 权限。
- 确保 minSdk <= 22，否则您会收到以下错误: com.baidu.speech.recognizerdemo I/WakeupEventAdapter: wakeup name:wp.error; params:{"error":11,"desc":"Wakeup engine model file invalid","sub_error":11005}
- 如果需要在程序一启动就调用 wakeup start，经常会收到 errorCode: 3, errorMessage: Open Recorder failed 错误。需要在启动前设置 delay 时间（3~5秒），并检查这错误。如果出现该错误，可以再次 delay 后再启动。
 