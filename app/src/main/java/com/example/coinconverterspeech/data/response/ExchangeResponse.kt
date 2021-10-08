package com.example.coinconverterspeech.data.response

typealias ExchangeResponse = HashMap<String, ExchangeResponseValue>


data class ExchangeResponseValue(
    var id: Long,
    val code: String,
    val codein: String,
    val name: String,
    val bid: Double,
)