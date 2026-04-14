package com.example.watertrackerapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watertrackerapp.data.WaterDatabase
import com.example.watertrackerapp.data.WaterRecord
import com.example.watertrackerapp.databinding.ActivityMainBinding
import com.example.watertrackerapp.ui.TodayRecordAdapter
import com.example.watertrackerapp.ui.WaterSummaryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WaterSummaryAdapter
    private lateinit var todayRecordAdapter: TodayRecordAdapter
    private val waterDao by lazy { WaterDatabase.getInstance(this).waterDao() }
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val prefs by lazy { getSharedPreferences("water_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupActions()
        refreshData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        adapter = WaterSummaryAdapter()
        todayRecordAdapter = TodayRecordAdapter()
        binding.recyclerTodayRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerTodayRecords.adapter = todayRecordAdapter
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter
    }

    private fun setupActions() {
        binding.buttonAdd250.setOnClickListener { addWaterRecord(250) }
        binding.buttonAdd500.setOnClickListener { addWaterRecord(500) }
        binding.buttonAddCustom.setOnClickListener {
            val amount = binding.editCustomAmount.text?.toString()?.trim().orEmpty().toIntOrNull()
            if (amount == null || amount <= 0) {
                binding.editCustomAmount.error = "请输入有效毫升数"
            } else {
                binding.editCustomAmount.error = null
                addWaterRecord(amount)
            }
        }
        binding.buttonSaveGoal.setOnClickListener {
            val goal = binding.editDailyGoal.text?.toString()?.trim().orEmpty().toIntOrNull()
            if (goal == null || goal < 500) {
                binding.editDailyGoal.error = "目标建议不少于 500ml"
            } else {
                binding.editDailyGoal.error = null
                prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply()
                refreshData()
            }
        }
    }

    private fun refreshData() {
        lifecycleScope.launch(Dispatchers.IO) {
            loadDataOnMain()
        }
    }

    private suspend fun loadDataOnMain() {
        val date = currentDate()
        val goal = currentGoal()
        val todayTotal = waterDao.getTodayTotal(date)
        val todayCount = waterDao.getTodayRecordCount(date)
        val todayRecords = waterDao.getTodayRecords(date)
        val summaries = waterDao.getDailySummaries()
        withContext(Dispatchers.Main) {
            val progress = ((todayTotal.toFloat() / goal) * 100).toInt().coerceAtMost(100)
            binding.textTodayTotal.text = "$todayTotal ml"
            binding.textGoal.text = "目标 ${goal} ml"
            binding.textTodayCount.text = "今天已记录 $todayCount 次"
            binding.editDailyGoal.setText(goal.toString())
            binding.progressHydration.progress = progress
            binding.textStatus.text =
                if (todayTotal >= goal) "今日目标已达成" else "还差 ${goal - todayTotal} ml 达标"
            binding.textLastRecord.text =
                if (todayRecords.isEmpty()) "今日还没有饮水记录" else "最近一次：${formatTime(todayRecords.first().createdAt)}"
            binding.textTodayEmpty.visibility = if (todayRecords.isEmpty()) View.VISIBLE else View.GONE
            binding.textHistoryEmpty.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
            todayRecordAdapter.submitList(todayRecords)
            adapter.submitList(summaries)
            maybePlayGoalCelebration(date, todayTotal, goal)
        }
    }

    private fun addWaterRecord(amountMl: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            waterDao.insert(
                WaterRecord(
                    date = currentDate(),
                    amountMl = amountMl,
                    createdAt = System.currentTimeMillis()
                )
            )
            loadDataOnMain()
        }
    }

    private fun currentDate(): String = dateFormatter.format(Date())

    private fun currentGoal(): Int = prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL_ML)

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    private fun maybePlayGoalCelebration(date: String, todayTotal: Int, goal: Int) {
        val celebratedDate = prefs.getString(KEY_LAST_CELEBRATION_DATE, null)
        if (todayTotal >= goal && celebratedDate != date) {
            prefs.edit().putString(KEY_LAST_CELEBRATION_DATE, date).apply()
            binding.goalCelebrationView.play()
        }
    }

    companion object {
        private const val DEFAULT_DAILY_GOAL_ML = 2000
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_LAST_CELEBRATION_DATE = "last_celebration_date"
    }
}
