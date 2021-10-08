package com.example.coinconverterspeech.data

import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.data.response.ExchangeResponseValue

object Convert {

    fun convert(exchangeResponseValue: ExchangeResponseValue): ExchangeValue{
        with(exchangeResponseValue){
            return ExchangeValue(
                id = id,
                code = code,
                codein = codein,
                name = name,
                bid = bid,
                deleted = false
            )
        }
    }
}