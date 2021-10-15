package com.example.coinconverterspeech.data.repository

import com.example.coinconverterspeech.core.exceptions.RemoteException
import com.example.coinconverterspeech.data.Convert
import com.example.coinconverterspeech.data.database.AppDatabase
import com.example.coinconverterspeech.data.model.ErrorResponse
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.data.response.ExchangeResponseValue
import com.example.coinconverterspeech.data.services.AwesomeService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class CoinRepositoryImpl(
    appDatabase: AppDatabase,
    private val service: AwesomeService
) : CoinRepository {

    private val dao = appDatabase.exchangeDao()

    override suspend fun getExchangeValue(coins: String) = flow {
        try {
            val exchangeValue = service.exchangeValue(coins)
            val exchange = exchangeValue.values.first()
            emit(exchange)
        } catch (e: HttpException) {
            // {"status":404,"code":"CoinNotExists","message":"moeda nao encontrada USD-USD"}
            val json = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(json, ErrorResponse::class.java)
            throw RemoteException(errorResponse.message)
        }
    }

    override suspend fun save(exchange: ExchangeResponseValue) {
        dao.save(Convert.convert(exchange))
    }

    override fun list(): Flow<List<ExchangeValue>> {
        return dao.findAll()
    }

    override fun getListDeleted(): Flow<List<ExchangeValue>> {
        return dao.findAllDeleted()
    }

    override suspend fun update(exchange: ExchangeValue) {
        dao.update(exchange)
    }

    override suspend fun deletedPermanently(exchange: ExchangeValue) {
        dao.delete(exchange)
    }

}