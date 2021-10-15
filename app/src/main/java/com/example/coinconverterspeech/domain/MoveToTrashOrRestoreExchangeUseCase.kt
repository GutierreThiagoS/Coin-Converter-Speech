package com.example.coinconverterspeech.domain

import com.example.coinconverterspeech.core.UseCase
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.data.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MoveToTrashOrRestoreExchangeUseCase(
    private val repository: CoinRepository
): UseCase.NoSource<ExchangeValue>() {
    override suspend fun execute(param: ExchangeValue): Flow<Unit> {
        return flow {
            repository.update(param)
            emit(Unit)
        }
    }
}