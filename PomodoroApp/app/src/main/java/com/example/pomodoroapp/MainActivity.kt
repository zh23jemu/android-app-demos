package com.example.pomodoroapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
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
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isStudyMode = true
    private var studyDurationMinutes = DEFAULT_STUDY_MINUTES
    private var breakDurationMinutes = DEFAULT_BREAK_MINUTES
    private var remainingMillis = studyDurationMinutes * 60 * 1000L

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
        toneGenerator.release()
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
                applyCustomDurations()
                startTimer()
            }
        }
        binding.buttonPause.setOnClickListener { pauseTimer() }
        binding.buttonReset.setOnClickListener { resetTimer() }
        binding.buttonSkip.setOnClickListener { skipCurrentPhase() }
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
                    storage.saveStudySession(studyDurationMinutes)
                }
                showFinishNotification()
                playReminderTone()
                isStudyMode = !isStudyMode
                remainingMillis = currentPhaseDurationMs()
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
        applyCustomDurations()
        isStudyMode = true
        remainingMillis = currentPhaseDurationMs()
        updateTimerUi()
    }

    private fun skipCurrentPhase() {
        countDownTimer?.cancel()
        isRunning = false
        if (isStudyMode) {
            Toast.makeText(this, "已跳过当前专注周期", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "已跳过当前休息周期", Toast.LENGTH_SHORT).show()
        }
        isStudyMode = !isStudyMode
        remainingMillis = currentPhaseDurationMs()
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
        binding.editStudyMinutes.setText(studyDurationMinutes.toString())
        binding.editBreakMinutes.setText(breakDurationMinutes.toString())
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

    private fun playReminderTone() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
    }

    private fun applyCustomDurations() {
        val studyInput = binding.editStudyMinutes.text?.toString()?.trim().orEmpty()
        val breakInput = binding.editBreakMinutes.text?.toString()?.trim().orEmpty()
        val newStudy = studyInput.toIntOrNull()?.coerceIn(1, 180) ?: studyDurationMinutes
        val newBreak = breakInput.toIntOrNull()?.coerceIn(1, 60) ?: breakDurationMinutes
        val changed = newStudy != studyDurationMinutes || newBreak != breakDurationMinutes
        studyDurationMinutes = newStudy
        breakDurationMinutes = newBreak
        if (!isRunning && changed) {
            remainingMillis = currentPhaseDurationMs()
        }
    }

    private fun currentPhaseDurationMs(): Long {
        val minutes = if (isStudyMode) studyDurationMinutes else breakDurationMinutes
        return minutes * 60 * 1000L
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
        private const val DEFAULT_STUDY_MINUTES = 25
        private const val DEFAULT_BREAK_MINUTES = 5
        private const val CHANNEL_ID = "pomodoro_channel"
    }
}
