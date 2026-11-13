package com.sami.todolist.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.sami.todolist.data.dao.TaskWithDetails
import com.sami.todolist.ui.screens.*
import com.sami.todolist.ui.theme.TodoListTheme
import com.sami.todolist.ui.viewmodel.TodoViewModel

enum class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TASKS("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    CATEGORIES("Categories", Icons.Filled.Category, Icons.Outlined.Category),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    ABOUT("About", Icons.Filled.Info, Icons.Outlined.Info)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TodoViewModel by viewModels()

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Notification permission callback
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val categories by viewModel.categories.collectAsState()
            var currentNav by remember { mutableStateOf(NavigationItem.TASKS) }

            var editingTask by remember { mutableStateOf<TaskWithDetails?>(null) }
            var isCreatingTask by remember { mutableStateOf(false) }

            val snackbarHostState = remember { SnackbarHostState() }

            // Show snackbar messages for backup/restore operations
            LaunchedEffect(uiState.userMessage) {
                uiState.userMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearUserMessage()
                }
            }

            TodoListTheme(themeMode = uiState.currentTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar {
                            NavigationItem.entries.forEach { item ->
                                val selected = currentNav == item
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentNav = item },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    },
                                    label = { Text(item.title) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentNav) {
                            NavigationItem.TASKS -> {
                                MainTaskListScreen(
                                    viewModel = viewModel,
                                    onAddNewTask = { isCreatingTask = true },
                                    onEditTask = { taskWithDetails ->
                                        editingTask = taskWithDetails
                                    }
                                )
                            }
                            NavigationItem.CALENDAR -> {
                                CalendarViewScreen(
                                    viewModel = viewModel,
                                    onEditTask = { taskWithDetails ->
                                        editingTask = taskWithDetails
                                    }
                                )
                            }
                            NavigationItem.CATEGORIES -> {
                                CategoriesScreen(viewModel = viewModel)
                            }
                            NavigationItem.SETTINGS -> {
                                SettingsScreen(viewModel = viewModel)
                            }
                            NavigationItem.ABOUT -> {
                                AboutScreen()
                            }
                        }
                    }

                    // Task Editor / Creator Dialog
                    if (isCreatingTask || editingTask != null) {
                        TaskEditorDialog(
                            initialTask = editingTask,
                            categories = categories,
                            onDismiss = {
                                isCreatingTask = false
                                editingTask = null
                            },
                            onSave = { task, subtasks ->
                                viewModel.saveTask(task, subtasks)
                            },
                            onDelete = { taskId ->
                                viewModel.deleteTask(taskId)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
