package com.mxspace.ivassistant.abilities.asr.ali.models

import com.google.gson.annotations.SerializedName

data class AliASRResult(
    @SerializedName("header")
    var header: Header? = null,
    @SerializedName("payload")
    var payload: Payload? = null,
)