package com.example.coinconverterspeech.domain

import com.example.coinconverterspeech.core.UseCase
import com.example.coinconverterspeech.data.repository.CoinRepository
import com.example.coinconverterspeech.data.response.ExchangeResponseValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SaveExchangeUseCase(
    private val repository: CoinRepository
) : UseCase.NoSource<ExchangeResponseValue>() {

    override suspend fun execute(param: ExchangeResponseValue): Flow<Unit> {
        return flow {
            repository.save(param)
            emit(Unit)
        }
    }
}