package com.sami.todolist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.sami.todolist.data.database.TodoDatabase
import com.sami.todolist.data.repository.TodoRepository

class TodoApplication : Application() {

    val database: TodoDatabase by lazy { TodoDatabase.getDatabase(this) }
    val repository: TodoRepository by lazy { TodoRepository(database.todoDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_REMINDERS_ID = "todo_reminders_channel"
    }
}
