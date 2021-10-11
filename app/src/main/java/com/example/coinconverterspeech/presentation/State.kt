package com.example.coinconverterspeech.presentation

import com.example.coinconverterspeech.data.model.ExchangeValue

sealed class State {

    object Loading : State()
    object Saved: State()

    data class Success(val list: List<ExchangeValue>) : State()
    data class Error(val error: Throwable) : State()
}