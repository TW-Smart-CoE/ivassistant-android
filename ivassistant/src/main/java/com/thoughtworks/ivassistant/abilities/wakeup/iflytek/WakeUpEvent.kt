package com.thoughtworks.ivassistant.abilities.wakeup.iflytek

data class WakeUpEvent(
    val sst: String,
    val id: Int,
    val score: Int,
    val bos: Int,
    val eos: Int,
    val keyword: String
)