package com.example.pomodoroapp.data

data class SessionRecord(
    val timestamp: Long,
    val durationMinutes: Int
)

data class DailySummary(
    val date: String,
    val completedCount: Int,
    val totalMinutes: Int
)
