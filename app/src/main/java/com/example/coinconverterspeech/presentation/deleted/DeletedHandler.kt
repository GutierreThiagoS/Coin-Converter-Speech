package com.example.coinconverterspeech.presentation.deleted

import com.example.coinconverterspeech.data.model.ExchangeResponseValue

interface DeletedHandler {

    fun onClickRestore(item: ExchangeResponseValue)

    fun onCLickRemove(item: ExchangeResponseValue)
}