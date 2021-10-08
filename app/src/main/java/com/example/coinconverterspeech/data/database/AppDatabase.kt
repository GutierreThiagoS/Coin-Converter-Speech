package com.example.coinconverterspeech.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.coinconverterspeech.data.database.dao.ExchangeDao
import com.example.coinconverterspeech.data.model.ExchangeValue

@Database(entities = [ExchangeValue::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exchangeDao(): ExchangeDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "exchange_app_speech_db"
            ).build()
        }
    }
}