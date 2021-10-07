package com.example.coinconverterspeech.presentation

import com.example.coinconverterspeech.data.model.ExchangeResponseValue

sealed class State {

    object Loading : State()

    data class Success(val list: List<ExchangeResponseValue>) : State()
    data class Error(val error: Throwable) : State()
}