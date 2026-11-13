package com.sami.todolist.data.dao

import androidx.room.*
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: TaskCategoryEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubTaskEntity>
)

@Dao
interface TodoDao {

    // --- Task Queries ---
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDate ASC, priority DESC, createdAt DESC")
    fun getAllTasksWithDetailsFlow(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC, priority DESC, createdAt DESC")
    fun getPendingTasksWithDetailsFlow(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC, createdAt DESC")
    fun getCompletedTasksWithDetailsFlow(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY isCompleted ASC, dueDate ASC")
    fun getTasksByCategoryFlow(categoryId: Long): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskWithDetailsById(taskId: Long): TaskWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean, completedAt: Long?)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    // --- Subtasks ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubTaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubTaskEntity)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subtaskId")
    suspend fun updateSubtaskStatus(subtaskId: Long, isCompleted: Boolean)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksByTaskId(taskId: Long)

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun getAllCategoriesFlow(): Flow<List<TaskCategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllCategoriesList(): List<TaskCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TaskCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultCategories(categories: List<TaskCategoryEntity>)

    @Update
    suspend fun updateCategory(category: TaskCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: TaskCategoryEntity)

    // --- Backup & Restore Bulk Operations ---
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksRaw(): List<TaskEntity>

    @Query("SELECT * FROM subtasks")
    suspend fun getAllSubtasksRaw(): List<SubTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAllCategories(categories: List<TaskCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAllTasks(tasks: List<TaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restoreAllSubtasks(subtasks: List<SubTaskEntity>)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Query("DELETE FROM subtasks")
    suspend fun clearAllSubtasks()

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()
}
