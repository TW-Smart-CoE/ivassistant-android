package com.thoughtworks.ivassistant.abilities.asr.ali

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.alibaba.idst.nui.CommonUtils
import com.alibaba.nls.client.AccessToken
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.META_DATA_ACCESS_KEY
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.META_DATA_ACCESS_KEY_SECRET
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.META_DATA_APP_KEY
import com.thoughtworks.ivassistant.abilities.asr.ali.AliAsrConstant.TAG
import com.thoughtworks.ivassistant.abilities.tts.ali.AliTtsConstant.MILL_SECONDS
import com.thoughtworks.ivassistant.utils.SpUtils
import com.thoughtworks.ivassistant.utils.Utils.getDeviceId
import com.thoughtworks.ivassistant.utils.Utils.getManifestMetaData
import java.io.IOException

@SuppressLint("StaticFieldLeak")
class AliAsrInitializer {
    lateinit var context: Context
    var isInit = false
    var asrConfig = AliAsrConfig()
    var params = mapOf<String, Any>()

    fun init(context: Context, params: Map<String, Any>) {
        if (isInit) return

        this.params = params

        CommonUtils.copyAssetsData(context)
        this.context = context.applicationContext

        asrConfig = createConfig(context)
        isInit = true
    }

    private fun createConfig(context: Context): AliAsrConfig {
        val accessKey =
            params["access_key"]?.toString() ?: context.getManifestMetaData(META_DATA_ACCESS_KEY)
            ?: ""
        val accessKeySecret =
            params["access_key_secret"]?.toString() ?: context.getManifestMetaData(
                META_DATA_ACCESS_KEY_SECRET
            ) ?: ""
        val appKey =
            params["app_key"]?.toString() ?: context.getManifestMetaData(META_DATA_APP_KEY) ?: ""

        val deviceId = context.getDeviceId()
        val workspace = CommonUtils.getModelPath(context)
        val token = params["token"]?.toString() ?: getToken(accessKey, accessKeySecret)

        var debugPath = ""
        context.externalCacheDir?.absolutePath?.also {
            debugPath = "$it/debug_${System.currentTimeMillis()}"
        }

        return AliAsrConfig(
            accessKey = accessKey,
            accessKeySecret = accessKeySecret,
            appKey = appKey,
            deviceId = deviceId,
            workspace = workspace,
            debugPath = debugPath,
            token = token,
        )
    }

    private fun getToken(accessKey: String, accessKeySecret: String): String {
        try {
            val spUtils = SpUtils(context)
            val savedExpireTime = spUtils.getLong(SpUtils.SP_ALI_EXPIRE_TIME_KEY)
            if (savedExpireTime == 0L || savedExpireTime * MILL_SECONDS <= System.currentTimeMillis()) {
                val accessToken = AccessToken(accessKey, accessKeySecret)
                accessToken.apply()

                val expireTime = accessToken.expireTime
                val token = accessToken.token ?: ""

                if (token.isEmpty()) {
                    Log.e(TAG, "Get access token failed!")
                }

                spUtils.saveLong(SpUtils.SP_ALI_EXPIRE_TIME_KEY, expireTime)
                spUtils.saveStr(SpUtils.SP_ALI_ACCESS_TOKEN_KEY, token)

                return token
            } else {
                return spUtils.getStr(SpUtils.SP_ALI_ACCESS_TOKEN_KEY)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Get token failed!", e)
            return ""
        }
    }
}