package com.example.tasknote.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tasknote.data.local.entities.Category
import com.example.tasknote.data.local.entities.Project
import com.example.tasknote.data.local.entities.Task
import com.example.tasknote.navigation.Screen
import com.example.tasknote.ui.components.EmptyState
import com.example.tasknote.ui.components.TaskNoteBottomNavigation
import com.example.tasknote.ui.components.TaskNoteTopBar
import com.example.tasknote.ui.theme.CategoryCompras
import com.example.tasknote.ui.theme.CategoryPersonal
import com.example.tasknote.ui.theme.CategorySalud
import com.example.tasknote.ui.theme.CategoryTrabajo
import com.example.tasknote.ui.theme.PrimaryBlue
import com.example.tasknote.ui.theme.PrimaryDark
import com.example.tasknote.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val allProjects by viewModel.allProjects.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    val scope = rememberCoroutineScope()
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Filtrar proyectos por búsqueda
    val filteredProjects = remember(allProjects, searchQuery) {
        if (searchQuery.isBlank()) allProjects
        else allProjects.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Calcular estadísticas
    val activeProjects = filteredProjects.size
    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.completed }
    val completionPercentage = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

    Scaffold(
        topBar = {
            TaskNoteTopBar(
                title = "Proyectos",
                onBackPressed = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        BadgedBox(badge = { if (true) Badge() }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                        }
                    }
                }
            )
        },
        bottomBar = {
            TaskNoteBottomNavigation (
                currentRoute = Screen.Projects.route,
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        "tasks" -> navController.navigate(Screen.TaskList.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        "calendar" -> navController.navigate(Screen.Calendar.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        "projects" -> navController.navigate(Screen.Projects.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                        "profile" -> navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProjectDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo proyecto")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Barra de búsqueda
            com.example.tasknote.ui.components.SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta de resumen
            SummaryCard(
                activeProjects = activeProjects,
                completionPercentage = completionPercentage
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de proyectos
            if (filteredProjects.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Folder,
                    title = "Sin proyectos aún",
                    description = "Crea tu primer proyecto para organizar tus tareas por categorías y hacer seguimiento del progreso.",
                    onAction = { showAddProjectDialog = true },
                    actionText = "Nuevo Proyecto"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProjects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            tasks = allTasks.filter { it.projectId == project.id },
                            onClick = {
                                // Navegar a tareas filtradas por proyecto (podría implementarse)
                                // Por ahora, ir a la lista de tareas
                                navController.navigate(Screen.TaskList.route)
                            },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteProject(project)
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        // Diálogo para agregar proyecto
        if (showAddProjectDialog) {
            AddProjectDialog(
                onDismiss = { showAddProjectDialog = false },
                onAdd = { name, category ->
                    scope.launch {
                        val project = Project(
                            name = name,
                            category = category
                        )
                        viewModel.createProject(project)
                    }
                    showAddProjectDialog = false
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(
    activeProjects: Int,
    completionPercentage: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryDark, PrimaryBlue)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = activeProjects.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Activos",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$completionPercentage%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Completado",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    tasks: List<Task>,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val completedCount = tasks.count { it.completed }
    val totalTasks = tasks.size
    val progress = if (totalTasks > 0) (completedCount.toFloat() / totalTasks) else 0f

    val categoryColor = when (project.category) {
        Category.TRABAJO -> CategoryTrabajo
        Category.SALUD -> CategorySalud
        Category.COMPRAS -> CategoryCompras
        Category.PERSONAL -> CategoryPersonal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del proyecto
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(project.category),
                    contentDescription = project.category.name,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del proyecto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (totalTasks > 0) "$totalTasks tareas pendientes" else "Sin tareas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Barra de progreso
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Progreso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = categoryColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            // Botón de opciones (eliminar)
            var showOptionsMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            showOptionsMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.TRABAJO -> Icons.Default.Work
    Category.SALUD -> Icons.Default.Favorite
    Category.COMPRAS -> Icons.Default.ShoppingCart
    Category.PERSONAL -> Icons.Default.Person
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, category: Category) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.TRABAJO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Nuevo Proyecto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del proyecto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.values().forEach { category ->
                        val isSelected = category == selectedCategory
                        val categoryColor = when (category) {
                            Category.TRABAJO -> CategoryTrabajo
                            Category.SALUD -> CategorySalud
                            Category.COMPRAS -> CategoryCompras
                            Category.PERSONAL -> CategoryPersonal
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = categoryIcon(category),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (category) {
                                            Category.TRABAJO -> "Trabajo"
                                            Category.SALUD -> "Salud"
                                            Category.COMPRAS -> "Compras"
                                            Category.PERSONAL -> "Personal"
                                        }
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), selectedCategory)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}