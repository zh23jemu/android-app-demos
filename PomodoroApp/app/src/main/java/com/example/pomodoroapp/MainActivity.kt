package com.example.pomodoroapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoroapp.data.SessionStorage
import com.example.pomodoroapp.databinding.ActivityMainBinding
import com.example.pomodoroapp.ui.HistoryAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: SessionStorage
    private lateinit var historyAdapter: HistoryAdapter

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isStudyMode = true
    private var remainingMillis = STUDY_DURATION_MS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = SessionStorage(this)
        createNotificationChannel()
        setupToolbar()
        setupRecyclerView()
        setupButtons()
        updateTimerUi()
        refreshStats()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }
    }

    private fun setupButtons() {
        binding.buttonStart.setOnClickListener {
            if (!isRunning) {
                startTimer()
            }
        }
        binding.buttonPause.setOnClickListener { pauseTimer() }
        binding.buttonReset.setOnClickListener { resetTimer() }
    }

    private fun startTimer() {
        isRunning = true
        updateStatusText()
        countDownTimer = object : CountDownTimer(remainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateTimerUi()
            }

            override fun onFinish() {
                isRunning = false
                if (isStudyMode) {
                    storage.saveStudySession(STUDY_MINUTES)
                }
                showFinishNotification()
                isStudyMode = !isStudyMode
                remainingMillis = if (isStudyMode) STUDY_DURATION_MS else BREAK_DURATION_MS
                updateTimerUi()
                refreshStats()
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        updateStatusText()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        isStudyMode = true
        remainingMillis = STUDY_DURATION_MS
        updateTimerUi()
    }

    private fun refreshStats() {
        val summaries = storage.getDailySummaries()
        historyAdapter.submitList(summaries)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todaySummary = summaries.firstOrNull { it.date == today }
        val totalCompleted = summaries.sumOf { it.completedCount }

        binding.textTodayCount.text = "${todaySummary?.completedCount ?: 0}"
        binding.textTodayMinutes.text = "${todaySummary?.totalMinutes ?: 0} 分钟"
        binding.textTotalCount.text = "$totalCompleted"
        binding.textHistoryEmpty.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateTimerUi() {
        val totalSeconds = remainingMillis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        binding.textTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        updateStatusText()
    }

    private fun updateStatusText() {
        binding.textMode.text = if (isStudyMode) "学习阶段" else "休息阶段"
        binding.textStatus.text = if (isRunning) "进行中" else "未开始/已暂停"
    }

    private fun showFinishNotification() {
        val title = if (isStudyMode) "学习结束" else "休息结束"
        val content = if (isStudyMode) "本次番茄钟已完成，开始休息吧" else "休息结束，可以继续专注学习"
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(this)
            .notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "番茄钟提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val STUDY_MINUTES = 25
        private const val BREAK_MINUTES = 5
        private const val STUDY_DURATION_MS = STUDY_MINUTES * 60 * 1000L
        private const val BREAK_DURATION_MS = BREAK_MINUTES * 60 * 1000L
        private const val CHANNEL_ID = "pomodoro_channel"
    }
}
