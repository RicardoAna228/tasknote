package com.example.tasknote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.tasknote.data.local.dao.ProjectDao
import com.example.tasknote.data.local.dao.SubTaskDao
import com.example.tasknote.data.local.dao.TaskDao
import com.example.tasknote.data.local.entities.Category
import com.example.tasknote.data.local.entities.Priority
import com.example.tasknote.data.local.entities.Project
import com.example.tasknote.data.local.entities.SubTask
import com.example.tasknote.data.local.entities.Task

@Database(
    entities = [Task::class, Project::class, SubTask::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskNoteDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun subTaskDao(): SubTaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskNoteDatabase? = null

        fun getDatabase(context: Context): TaskNoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskNoteDatabase::class.java,
                    "tasknote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(priority: String): Priority = Priority.valueOf(priority)

    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(category: String): Category = Category.valueOf(category)

    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? = value?.let { java.util.Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? = date?.time
}