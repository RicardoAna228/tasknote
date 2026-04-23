package com.example.tasknote.data.repository

import com.example.tasknote.data.local.TaskNoteDatabase
import com.example.tasknote.data.local.UserPreferences
import com.example.tasknote.data.local.entities.Project
import com.example.tasknote.data.local.entities.SubTask
import com.example.tasknote.data.local.entities.Task
import kotlinx.coroutines.flow.Flow

class TaskNoteRepository(
    private val database: TaskNoteDatabase,
    private val userPreferences: UserPreferences
) {
    fun getAllTasks(): Flow<List<Task>> = database.taskDao().getAllTasks()
    suspend fun getTaskById(taskId: Int): Task? = database.taskDao().getTaskById(taskId)
    suspend fun insertTask(task: Task): Long = database.taskDao().insertTask(task)
    suspend fun updateTask(task: Task) = database.taskDao().updateTask(task)
    suspend fun deleteTask(task: Task) = database.taskDao().deleteTask(task)

    fun getAllProjects(): Flow<List<Project>> = database.projectDao().getAllProjects()
    suspend fun insertProject(project: Project): Long = database.projectDao().insertProject(project)
    suspend fun updateProject(project: Project) = database.projectDao().updateProject(project)
    suspend fun deleteProject(project: Project) = database.projectDao().deleteProject(project)

    fun getSubTasksForTask(taskId: Int): Flow<List<SubTask>> = database.subTaskDao().getSubTasksForTask(taskId)
    suspend fun insertSubTask(subTask: SubTask) = database.subTaskDao().insertSubTask(subTask)
    suspend fun updateSubTask(subTask: SubTask) = database.subTaskDao().updateSubTask(subTask)
    suspend fun deleteSubTask(subTask: SubTask) = database.subTaskDao().deleteSubTask(subTask)

    val darkThemeFlow: Flow<Boolean> = userPreferences.darkThemeFlow
    suspend fun setDarkTheme(enabled: Boolean) = userPreferences.setDarkTheme(enabled)
    val notificationsEnabledFlow: Flow<Boolean> = userPreferences.notificationsEnabledFlow
    suspend fun setNotificationsEnabled(enabled: Boolean) = userPreferences.setNotificationsEnabled(enabled)
}