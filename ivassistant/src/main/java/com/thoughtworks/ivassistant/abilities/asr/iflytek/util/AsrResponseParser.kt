package com.thoughtworks.ivassistant.abilities.asr.iflytek.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// 定义数据类
data class ASRResponse(
    @SerializedName("sn") val sn: Int,
    @SerializedName("ls") val ls: Boolean,
    @SerializedName("bg") val bg: Int,
    @SerializedName("ed") val ed: Int,
    @SerializedName("ws") val ws: List<Word>
)

data class Word(
    @SerializedName("bg") val bg: Int,
    @SerializedName("cw") val cw: List<ChineseWord>
)

data class ChineseWord(
    @SerializedName("sc") val sc: Double,
    @SerializedName("w") val w: String
)

// 解析Gson字符串并返回识别出的中文
fun parseAsrResponse(jsonString: String): String {
    // 创建Gson实例
    val gson = Gson()

    // 解析JSON字符串到数据类
    val asrResponse = gson.fromJson(jsonString, ASRResponse::class.java)

    // 用于保存解析出的结果
    val stringBuilder = StringBuilder()

    // 遍历ws数组，并将cw中sc最高的"w"字段合并到结果中
    asrResponse.ws.forEach { word ->
        var bestScore = Double.NEGATIVE_INFINITY
        var bestWord: String? = null

        // 遍历cw数组的每个对象，挑选sc最高的分词
        word.cw.forEach { chineseWord ->
            if (chineseWord.sc > bestScore) {
                bestScore = chineseWord.sc
                bestWord = chineseWord.w
            }
        }

        bestWord?.let { stringBuilder.append(it) }
    }

    // 返回识别出的中文结果
    return stringBuilder.toString()
}