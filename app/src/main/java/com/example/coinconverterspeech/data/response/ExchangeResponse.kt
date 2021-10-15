package com.example.coinconverterspeech.data.response

typealias ExchangeResponse = HashMap<String, ExchangeResponseValue>


data class ExchangeResponseValue(
    var id: Long,
    val code: String,
    val codein: String,
    val name: String,
    val bid: Double,
    val ask: Double,
    val create_date: String,
    val coinToConverter: Double,
)