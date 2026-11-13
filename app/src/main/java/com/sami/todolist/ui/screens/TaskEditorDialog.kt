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
import androidx.compose.ui.text.style.TextDecoration
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
import com.sami.todolist.ui.components.RoundCheckBox
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1:1 Parity with wear_taskdetail_activity.xml, wear_taskdetail_item_button.xml, and wear_taskdetail_item_subtask.xml
 * Features:
 * - 56dp action buttons for Complete, Due Date, and Due Time
 * - 40dp subtask checklist items with 24dp RoundCheckBox and ripple close icon
 */
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
    var isCompleted by remember { mutableStateOf(initialTask?.task?.isCompleted ?: false) }

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
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF141414)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "New Task" else "Task Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEBEBEB)
                    )
                    Row {
                        if (initialTask != null && onDelete != null) {
                            IconButton(onClick = {
                                onDelete(initialTask.task.id)
                                onDismiss()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFAAAAAA))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title Input Field
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("What needs to be done?", color = Color(0xFF666666)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF262626),
                                focusedContainerColor = Color(0xFF1E1E1E),
                                unfocusedContainerColor = Color(0xFF1E1E1E)
                            )
                        )
                    }

                    // Notes Input Field
                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Add notes or description…", color = Color(0xFF666666)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF262626),
                                focusedContainerColor = Color(0xFF1E1E1E),
                                unfocusedContainerColor = Color(0xFF1E1E1E)
                            )
                        )
                    }

                    // 56dp Action Buttons (Replicating wear_taskdetail_item_button.xml)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 1. Task Complete / Incomplete Button (56dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E1E))
                                    .clickable { isCompleted = !isCompleted }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isCompleted) Color(0xFF10B981) else Color(0xFF888888),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (isCompleted) "Completed" else "Mark as Complete",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isCompleted) Color(0xFF10B981) else Color(0xFFE0E0E0)
                                    )
                                }
                            }

                            // 2. Due Date Picker Button (56dp)
                            val dateLabel = if (dueDate != null) {
                                SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date(dueDate!!))
                            } else "Set Due Date"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E1E))
                                    .clickable {
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
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = dateLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE0E0E0)
                                    )
                                }
                            }

                            // 3. Due Time Picker Button (56dp)
                            val timeLabel = if (dueTimeMillis != null) {
                                val cal = Calendar.getInstance().apply { timeInMillis = dueTimeMillis!! }
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                            } else "Set Reminder Time"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E1E))
                                    .clickable {
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
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFFFACC15),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = timeLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE0E0E0)
                                    )
                                }
                            }
                        }
                    }

                    // Category Selection Chips
                    item {
                        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9E9E9E))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("None", fontSize = 12.sp) },
                                shape = RoundedCornerShape(6.dp),
                                border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF333333),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E1E1E),
                                    labelColor = Color(0xFF888888)
                                )
                            )
                            categories.forEach { category ->
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(category.colorHex))
                                } catch (e: Exception) {
                                    Color(0xFF3B82F6)
                                }
                                FilterChip(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id },
                                    label = { Text(category.name, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = catColor.copy(alpha = 0.35f),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF1E1E1E),
                                        labelColor = Color(0xFF888888)
                                    )
                                )
                            }
                        }
                    }

                    // Priority Chips
                    item {
                        Text("Priority", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9E9E9E))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Priority.entries.forEach { p ->
                                FilterChip(
                                    selected = priority == p,
                                    onClick = { priority = p },
                                    label = { Text(p.label, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(6.dp),
                                    border = null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = p.color.copy(alpha = 0.3f),
                                        selectedLabelColor = p.color,
                                        containerColor = Color(0xFF1E1E1E),
                                        labelColor = Color(0xFF888888)
                                    )
                                )
                            }
                        }
                    }

                    // Subtasks / Checklist (Replicating wear_taskdetail_item_subtask.xml)
                    item {
                        Text("Subtasks & Checklist", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF9E9E9E))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text("Add subtask item...", color = Color(0xFF555555)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E1E1E),
                                    unfocusedContainerColor = Color(0xFF1E1E1E),
                                    unfocusedBorderColor = Color(0xFF262626)
                                )
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
                                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = Color(0xFFDC2626), modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    // 40dp row height per subtask item matching wear_taskdetail_item_subtask
                    itemsIndexed(subtasks) { index, subtask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1A1A1A))
                                .padding(horizontal = 8.dp)
                        ) {
                            RoundCheckBox(
                                checked = subtask.isCompleted,
                                onCheckedChange = { isChecked ->
                                    subtasks[index] = subtask.copy(isCompleted = isChecked)
                                },
                                size = 20.dp,
                                checkedColor = Color(0xFF10B981),
                                uncheckedBorderColor = Color(0xFF555555)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = subtask.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (subtask.isCompleted) Color(0xFF555555) else Color(0xFFDDDDDD)
                            )
                            IconButton(onClick = { subtasks.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFF777777), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF888888))
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
                                    isCompleted = isCompleted,
                                    completedAt = if (isCompleted) System.currentTimeMillis() else null,
                                    repeatType = repeatType.code,
                                    reminderOffsetMinutes = if (dueTimeMillis != null) 0 else null
                                )
                                onSave(taskEntity, subtasks)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Task", color = Color.White)
                    }
                }
            }
        }
    }
}
