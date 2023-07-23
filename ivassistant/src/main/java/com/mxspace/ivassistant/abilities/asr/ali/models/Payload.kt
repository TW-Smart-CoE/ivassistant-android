package com.mxspace.ivassistant.abilities.asr.ali.models

import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("result")
    var result: String = "",
    @SerializedName("duration")
    var duration: Int = 0,
)