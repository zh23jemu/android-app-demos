package com.example.todoapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.data.Task
import com.example.todoapp.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onCheckedChanged: (Task, Boolean) -> Unit,
    private val onDeleteClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val items = mutableListOf<Task>()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun submitList(tasks: List<Task>) {
        items.clear()
        items.addAll(tasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.checkboxCompleted.setOnCheckedChangeListener(null)
            binding.checkboxCompleted.text = task.title
            binding.checkboxCompleted.isChecked = task.isCompleted
            binding.textContent.text =
                if (task.content.isBlank()) "无详细内容" else task.content
            binding.textMeta.text =
                "${task.category} | ${formatter.format(Date(task.createdAt))}"
            binding.checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChanged(task, isChecked)
            }
            binding.buttonDelete.setOnClickListener {
                onDeleteClicked(task)
            }
        }
    }
}
