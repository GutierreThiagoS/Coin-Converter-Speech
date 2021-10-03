package com.example.coinconverterspeech.domain.di

import com.example.coinconverterspeech.domain.GetExchangeValueUseCase
import com.example.coinconverterspeech.domain.ListExchangeUseCase
import com.example.coinconverterspeech.domain.SaveExchangeUseCase
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object DomainModule {

    fun load() {
        loadKoinModules(useCaseModules())
    }

    private fun useCaseModules(): Module {
        return module {
            factory { ListExchangeUseCase(get()) }
            factory { SaveExchangeUseCase(get()) }
            factory { GetExchangeValueUseCase(get()) }
        }
    }
}