package com.example.watertrackerapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watertrackerapp.data.WaterDatabase
import com.example.watertrackerapp.data.WaterRecord
import com.example.watertrackerapp.databinding.ActivityMainBinding
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
    private val waterDao by lazy { WaterDatabase.getInstance(this).waterDao() }
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter
    }

    private fun setupActions() {
        binding.buttonAddWater.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                waterDao.insert(
                    WaterRecord(
                        date = currentDate(),
                        amountMl = 250,
                        createdAt = System.currentTimeMillis()
                    )
                )
                loadDataOnMain()
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
        val todayTotal = waterDao.getTodayTotal(date)
        val todayCount = waterDao.getTodayRecordCount(date)
        val summaries = waterDao.getDailySummaries()
        withContext(Dispatchers.Main) {
            val progress = ((todayTotal.toFloat() / DAILY_GOAL_ML) * 100).toInt().coerceAtMost(100)
            binding.textTodayTotal.text = "$todayTotal ml"
            binding.textGoal.text = "目标 ${DAILY_GOAL_ML} ml"
            binding.textTodayCount.text = "今天已记录 $todayCount 次"
            binding.progressHydration.progress = progress
            binding.textStatus.text =
                if (todayTotal >= DAILY_GOAL_ML) "今日目标已达成" else "还差 ${DAILY_GOAL_ML - todayTotal} ml 达标"
            binding.textHistoryEmpty.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
            adapter.submitList(summaries)
        }
    }

    private fun currentDate(): String = dateFormatter.format(Date())

    companion object {
        private const val DAILY_GOAL_ML = 2000
    }
}
