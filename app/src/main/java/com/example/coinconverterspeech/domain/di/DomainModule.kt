package com.example.coinconverterspeech.domain.di

import com.example.coinconverterspeech.domain.*
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
            factory { DeletedListExchangeUseCase(get()) }
            factory { DeleteExchangeUseCase(get()) }
            factory { MoveToTrashOrRestoreExchangeUseCase(get()) }
        }
    }
}