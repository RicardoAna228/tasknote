package com.example.tasknote.data.local.dao

import androidx.room.*
import com.example.tasknote.data.local.entities.Project
import com.example.tasknote.data.local.entities.SubTask
import com.example.tasknote.data.local.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksByProject(projectId: Int): Flow<List<Task>>
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface SubTaskDao {
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubTasksForTask(taskId: Int): Flow<List<SubTask>>

    @Insert
    suspend fun insertSubTask(subTask: SubTask)

    @Update
    suspend fun updateSubTask(subTask: SubTask)

    @Delete
    suspend fun deleteSubTask(subTask: SubTask)
}