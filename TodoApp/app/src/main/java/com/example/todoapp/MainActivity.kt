package com.example.todoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.data.TaskFilter
import com.example.todoapp.data.TaskCategoryFilter
import com.example.todoapp.data.TaskRepository
import com.example.todoapp.data.TodoDatabase
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.databinding.DialogAddTaskBinding
import com.example.todoapp.ui.TaskAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: TaskRepository
    private lateinit var taskAdapter: TaskAdapter
    private var currentFilter: TaskFilter = TaskFilter.ALL
    private var currentCategoryFilter: TaskCategoryFilter = TaskCategoryFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TaskRepository(TodoDatabase.getInstance(this).taskDao())
        setupToolbar()
        setupFilterSpinners()
        setupRecyclerView()
        setupActions()
        loadTasks()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupFilterSpinners() {
        val statusLabels = listOf("全部任务", "未完成", "已完成")
        binding.spinnerFilter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusLabels
        )
        binding.spinnerFilter.setSelection(0)

        val categoryLabels = listOf("全部分类", "学习", "生活")
        binding.spinnerCategoryFilter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categoryLabels
        )
        binding.spinnerCategoryFilter.setSelection(0)

        binding.buttonApplyFilter.setOnClickListener {
            currentFilter = when (binding.spinnerFilter.selectedItemPosition) {
                1 -> TaskFilter.ACTIVE
                2 -> TaskFilter.COMPLETED
                else -> TaskFilter.ALL
            }
            currentCategoryFilter = when (binding.spinnerCategoryFilter.selectedItemPosition) {
                1 -> TaskCategoryFilter.STUDY
                2 -> TaskCategoryFilter.LIFE
                else -> TaskCategoryFilter.ALL
            }
            loadTasks()
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onCheckedChanged = { task, checked ->
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.updateTask(task.copy(isCompleted = checked))
                    loadTasksOnMain()
                }
            },
            onEditClicked = { task ->
                showTaskDialog(task)
            },
            onDeleteClicked = { task ->
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.deleteTask(task)
                    loadTasksOnMain()
                }
            }
        )
        binding.recyclerTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskAdapter
        }
    }

    private fun setupActions() {
        binding.fabAddTask.setOnClickListener {
            showTaskDialog()
        }
    }

    private fun showTaskDialog(task: com.example.todoapp.data.Task? = null) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        val categories = listOf("学习", "生活")
        dialogBinding.spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        if (task != null) {
            dialogBinding.editTitle.setText(task.title)
            dialogBinding.editContent.setText(task.content)
            val categoryIndex = categories.indexOf(task.category).coerceAtLeast(0)
            dialogBinding.spinnerCategory.setSelection(categoryIndex)
        }

        AlertDialog.Builder(this)
            .setTitle(if (task == null) "新增任务" else "编辑任务")
            .setView(dialogBinding.root)
            .setNegativeButton("取消", null)
            .setPositiveButton("保存") { _, _ ->
                val title = dialogBinding.editTitle.text.toString().trim()
                val content = dialogBinding.editContent.text.toString().trim()
                val category = dialogBinding.spinnerCategory.selectedItem.toString()
                if (title.isBlank()) {
                    Toast.makeText(this, "请输入任务标题", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    if (task == null) {
                        repository.addTask(title, content, category)
                    } else {
                        repository.updateTask(
                            task.copy(
                                title = title,
                                content = content,
                                category = category
                            )
                        )
                    }
                    loadTasksOnMain()
                }
            }
            .show()
    }

    private fun loadTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tasks = repository.getTasks(currentFilter, currentCategoryFilter)
            val summary = repository.getSummary()
            withContext(Dispatchers.Main) {
                taskAdapter.submitList(tasks)
                binding.textEmpty.visibility = if (tasks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                updateSummary(summary)
            }
        }
    }

    private suspend fun loadTasksOnMain() {
        val tasks = repository.getTasks(currentFilter, currentCategoryFilter)
        val summary = repository.getSummary()
        withContext(Dispatchers.Main) {
            taskAdapter.submitList(tasks)
            binding.textEmpty.visibility = if (tasks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            updateSummary(summary)
        }
    }

    private fun updateSummary(summary: com.example.todoapp.data.TaskSummary) {
        binding.textTotalCount.text = "${summary.totalCount}"
        binding.textCompletedCount.text = "${summary.completedCount}"
        binding.textActiveCount.text = "${summary.activeCount}"
    }
}
