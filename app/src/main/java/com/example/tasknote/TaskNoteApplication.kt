package com.example.tasknote

import android.app.Application
import com.example.tasknote.data.local.TaskNoteDatabase
import com.example.tasknote.data.local.UserPreferences
import com.example.tasknote.data.repository.TaskNoteRepository

class TaskNoteApplication : Application() {
    val database by lazy { TaskNoteDatabase.getDatabase(this) }
    val userPreferences by lazy { UserPreferences(this) }
    val repository by lazy { TaskNoteRepository(database, userPreferences) }
}