package com.example.coinconverterspeech.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coinconverterspeech.data.model.ExchangeValue
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeDao {

    @Query("SELECT * FROM tb_exchange WHERE deleted = 0")
    fun findAll(): Flow<List<ExchangeValue>>

    @Query("SELECT * FROM tb_exchange WHERE deleted = 1")
    fun findAllDeleted(): Flow<List<ExchangeValue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: ExchangeValue)
}