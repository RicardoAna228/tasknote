package com.example.tasknote.viewmodel

import com.example.tasknote.data.local.entities.Project
import com.example.tasknote.data.local.entities.SubTask
import com.example.tasknote.data.local.entities.Task
import com.example.tasknote.data.repository.TaskNoteRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskNoteRepository
) : ViewModel() {

    private val _allTasks = repository.getAllTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val allTasks: StateFlow<List<Task>> = _allTasks

    private val _allProjects = repository.getAllProjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val allProjects: StateFlow<List<Project>> = _allProjects

    fun getSubTasksForTask(taskId: Int): Flow<List<SubTask>> = repository.getSubTasksForTask(taskId)

    fun createTask(task: Task) {
        viewModelScope.launch { repository.insertTask(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun createProject(project: Project) {
        viewModelScope.launch { repository.insertProject(project) }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch { repository.updateProject(project) }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    fun addSubTask(subTask: SubTask) {
        viewModelScope.launch { repository.insertSubTask(subTask) }
    }

    fun updateSubTask(subTask: SubTask) {
        viewModelScope.launch { repository.updateSubTask(subTask) }
    }

    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch { repository.deleteSubTask(subTask) }
    }

    val darkThemeFlow: Flow<Boolean> = repository.darkThemeFlow
    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    val notificationsEnabledFlow: Flow<Boolean> = repository.notificationsEnabledFlow
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    class Factory(
        private val repository: TaskNoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}