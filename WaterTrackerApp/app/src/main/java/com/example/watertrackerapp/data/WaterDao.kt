package com.example.watertrackerapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WaterDao {
    @Insert
    suspend fun insert(record: WaterRecord)

    @Query(
        """
        SELECT date, SUM(amountMl) AS totalMl, COUNT(*) AS recordCount
        FROM water_records
        GROUP BY date
        ORDER BY date DESC
        """
    )
    suspend fun getDailySummaries(): List<DailyWaterSummary>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_records WHERE date = :date")
    suspend fun getTodayTotal(date: String): Int

    @Query("SELECT COUNT(*) FROM water_records WHERE date = :date")
    suspend fun getTodayRecordCount(date: String): Int

    @Query("SELECT * FROM water_records WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getTodayRecords(date: String): List<WaterRecord>
}
