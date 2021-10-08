package com.example.coinconverterspeech.domain

import com.example.coinconverterspeech.core.UseCase
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.data.repository.CoinRepository
import kotlinx.coroutines.flow.Flow

class DeletedListExchangeUseCase(
    private val repository: CoinRepository
) : UseCase.NoParam<List<ExchangeValue>>() {

    override suspend fun execute(): Flow<List<ExchangeValue>> {
        return repository.getListDeleted()
    }

}