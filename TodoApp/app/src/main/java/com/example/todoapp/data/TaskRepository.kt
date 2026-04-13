package com.example.todoapp.data

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun getTasks(
        statusFilter: TaskFilter,
        categoryFilter: TaskCategoryFilter
    ): List<Task> {
        val tasks = when (statusFilter) {
            TaskFilter.ALL -> taskDao.getAllTasks()
            TaskFilter.ACTIVE -> taskDao.getActiveTasks()
            TaskFilter.COMPLETED -> taskDao.getCompletedTasks()
        }
        return when (categoryFilter) {
            TaskCategoryFilter.ALL -> tasks
            TaskCategoryFilter.STUDY -> tasks.filter { it.category == "学习" }
            TaskCategoryFilter.LIFE -> tasks.filter { it.category == "生活" }
        }
    }

    suspend fun getSummary(): TaskSummary {
        val tasks = taskDao.getAllTasks()
        return TaskSummary(
            totalCount = tasks.size,
            completedCount = tasks.count { it.isCompleted },
            activeCount = tasks.count { !it.isCompleted }
        )
    }

    suspend fun addTask(title: String, content: String, category: String) {
        taskDao.insert(
            Task(
                title = title,
                content = content,
                category = category,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateTask(task: Task) = taskDao.update(task)

    suspend fun deleteTask(task: Task) = taskDao.delete(task)
}

enum class TaskFilter {
    ALL,
    ACTIVE,
    COMPLETED
}

enum class TaskCategoryFilter {
    ALL,
    STUDY,
    LIFE
}

data class TaskSummary(
    val totalCount: Int,
    val completedCount: Int,
    val activeCount: Int
)
