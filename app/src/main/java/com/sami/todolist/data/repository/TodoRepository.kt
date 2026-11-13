package com.sami.todolist.data.repository

import com.sami.todolist.data.dao.TaskWithDetails
import com.sami.todolist.data.dao.TodoDao
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {

    val allTasksWithDetails: Flow<List<TaskWithDetails>> = todoDao.getAllTasksWithDetailsFlow()
    val pendingTasksWithDetails: Flow<List<TaskWithDetails>> = todoDao.getPendingTasksWithDetailsFlow()
    val completedTasksWithDetails: Flow<List<TaskWithDetails>> = todoDao.getCompletedTasksWithDetailsFlow()
    val allCategories: Flow<List<TaskCategoryEntity>> = todoDao.getAllCategoriesFlow()

    fun getTasksByCategory(categoryId: Long): Flow<List<TaskWithDetails>> {
        return todoDao.getTasksByCategoryFlow(categoryId)
    }

    suspend fun getTaskById(taskId: Long): TaskWithDetails? {
        return todoDao.getTaskWithDetailsById(taskId)
    }

    suspend fun saveTaskWithSubtasks(
        task: TaskEntity,
        subtasks: List<SubTaskEntity>
    ): Long {
        val taskId = todoDao.insertTask(task)
        todoDao.deleteSubtasksByTaskId(taskId)
        val linkedSubtasks = subtasks.map { it.copy(taskId = taskId) }
        todoDao.insertSubtasks(linkedSubtasks)
        return taskId
    }

    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        todoDao.updateTaskStatus(taskId, isCompleted, completedAt)
    }

    suspend fun toggleSubtaskCompletion(subtaskId: Long, isCompleted: Boolean) {
        todoDao.updateSubtaskStatus(subtaskId, isCompleted)
    }

    suspend fun deleteTask(taskId: Long) {
        todoDao.deleteTaskById(taskId)
    }

    suspend fun saveCategory(category: TaskCategoryEntity): Long {
        return todoDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: TaskCategoryEntity) {
        todoDao.deleteCategory(category)
    }
}
