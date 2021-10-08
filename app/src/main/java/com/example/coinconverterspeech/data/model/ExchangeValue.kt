package com.example.coinconverterspeech.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_exchange")
data class ExchangeValue(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo val code: String,
    @ColumnInfo val codein: String,
    @ColumnInfo val name: String,
    @ColumnInfo val bid: Double,
    @ColumnInfo val deleted: Boolean
)
