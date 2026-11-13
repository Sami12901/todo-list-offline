package com.sami.todolist.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sami.todolist.data.dao.TaskWithDetails
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.entity.TaskEntity
import com.sami.todolist.data.model.Priority
import com.sami.todolist.data.model.RepeatType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorDialog(
    initialTask: TaskWithDetails? = null,
    categories: List<TaskCategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity, List<SubTaskEntity>) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialTask?.task?.title ?: "") }
    var notes by remember { mutableStateOf(initialTask?.task?.notes ?: "") }
    var selectedCategoryId by remember { mutableStateOf(initialTask?.task?.categoryId) }
    var priority by remember { mutableStateOf(Priority.fromLevel(initialTask?.task?.priority ?: 0)) }
    var dueDate by remember { mutableStateOf(initialTask?.task?.dueDate) }
    var dueTimeMillis by remember { mutableStateOf(initialTask?.task?.dueTimeMillis) }
    var repeatType by remember { mutableStateOf(RepeatType.fromCode(initialTask?.task?.repeatType ?: "NONE")) }
    
    val subtasks = remember {
        mutableStateListOf<SubTaskEntity>().apply {
            if (initialTask != null) {
                addAll(initialTask.subtasks)
            }
        }
    }
    var newSubtaskText by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    if (dueDate != null) calendar.timeInMillis = dueDate!!

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "New Task" else "Edit Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        if (initialTask != null && onDelete != null) {
                            IconButton(onClick = {
                                onDelete(initialTask.task.id)
                                onDismiss()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title input
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Task Title *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Notes input
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Details") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Category Selector
                    item {
                        Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("None") }
                            )
                            categories.forEach { category ->
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(category.colorHex))
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                                FilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    label = { Text(category.name) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Priority Selector
                    item {
                        Text("Priority", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Priority.entries.forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = p.color.copy(alpha = 0.2f),
                                        selectedLabelColor = p.color
                                    )
                                )
                            }
                        }
                    }

                    // Date & Time Picker Row
                    item {
                        Text("Due Date & Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Date Button
                            val dateLabel = if (dueDate != null) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dueDate!!))
                            } else "Set Due Date"

                            OutlinedButton(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            calendar.set(year, month, dayOfMonth, 0, 0, 0)
                                            dueDate = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(dateLabel, fontSize = 13.sp)
                            }

                            // Time Button
                            val timeLabel = if (dueTimeMillis != null) {
                                val cal = Calendar.getInstance().apply { timeInMillis = dueTimeMillis!! }
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                            } else "Set Time"

                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val tCal = Calendar.getInstance().apply {
                                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                set(Calendar.MINUTE, minute)
                                            }
                                            dueTimeMillis = tCal.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(timeLabel, fontSize = 13.sp)
                            }
                        }
                    }

                    // Checklist / Subtasks Builder
                    item {
                        Text("Checklist / Subtasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("Add subtask...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        subtasks.add(
                                            SubTaskEntity(
                                                taskId = initialTask?.task?.id ?: 0,
                                                title = newSubtaskText.trim(),
                                                sortOrder = subtasks.size
                                            )
                                        )
                                        newSubtaskText = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    itemsIndexed(subtasks) { index, subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(Icons.Outlined.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(subtask.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { subtasks.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val taskEntity = TaskEntity(
                                    id = initialTask?.task?.id ?: 0L,
                                    title = title.trim(),
                                    notes = notes.trim(),
                                    categoryId = selectedCategoryId,
                                    dueDate = dueDate,
                                    dueTimeMillis = dueTimeMillis,
                                    priority = priority.level,
                                    isCompleted = initialTask?.task?.isCompleted ?: false,
                                    completedAt = initialTask?.task?.completedAt,
                                    repeatType = repeatType.code,
                                    reminderOffsetMinutes = if (dueTimeMillis != null) 0 else null
                                )
                                onSave(taskEntity, subtasks)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}
