package com.sami.todolist.data.model

enum class RepeatType(val code: String, val displayName: String) {
    NONE("NONE", "Never"),
    DAILY("DAILY", "Every Day"),
    WEEKDAYS("WEEKDAYS", "Mon to Fri"),
    WEEKLY("WEEKLY", "Every Week"),
    MONTHLY("MONTHLY", "Every Month"),
    YEARLY("YEARLY", "Every Year");

    companion object {
        fun fromCode(code: String): RepeatType {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: NONE
        }
    }
}
