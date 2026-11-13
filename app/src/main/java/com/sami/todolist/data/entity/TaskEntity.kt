package com.sami.todolist.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("dueDate"),
        Index("isCompleted")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val categoryId: Long? = null,
    val dueDate: Long? = null,
    val dueTimeMillis: Long? = null,
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val repeatType: String = "NONE",
    val repeatInterval: Int = 1,
    val reminderOffsetMinutes: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
