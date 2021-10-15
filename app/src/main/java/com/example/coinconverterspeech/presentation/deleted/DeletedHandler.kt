package com.example.coinconverterspeech.presentation.deleted

import com.example.coinconverterspeech.data.model.ExchangeValue

interface DeletedHandler {

    fun onClickRestore(item: ExchangeValue)

    fun onCLickRemove(item: ExchangeValue)
}