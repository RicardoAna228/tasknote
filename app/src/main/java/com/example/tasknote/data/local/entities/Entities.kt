package com.example.tasknote.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority { ALTA, MEDIA, BAJA }
enum class Category { TRABAJO, SALUD, COMPRAS, PERSONAL }

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: Category,
    val color: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val priority: Priority,
    val category: Category,
    val projectId: Int? = null,
    val dueDate: Long? = null,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subtasks")
data class SubTask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int,
    val title: String,
    val completed: Boolean = false
)