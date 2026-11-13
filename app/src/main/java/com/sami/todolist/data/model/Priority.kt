package com.sami.todolist.data.model

import androidx.compose.ui.graphics.Color

enum class Priority(val level: Int, val label: String, val color: Color) {
    NONE(0, "None", Color(0xFF94A3B8)),
    LOW(1, "Low", Color(0xFF10B981)),
    MEDIUM(2, "Medium", Color(0xFFF59E0B)),
    HIGH(3, "High", Color(0xFFEF4444));

    companion object {
        fun fromLevel(level: Int): Priority {
            return entries.find { it.level == level } ?: NONE
        }
    }
}
