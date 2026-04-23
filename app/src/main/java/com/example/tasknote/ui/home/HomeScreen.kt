package com.example.tasknote.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tasknote.data.local.entities.Category
import com.example.tasknote.navigation.Screen
import com.example.tasknote.ui.components.TaskItem
import com.example.tasknote.ui.components.TaskNoteBottomNavigation
import com.example.tasknote.ui.theme.CategoryCompras
import com.example.tasknote.ui.theme.CategoryPersonal
import com.example.tasknote.ui.theme.CategorySalud
import com.example.tasknote.ui.theme.CategoryTrabajo
import com.example.tasknote.ui.theme.PrimaryBlue
import com.example.tasknote.ui.theme.PrimaryDark
import com.example.tasknote.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allTasks by viewModel.allTasks.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()

    // Filtrar tareas para hoy (simplificado)
    val todayTasks = allTasks.filter { task ->
        task.dueDate?.let { dueDate ->
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val taskDate = Calendar.getInstance().apply {
                timeInMillis = dueDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            taskDate == today
        } ?: false
    }

    val upcomingTasks = allTasks.filter { !it.completed && (it.dueDate == null || it.dueDate > System.currentTimeMillis()) }
    val completedTasks = allTasks.count { it.completed }

    // Proyectos con emojis
    val categoryEmojis = mapOf(
        Category.TRABAJO to "💼",
        Category.PERSONAL to "👤",
        Category.COMPRAS to "🛒",
        Category.SALUD to "💪"
    )

    Scaffold(
        topBar = {
            // Barra superior personalizada como en el diseño
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "TaskNote",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                    BadgedBox(badge = { if (true) Badge() }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryDark)
                            )
                        )
                        .clickable { navController.navigate(Screen.Profile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("U", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            TaskNoteBottomNavigation (
                currentRoute = Screen.Home.route,
                onNavigate = { route ->
                    when (route) {
                        "home" -> {  }
                        "tasks" -> navController.navigate(Screen.TaskList.route)
                        "calendar" -> navController.navigate(Screen.Calendar.route)
                        "projects" -> navController.navigate(Screen.Projects.route)
                        "profile" -> navController.navigate(Screen.Profile.route)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.NewTask.route) },
                containerColor = PrimaryBlue,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea", tint = Color.White)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Saludo
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = "Hola, Usuario 👋",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tienes ${todayTasks.size} tareas para hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tarjetas de estadísticas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        value = todayTasks.size.toString(),
                        label = "Hoy",
                        backgroundColor = PrimaryDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = upcomingTasks.size.toString(),
                        label = "Próximas",
                        backgroundColor = Color(0xFF3A4F96),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = completedTasks.toString(),
                        label = "Completas",
                        backgroundColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Barra de búsqueda
            item {
                com.example.tasknote.ui.components.SearchBar(
                    query = "",
                    onQueryChange = { /* ... */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Sección Proyectos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proyectos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.Projects.route) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Ver todo",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Chips de categorías
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Category.values().forEach { category ->
                        CategoryChip(
                            category = category,
                            emoji = categoryEmojis[category] ?: "📁",
                            onClick = {
                                // Filtrar tareas por categoría (navegar a Tasks con filtro)
                                navController.navigate(Screen.TaskList.route)
                            }
                        )
                    }
                }
            }

            // Sección Tareas Recientes
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tareas Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { navController.navigate(Screen.TaskList.route) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Ver todo",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Lista de tareas recientes (máximo 5)
            val recentTasks = allTasks
                .filter { !it.completed }
                .sortedBy { it.dueDate ?: Long.MAX_VALUE }
                .take(5)

            if (recentTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay tareas pendientes. ¡Crea una nueva!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            recentTasks.forEachIndexed { index, task ->
                                TaskItem(
                                    task = task,
                                    onTaskClick = {
                                        navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                    },
                                    onCheckChange = { checked ->
                                        viewModel.updateTask(task.copy(completed = checked))
                                    }
                                )
                                if (index < recentTasks.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    emoji: String,
    onClick: () -> Unit
) {
    val (bgColor, textColor) = when (category) {
        Category.TRABAJO -> CategoryTrabajo.copy(alpha = 0.12f) to CategoryTrabajo
        Category.PERSONAL -> CategoryPersonal.copy(alpha = 0.12f) to CategoryPersonal
        Category.COMPRAS -> CategoryCompras.copy(alpha = 0.12f) to CategoryCompras
        Category.SALUD -> CategorySalud.copy(alpha = 0.12f) to CategorySalud
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (category) {
                Category.TRABAJO -> "Trabajo"
                Category.PERSONAL -> "Personal"
                Category.COMPRAS -> "Compras"
                Category.SALUD -> "Salud"
            },
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}