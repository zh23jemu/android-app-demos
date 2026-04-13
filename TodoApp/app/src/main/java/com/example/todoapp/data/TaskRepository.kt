package com.example.todoapp.data

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun getTasks(filter: TaskFilter): List<Task> {
        return when (filter) {
            TaskFilter.ALL -> taskDao.getAllTasks()
            TaskFilter.ACTIVE -> taskDao.getActiveTasks()
            TaskFilter.COMPLETED -> taskDao.getCompletedTasks()
        }
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
