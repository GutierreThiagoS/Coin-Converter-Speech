package com.example.coinconverterspeech.data.repository

import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.data.response.ExchangeResponseValue
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    suspend fun getExchangeValue(coins: String): Flow<ExchangeResponseValue>

    suspend fun save(exchange: ExchangeResponseValue)
    fun list(): Flow<List<ExchangeValue>>

    fun getListDeleted(): Flow<List<ExchangeValue>>

}