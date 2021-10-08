package com.example.coinconverterspeech.presentation.deleted

import androidx.lifecycle.*
import com.example.coinconverterspeech.data.model.ExchangeValue
import com.example.coinconverterspeech.domain.DeletedListExchangeUseCase
import com.example.coinconverterspeech.presentation.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class DeletedViewModel(
    private val deletedListExchangeUseCase: DeletedListExchangeUseCase
): ViewModel(), LifecycleObserver {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun getExchanges() {
        viewModelScope.launch {
            deletedListExchangeUseCase()
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

    fun deletedPermanet(item: ExchangeValue){

    }

    fun restorePermanet(item: ExchangeValue){

    }
}