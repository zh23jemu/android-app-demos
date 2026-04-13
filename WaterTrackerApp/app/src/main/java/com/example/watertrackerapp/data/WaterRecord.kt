package com.example.watertrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val amountMl: Int,
    val createdAt: Long
)
