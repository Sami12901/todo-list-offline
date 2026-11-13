package com.sami.todolist.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sami.todolist.data.dao.TodoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class LocalBackupManager(private val todoDao: TodoDao) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportBackup(context: Context, destinationUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val categories = todoDao.getAllCategoriesList()
                val tasks = todoDao.getAllTasksRaw()
                val subtasks = todoDao.getAllSubtasksRaw()

                val payload = BackupPayload(
                    metadata = BackupMetadata(),
                    categories = categories,
                    tasks = tasks,
                    subtasks = subtasks
                )

                val jsonString = gson.toJson(payload)

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(jsonString)
                        writer.flush()
                    }
                } ?: return@withContext Result.failure(Exception("Could not open destination storage for writing."))

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun importBackup(
        context: Context,
        sourceUri: Uri,
        overwriteExisting: Boolean = true
    ): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                } ?: return@withContext Result.failure(Exception("Could not read backup file."))

                val payload = gson.fromJson(jsonString, BackupPayload::class.java)
                    ?: return@withContext Result.failure(Exception("Invalid backup JSON format."))

                if (overwriteExisting) {
                    todoDao.clearAllSubtasks()
                    todoDao.clearAllTasks()
                    todoDao.clearAllCategories()
                }

                if (payload.categories.isNotEmpty()) {
                    todoDao.restoreAllCategories(payload.categories)
                }
                if (payload.tasks.isNotEmpty()) {
                    todoDao.restoreAllTasks(payload.tasks)
                }
                if (payload.subtasks.isNotEmpty()) {
                    todoDao.restoreAllSubtasks(payload.subtasks)
                }

                Result.success(payload.tasks.size)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
