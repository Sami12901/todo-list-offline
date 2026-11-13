package com.sami.todolist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sami.todolist.data.dao.TaskWithDetails
import com.sami.todolist.ui.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarViewScreen(
    viewModel: TodoViewModel,
    onEditTask: (TaskWithDetails) -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    var currentMonthCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateMillis by remember { mutableStateOf(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis) }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // Days in current month
    val daysInMonth = remember(currentMonthCalendar) {
        val cal = currentMonthCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

        val list = mutableListOf<Long?>()
        for (i in 0 until firstDayOfWeek) {
            list.add(null)
        }
        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            list.add(cal.timeInMillis)
        }
        list
    }

    // Filter tasks for selected day
    val selectedDayTasks = remember(selectedDateMillis, tasks) {
        val nextDay = selectedDateMillis + (24 * 60 * 60 * 1000L)
        tasks.filter { item ->
            val dueDate = item.task.dueDate
            dueDate != null && dueDate in selectedDateMillis until nextDay
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val cal = currentMonthCalendar.clone() as Calendar
                cal.add(Calendar.MONTH, -1)
                currentMonthCalendar = cal
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Text(
                text = monthFormat.format(currentMonthCalendar.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                val cal = currentMonthCalendar.clone() as Calendar
                cal.add(Calendar.MONTH, 1)
                currentMonthCalendar = cal
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday Headers (S M T W T F S)
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Days Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(260.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(daysInMonth) { dateMillis ->
                if (dateMillis != null) {
                    val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                    val dayNum = cal.get(Calendar.DAY_OF_MONTH)
                    val isSelected = dateMillis == selectedDateMillis
                    val hasTasks = tasks.any {
                        it.task.dueDate != null &&
                                it.task.dueDate in dateMillis until (dateMillis + 86400000L)
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { selectedDateMillis = dateMillis },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayNum.toString(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            if (hasTasks) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        // Selected Date Task List
        val selectedDateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        Text(
            text = "Tasks for ${selectedDateFormat.format(Date(selectedDateMillis))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDayTasks.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks scheduled for this day.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDayTasks) { item ->
                    TaskCard(
                        item = item,
                        onToggleTask = { viewModel.toggleTaskCompletion(item) },
                        onToggleSubtask = { viewModel.toggleSubtaskCompletion(it) },
                        onClick = { onEditTask(item) }
                    )
                }
            }
        }
    }
}
