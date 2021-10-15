package com.example.coinconverterspeech.presentation.historic

import com.example.coinconverterspeech.data.model.ExchangeValue

interface HistoricHandler {
    fun onClickMoveToTrash(item: ExchangeValue, position: Int)
    fun onClickSpeech(item: ExchangeValue)
}