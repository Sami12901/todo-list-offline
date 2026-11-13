package com.sami.todolist.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sami.todolist.data.dao.TaskWithDetails
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.model.Priority
import com.sami.todolist.ui.components.RoundCheckBox
import com.sami.todolist.ui.viewmodel.TaskFilter
import com.sami.todolist.ui.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1:1 Parity with:
 * - wear_tasklist_activity.xml
 * - wear_tasklist_item.xml (54dp #1A1A1A card, 8dp radius)
 * - wear_tasklist_item_label.xml (Category label + arrow)
 * - wear_tasklist_item_completed.xml ("Check all completed tasks" toggle)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTaskListScreen(
    viewModel: TodoViewModel,
    onAddNewTask: () -> Unit,
    onEditTask: (TaskWithDetails) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val completedCount = remember(allTasks) { allTasks.count { it.task.isCompleted } }

    val activeCategoryName = remember(uiState.selectedCategoryId, categories) {
        if (uiState.selectedCategoryId == null) "All Tasks"
        else categories.find { it.id == uiState.selectedCategoryId }?.name ?: "Tasks"
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Top Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search tasks…", color = Color(0xFF888888)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFAAAAAA)) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFAAAAAA))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedTextColor = Color(0xFFEBEBEB),
                        unfocusedTextColor = Color(0xFFEBEBEB),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF3B82F6)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Horizontal Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E1E1E),
                                labelColor = Color(0xFFCCCCCC)
                            ),
                            border = null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    items(categories) { category ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                            Color(0xFF3B82F6)
                        }
                        FilterChip(
                            selected = uiState.selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.35f),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E1E1E),
                                labelColor = Color(0xFFCCCCCC)
                            ),
                            border = null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Tabs (All, Today, Upcoming, Completed)
                TabRow(
                    selectedTabIndex = uiState.currentFilter.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF3B82F6),
                    divider = {}
                ) {
                    TaskFilter.entries.forEach { filter ->
                        Tab(
                            selected = uiState.currentFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            text = {
                                Text(
                                    text = filter.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (uiState.currentFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.currentFilter == filter) Color(0xFF3B82F6) else Color(0xFF888888)
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewTask,
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color(0xFF0F0F0F)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Category Header Label (wear_tasklist_item_label.xml)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = activeCategoryName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3B3B3)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF777777)
                    )
                }
            }

            // Task Items
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Checklist,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No tasks found",
                                fontSize = 14.sp,
                                color = Color(0xFF777777)
                            )
                            Text(
                                text = "Click + to create your task.",
                                fontSize = 12.sp,
                                color = Color(0xFF555555)
                            )
                        }
                    }
                }
            } else {
                items(tasks, key = { it.task.id }) { item ->
                    TaskCard(
                        item = item,
                        onToggleTask = { viewModel.toggleTaskCompletion(item) },
                        onToggleSubtask = { viewModel.toggleSubtaskCompletion(it) },
                        onClick = { onEditTask(item) }
                    )
                }
            }

            // Completed Tasks Banner (Replicating wear_tasklist_item_completed.xml)
            if (completedCount > 0 && uiState.currentFilter != TaskFilter.COMPLETED) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A1A))
                            .clickable { viewModel.setFilter(TaskFilter.COMPLETED) }
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF9E9E9E)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Check completed tasks ($completedCount)",
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1:1 Parity with wear_tasklist_item.xml
 * Dimensions: 54dp height, 8dp radius, #1A1A1A background
 */
@Composable
fun TaskCard(
    item: TaskWithDetails,
    onToggleTask: () -> Unit,
    onToggleSubtask: (SubTaskEntity) -> Unit,
    onClick: () -> Unit
) {
    val task = item.task
    var isExpanded by remember { mutableStateOf(false) }
    val priority = Priority.fromLevel(task.priority)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A1A))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // RoundCheckBox (24dp circular animated check)
                RoundCheckBox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleTask() },
                    size = 24.dp,
                    checkedColor = Color(0xFF10B981),
                    uncheckedBorderColor = Color(0xFF555555)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Title & notes
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (task.isCompleted) Color(0xFF666666) else Color(0xFFEBEBEB),
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.notes,
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Priority Badge
                if (priority != Priority.NONE) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priority.color.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priority.label,
                            color = priority.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Meta Row (wear_tasklist_item.xml bottom icon indicators)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 34.dp, top = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Due Date / Time
                if (task.dueDate != null) {
                    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(task.dueDate))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = dateStr, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    }
                }

                // Reminder Bell
                if (task.reminderOffsetMinutes != null) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Reminder",
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFFFACC15)
                    )
                }

                // Repeat Loop
                if (task.repeatType != "NONE") {
                    Icon(
                        Icons.Outlined.Repeat,
                        contentDescription = "Repeat",
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFF60A5FA)
                    )
                }

                // Subtask checklist counter
                if (item.subtasks.isNotEmpty()) {
                    val completed = item.subtasks.count { it.isCompleted }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF262626))
                            .clickable { isExpanded = !isExpanded }
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Checklist,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$completed/${item.subtasks.size}",
                            fontSize = 10.sp,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }

                // Category Tag
                if (item.category != null) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(item.category.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF3B82F6)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(catColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.category.name,
                            fontSize = 10.sp,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Expandable Subtasks
            AnimatedVisibility(visible = isExpanded && item.subtasks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 34.dp, top = 8.dp)
                ) {
                    item.subtasks.forEach { subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            RoundCheckBox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { onToggleSubtask(subtask) },
                                size = 18.dp,
                                checkedColor = Color(0xFF10B981),
                                uncheckedBorderColor = Color(0xFF555555)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = subtask.title,
                                fontSize = 13.sp,
                                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (subtask.isCompleted) Color(0xFF555555) else Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            }
        }
    }
}
