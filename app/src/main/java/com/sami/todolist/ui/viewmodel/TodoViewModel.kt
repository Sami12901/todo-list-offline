package com.sami.todolist.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sami.todolist.TodoApplication
import com.sami.todolist.backup.LocalBackupManager
import com.sami.todolist.data.dao.TaskWithDetails
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.entity.TaskEntity
import com.sami.todolist.reminder.TaskReminderScheduler
import com.sami.todolist.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TaskFilter(val title: String) {
    ALL("All"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed")
}

data class TodoUiState(
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val currentTheme: AppThemeMode = AppThemeMode.CLASSIC_BLUE,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val userMessage: String? = null
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TodoApplication
    private val repository = app.repository
    private val backupManager = LocalBackupManager(app.database.todoDao())

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<TaskCategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskWithDetails>> = repository.allTasksWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<TaskWithDetails>> = combine(
        allTasks,
        _uiState
    ) { tasks, state ->
        val nowCal = Calendar.getInstance()
        val startOfToday = nowCal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = nowCal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        tasks.filter { item ->
            val matchesCategory = state.selectedCategoryId == null || item.task.categoryId == state.selectedCategoryId
            val matchesSearch = state.searchQuery.isBlank() ||
                    item.task.title.contains(state.searchQuery, ignoreCase = true) ||
                    item.task.notes.contains(state.searchQuery, ignoreCase = true)

            val matchesFilter = when (state.currentFilter) {
                TaskFilter.ALL -> !item.task.isCompleted
                TaskFilter.TODAY -> {
                    !item.task.isCompleted &&
                            item.task.dueDate != null &&
                            item.task.dueDate in startOfToday..endOfToday
                }
                TaskFilter.UPCOMING -> {
                    !item.task.isCompleted &&
                            (item.task.dueDate == null || item.task.dueDate > endOfToday)
                }
                TaskFilter.COMPLETED -> item.task.isCompleted
            }

            matchesCategory && matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: TaskFilter) {
        _uiState.update { it.copy(currentFilter = filter) }
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setTheme(theme: AppThemeMode) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun toggleTaskCompletion(taskWithDetails: TaskWithDetails) {
        viewModelScope.launch {
            val newStatus = !taskWithDetails.task.isCompleted
            repository.toggleTaskCompletion(taskWithDetails.task.id, newStatus)
            if (newStatus) {
                TaskReminderScheduler.cancelAlarm(getApplication(), taskWithDetails.task.id)
            }
        }
    }

    fun toggleSubtaskCompletion(subtask: SubTaskEntity) {
        viewModelScope.launch {
            repository.toggleSubtaskCompletion(subtask.id, !subtask.isCompleted)
        }
    }

    fun saveTask(
        task: TaskEntity,
        subtasks: List<SubTaskEntity>
    ) {
        viewModelScope.launch {
            val taskId = repository.saveTaskWithSubtasks(task, subtasks)

            // Schedule alarm if due date and reminder are set
            if (!task.isCompleted && task.dueDate != null) {
                val dueTime = task.dueTimeMillis ?: 0L
                val offset = (task.reminderOffsetMinutes ?: 0) * 60 * 1000L
                val triggerTime = (task.dueDate + dueTime) - offset
                TaskReminderScheduler.scheduleAlarm(
                    context = getApplication(),
                    taskId = taskId,
                    title = task.title,
                    notes = task.notes,
                    triggerAtMillis = triggerTime
                )
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            TaskReminderScheduler.cancelAlarm(getApplication(), taskId)
        }
    }

    fun saveCategory(category: TaskCategoryEntity) {
        viewModelScope.launch {
            repository.saveCategory(category)
        }
    }

    fun deleteCategory(category: TaskCategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Local SAF Backup
    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val result = backupManager.exportBackup(getApplication(), uri)
            _uiState.update {
                it.copy(
                    isExporting = false,
                    userMessage = if (result.isSuccess) "Backup exported successfully!" else "Export failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    // Local SAF Restore
    fun importBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val result = backupManager.importBackup(getApplication(), uri, overwriteExisting = true)
            _uiState.update {
                it.copy(
                    isImporting = false,
                    userMessage = if (result.isSuccess) "Successfully restored ${result.getOrNull()} tasks!" else "Import failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
}
