package com.sami.todolist.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sami.todolist.data.dao.TodoDao
import com.sami.todolist.data.entity.SubTaskEntity
import com.sami.todolist.data.entity.TaskCategoryEntity
import com.sami.todolist.data.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        TaskCategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getDatabase(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_list_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(database.todoDao())
                    }
                }
            }

            suspend fun populateDefaultCategories(dao: TodoDao) {
                val defaults = listOf(
                    TaskCategoryEntity(name = "Work", colorHex = "#3B82F6", iconName = "ic_work", sortOrder = 1),
                    TaskCategoryEntity(name = "Personal", colorHex = "#10B981", iconName = "ic_person", sortOrder = 2),
                    TaskCategoryEntity(name = "Study", colorHex = "#8B5CF6", iconName = "ic_school", sortOrder = 3),
                    TaskCategoryEntity(name = "Shopping", colorHex = "#F59E0B", iconName = "ic_shopping_cart", sortOrder = 4),
                    TaskCategoryEntity(name = "Health", colorHex = "#EC4899", iconName = "ic_fitness", sortOrder = 5)
                )
                dao.insertDefaultCategories(defaults)
            }
        }
    }
}
