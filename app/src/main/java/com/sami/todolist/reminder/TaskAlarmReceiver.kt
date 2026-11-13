package com.sami.todolist.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sami.todolist.R
import com.sami.todolist.TodoApplication
import com.sami.todolist.data.database.TodoDatabase
import com.sami.todolist.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
        val taskNotes = intent.getStringExtra(EXTRA_TASK_NOTES) ?: ""

        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                if (taskId != -1L) {
                    val db = TodoDatabase.getDatabase(context)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.todoDao().updateTaskStatus(taskId, true, System.currentTimeMillis())
                    }
                    NotificationManagerCompat.from(context).cancel(taskId.toInt())
                }
            }
            ACTION_SNOOZE_TASK -> {
                if (taskId != -1L) {
                    NotificationManagerCompat.from(context).cancel(taskId.toInt())
                    val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000) // 10 minutes
                    TaskReminderScheduler.scheduleAlarm(context, taskId, taskTitle, taskNotes, snoozeTime)
                }
            }
            else -> {
                showTaskNotification(context, taskId, taskTitle, taskNotes)
            }
        }
    }

    private fun showTaskNotification(
        context: Context,
        taskId: Long,
        title: String,
        notes: String
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Mark as Done" action
        val doneIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 100000).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Snooze 10m" action
        val snoozeIntent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_NOTES, notes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId + 200000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TodoApplication.CHANNEL_REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(notes.ifBlank { "You have a scheduled task reminder." })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_logo, context.getString(R.string.action_done), donePendingIntent)
            .addAction(R.drawable.ic_logo, context.getString(R.string.action_snooze), snoozePendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (e: SecurityException) {
            // Android 13+ permission not granted
        }
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.sami.todolist.ACTION_TRIGGER_REMINDER"
        const val ACTION_COMPLETE_TASK = "com.sami.todolist.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.sami.todolist.ACTION_SNOOZE_TASK"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_NOTES = "extra_task_notes"
    }
}
