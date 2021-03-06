package com.example.coinconverterspeech.presentation.coin_converter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinconverterspeech.data.Convert
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.data.response.ExchangeResponseValue
import com.example.coinconverterspeech.domain.GetExchangeValueUseCase
import com.example.coinconverterspeech.domain.SaveExchangeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CoinConverterViewModel(
    private val saveExchangeUseCase: SaveExchangeUseCase,
    private val getExchangeValueUseCase: GetExchangeValueUseCase
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    val speechSaved = MutableLiveData(false)

    fun getExchangeValue(coins: String) {
        viewModelScope.launch{
            getExchangeValueUseCase(coins)
                .flowOn(Dispatchers.Main)
                .onStart {
                    _state.value = State.Loading
                }
                .catch {
                    _state.value = State.Error(it)
                }
                .collect {
                    _state.value = State.Success(it)
                }
        }
    }

    fun saveExchange(exchange: ExchangeResponseValue) {
        viewModelScope.launch {
            saveExchangeUseCase(exchange)
                .flowOn(Dispatchers.Main)
                .onStart {
                    _state.value = State.Loading
                }
                .catch {
                    _state.value = State.Error(it)
                }
                .collect {
                    _state.value = State.Saved(Convert.convert(exchange))
                }
        }
    }

    sealed class State {
        object Loading: State()
        data class Saved(val exchange: ExchangeValue): State()

        data class Success(val exchange: ExchangeResponseValue): State()
        data class Error(val error: Throwable): State()
    }
}