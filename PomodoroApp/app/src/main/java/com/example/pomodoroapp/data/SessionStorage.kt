package com.example.pomodoroapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionStorage(context: Context) {

    private val prefs = context.getSharedPreferences("pomodoro_sessions", Context.MODE_PRIVATE)
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun saveStudySession(durationMinutes: Int) {
        val records = JSONArray(prefs.getString(KEY_SESSIONS, "[]"))
        val item = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("durationMinutes", durationMinutes)
        }
        records.put(item)
        prefs.edit().putString(KEY_SESSIONS, records.toString()).apply()
    }

    fun getDailySummaries(): List<DailySummary> {
        val records = JSONArray(prefs.getString(KEY_SESSIONS, "[]"))
        val grouped = linkedMapOf<String, MutableList<SessionRecord>>()
        for (index in 0 until records.length()) {
            val item = records.getJSONObject(index)
            val record = SessionRecord(
                timestamp = item.getLong("timestamp"),
                durationMinutes = item.getInt("durationMinutes")
            )
            val date = formatter.format(Date(record.timestamp))
            grouped.getOrPut(date) { mutableListOf() }.add(record)
        }
        return grouped.map { (date, list) ->
            DailySummary(
                date = date,
                completedCount = list.size,
                totalMinutes = list.sumOf { it.durationMinutes }
            )
        }.sortedByDescending { it.date }
    }

    companion object {
        private const val KEY_SESSIONS = "sessions"
    }
}
