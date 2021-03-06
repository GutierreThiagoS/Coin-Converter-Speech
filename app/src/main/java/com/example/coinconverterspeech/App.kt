package com.example.coinconverterspeech

import android.app.Application
import android.app.Presentation
import com.example.coinconverterspeech.data.di.DataModules
import com.example.coinconverterspeech.domain.di.DomainModule
import com.example.coinconverterspeech.presentation.di.PresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    companion object{
        lateinit var context: Application
    }

    override fun onCreate() {
        super.onCreate()

        context = this@App

        startKoin {
            androidContext(this@App)
        }

        DataModules.load()
        DomainModule.load()
        PresentationModule.load()

    }
}