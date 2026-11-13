package com.sami.todolist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.ui.viewmodel.TodoViewModel

/**
 * 1:1 Parity with wear_taskctg_activity.xml & wear_taskctg_item.xml
 * Features:
 * - 48dp card height with #1A1A1A background
 * - 8dp corner radius
 * - Category color dot and title on left
 * - Right-aligned category task count in white_60alpha (#9E9E9E)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(viewModel: TodoViewModel) {
    val categories by viewModel.categories.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#3B82F6") }

    val availableColors = listOf(
        "#3B82F6", "#10B981", "#8B5CF6", "#F59E0B", "#EF4444", "#EC4899", "#06B6D4", "#64748B"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = Color(0xFF101010)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Categories",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEBEBEB)
            )
            Text(
                text = "Manage your task categories and colors",
                fontSize = 13.sp,
                color = Color(0xFF888888)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF3B82F6)
                    }

                    // Task count for this category
                    val taskCount = allTasks.count { it.task.categoryId == category.id && !it.task.isCompleted }

                    // 48dp row height matching wear_taskctg_item.xml
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1A1A))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Category Color Dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            // Category Name (white_87alpha)
                            Text(
                                text = category.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFDDDDDD),
                                modifier = Modifier.weight(1f)
                            )

                            // Category Task Count (white_60alpha)
                            Text(
                                text = "$taskCount",
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E),
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            // Delete Option
                            IconButton(
                                onClick = { viewModel.deleteCategory(category) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF1E1E1E),
                title = { Text("Add Category", color = Color(0xFFEBEBEB)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name", color = Color(0xFF888888)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF333333)
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Pick Badge Color:", fontSize = 13.sp, color = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableColors.forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColorHex = hex }
                                        .then(
                                            if (selectedColorHex == hex) Modifier.padding(2.dp) else Modifier
                                        )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                viewModel.saveCategory(
                                    TaskCategoryEntity(
                                        name = newCategoryName.trim(),
                                        colorHex = selectedColorHex,
                                        sortOrder = categories.size + 1
                                    )
                                )
                                newCategoryName = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = Color(0xFF888888))
                    }
                }
            )
        }
    }
}
