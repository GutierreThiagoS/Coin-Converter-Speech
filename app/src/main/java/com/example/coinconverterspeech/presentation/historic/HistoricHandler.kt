package com.example.coinconverterspeech.presentation.historic

import com.example.coinconverterspeech.data.model.ExchangeValue

interface HistoricHandler {
    fun onClickDelete(item: ExchangeValue, position: Int)
}