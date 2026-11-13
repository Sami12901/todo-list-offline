package com.sami.todolist.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sami.todolist.data.database.TodoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val db = TodoDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val tasks = db.todoDao().getAllTasksRaw().filter { !it.isCompleted }
                val now = System.currentTimeMillis()

                for (task in tasks) {
                    val dueDate = task.dueDate ?: continue
                    val dueTime = task.dueTimeMillis ?: 0L
                    val offset = (task.reminderOffsetMinutes ?: 0) * 60 * 1000L
                    val triggerTime = (dueDate + dueTime) - offset

                    if (triggerTime > now) {
                        TaskReminderScheduler.scheduleAlarm(
                            context = context,
                            taskId = task.id,
                            title = task.title,
                            notes = task.notes,
                            triggerAtMillis = triggerTime
                        )
                    }
                }
            }
        }
    }
}
