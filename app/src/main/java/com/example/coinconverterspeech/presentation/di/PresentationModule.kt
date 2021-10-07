package com.example.coinconverterspeech.presentation.di

import com.example.coinconverterspeech.presentation.historic.HistoryViewModel
import com.example.coinconverterspeech.presentation.coin_converter.CoinConverterViewModel
import com.example.coinconverterspeech.presentation.deleted.DeletedViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object PresentationModule {

    fun load() {
        loadKoinModules(viewModelModules())
    }

    private fun viewModelModules(): Module {
        return module {
            viewModel { HistoryViewModel(get()) }
            viewModel { CoinConverterViewModel(get(), get()) }
            viewModel { DeletedViewModel(get()) }
        }
    }
}