package com.sami.todolist.backup

import com.google.gson.annotations.SerializedName
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.entity.TaskEntity

data class BackupMetadata(
    @SerializedName("appName") val appName: String = "TODO LIST",
    @SerializedName("appVersion") val appVersion: String = "1.0.0",
    @SerializedName("developer") val developer: String = "Developed by Sami",
    @SerializedName("exportTimestamp") val exportTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("schemaVersion") val schemaVersion: Int = 1
)

data class BackupPayload(
    @SerializedName("metadata") val metadata: BackupMetadata = BackupMetadata(),
    @SerializedName("categories") val categories: List<TaskCategoryEntity> = emptyList(),
    @SerializedName("tasks") val tasks: List<TaskEntity> = emptyList(),
    @SerializedName("subtasks") val subtasks: List<SubTaskEntity> = emptyList()
)
