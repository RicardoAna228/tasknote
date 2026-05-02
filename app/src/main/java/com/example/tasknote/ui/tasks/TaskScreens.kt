package com.example.tasknote.ui.tasks

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tasknote.data.local.entities.Category
import com.example.tasknote.data.local.entities.Priority
import com.example.tasknote.data.local.entities.SubTask
import com.example.tasknote.data.local.entities.Task
import com.example.tasknote.navigation.Screen
import com.example.tasknote.ui.components.CategoryTag
import com.example.tasknote.ui.components.EmptyState
import com.example.tasknote.ui.components.PriorityTag
import com.example.tasknote.ui.components.TaskItem
import com.example.tasknote.ui.components.TaskNoteBottomNavigation
import com.example.tasknote.ui.components.TaskNoteTopBar
import com.example.tasknote.ui.components.formatDate
import com.example.tasknote.ui.theme.CategoryCompras
import com.example.tasknote.ui.theme.CategoryPersonal
import com.example.tasknote.ui.theme.CategorySalud
import com.example.tasknote.ui.theme.CategoryTrabajo
import com.example.tasknote.ui.theme.PrimaryBlue
import com.example.tasknote.ui.theme.PrimaryDark
import com.example.tasknote.ui.theme.PriorityAlta
import com.example.tasknote.ui.theme.PriorityBaja
import com.example.tasknote.ui.theme.PriorityMedia
import com.example.tasknote.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --------------------------------------------
// Pantalla de Lista de Tareas
// --------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val filteredTasks = remember(allTasks, searchQuery, selectedCategory) {
        allTasks.filter { task ->
            (searchQuery.isEmpty() || task.title.contains(searchQuery, ignoreCase = true)) &&
                    (selectedCategory == null || task.category == selectedCategory)
        }
    }
    val pendingTasks = filteredTasks.filter { !it.completed }
    val completedTasks = filteredTasks.filter { it.completed }

    Scaffold(
        topBar = {
            TaskNoteTopBar(
                title = "Tareas",
                onBackPressed = { navController.navigateUp() },
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
            TaskNoteBottomNavigation(
                currentRoute = Screen.TaskList.route,
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Screen.Home.route)
                        "tasks" -> { /* ya estamos */ }
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
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva tarea")
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

            // Chips de filtro por categoría
            ScrollableRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Todas") }
                )
                Category.values().forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.name.lowercase()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (category) {
                                    Category.TRABAJO -> Icons.Default.Work
                                    Category.PERSONAL -> Icons.Default.Home
                                    Category.COMPRAS -> Icons.Default.ShoppingCart
                                    Category.SALUD -> Icons.Default.Favorite
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Check,
                    title = if (searchQuery.isNotEmpty()) "Sin resultados" else "¡Todo listo por hoy!",
                    description = if (searchQuery.isNotEmpty())
                        "No encontramos tareas que coincidan con \"$searchQuery\"."
                    else
                        "No tienes tareas pendientes. Crea una nueva para empezar a organizar tu día.",
                    onAction = if (searchQuery.isNotEmpty()) {
                        { searchQuery = "" }
                    } else {
                        { navController.navigate(Screen.NewTask.route) }
                    },
                    actionText = if (searchQuery.isNotEmpty()) "Limpiar búsqueda" else "+ Nueva Tarea"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (pendingTasks.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Pendientes hoy",
                                topPadding = 0.dp
                            )
                        }
                        items(pendingTasks) { task ->
                            TaskItem(
                                task = task,
                                onTaskClick = {
                                    navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                },
                                onCheckChange = { completed ->
                                    viewModel.updateTask(task.copy(completed = completed))
                                },
                                onEditClick = {
                                    navController.navigate(Screen.EditTask.createRoute(task.id))
                                },
                                onDeleteClick = {
                                    viewModel.deleteTask(task)
                                }
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                        }
                    }

                    if (completedTasks.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Completadas")
                        }
                        items(completedTasks) { task ->
                            TaskItem(
                                task = task,
                                onTaskClick = {
                                    navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                },
                                onCheckChange = { completed ->
                                    viewModel.updateTask(task.copy(completed = completed))
                                }
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, topPadding: Dp = 0.dp) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = topPadding, bottom = 8.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrollableRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

// --------------------------------------------
// Pantalla de Detalle de Tarea
// --------------------------------------------
@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: Int,
    viewModel: TaskViewModel
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val task = allTasks.find { it.id == taskId }
    val subTasks by viewModel.getSubTasksForTask(taskId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val completedSubTasks = subTasks.count { it.completed }
    val totalSubTasks = subTasks.size
    val progress = if (totalSubTasks > 0) completedSubTasks.toFloat() / totalSubTasks else 0f

    Scaffold(
        topBar = {
            TaskNoteTopBar(
                title = "Detalle de Tarea",
                onBackPressed = { navController.navigateUp() },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.EditTask.createRoute(task.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = PriorityAlta
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Tarjeta de cabecera
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryDark
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryTag(task.category)
                            PriorityTag(task.priority)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (totalSubTasks > 0) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Progreso",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Información
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        InfoRow(
                            icon = Icons.Default.DateRange,
                            label = "Fecha límite",
                            value = task.dueDate?.let { formatDate(it) } ?: "Sin fecha"
                        )
                        val projectName = task.projectId
                            ?.let { projectId -> allProjects.firstOrNull { it.id == projectId }?.name }
                            ?: task.category.name.lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        InfoRow(
                            icon = Icons.Default.Folder,
                            label = "Proyecto",
                            value = projectName
                        )
                        InfoRow(
                            icon = Icons.Default.Flag,
                            label = "Prioridad",
                            value = task.priority.name.lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        )
                        if (task.description.isNotBlank()) {
                            InfoRow(
                                icon = Icons.Default.Description,
                                label = "Descripción",
                                value = task.description,
                                isLast = true
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Subtareas
            if (subTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Subtareas ($completedSubTasks/$totalSubTasks)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(subTasks) { subTask ->
                    SubtaskItem(
                        subTask = subTask,
                        onCheckedChange = { completed ->
                            scope.launch {
                                viewModel.updateSubTask(subTask.copy(completed = completed))
                            }
                        }
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Botones de acción
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.updateTask(task.copy(completed = !task.completed))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (task.completed) PrimaryBlue else PriorityBaja
                        )
                    ) {
                        Icon(
                            if (task.completed) Icons.Default.Close else Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (task.completed) "Marcar pendiente" else "Marcar completa")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PriorityAlta.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = PriorityAlta
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar tarea") },
            text = { Text("¿Estás seguro de que deseas eliminar \"${task.title}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteTask(task)
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PriorityAlta)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = PrimaryBlue
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SubtaskItem(
    subTask: SubTask,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = subTask.completed,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = PriorityBaja)
        )
        Text(
            text = subTask.title,
            style = MaterialTheme.typography.bodyMedium,
            textDecoration = if (subTask.completed) TextDecoration.LineThrough else TextDecoration.None,
            color = if (subTask.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// --------------------------------------------
// Pantalla de Nueva Tarea
// --------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    TaskFormScreen(
        navController = navController,
        viewModel = viewModel,
        task = null
    )
}

// --------------------------------------------
// Pantalla de Edición de Tarea
// --------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    navController: NavController,
    taskId: Int,
    viewModel: TaskViewModel
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val task = allTasks.find { it.id == taskId }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    TaskFormScreen(
        navController = navController,
        viewModel = viewModel,
        task = task
    )
}

// --------------------------------------------
// Formulario común para Crear/Editar
// --------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    task: Task?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.MEDIA) }
    var category by remember { mutableStateOf(task?.category ?: Category.TRABAJO) }
    var dueDate by remember { mutableStateOf(task?.dueDate) }
    val allProjects by viewModel.allProjects.collectAsState()
    var selectedProjectId by remember { mutableStateOf(task?.projectId) }
    var selectedDefaultCategory by remember { mutableStateOf<Category?>(null) }
    val projectOptions = remember(allProjects) {
        val categoryDefaults = Category.values().map { category ->
            ProjectOption(
                id = null,
                name = category.name.lowercase().replaceFirstChar { it.titlecase() },
                category = category
            )
        }
        val customProjects = allProjects.map { project ->
            ProjectOption(id = project.id, name = project.name, category = project.category)
        }
        categoryDefaults + customProjects
    }

    var titleError by remember { mutableStateOf(false) }
    var priorityError by remember { mutableStateOf(false) }
    var showErrorCard by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun validateAndSave() {
        titleError = title.isBlank()
        priorityError = priority == null
        showErrorCard = titleError || priorityError

        if (!showErrorCard) {
            val resolvedCategory = when {
                selectedProjectId != null -> allProjects.firstOrNull { it.id == selectedProjectId }?.category ?: category
                selectedDefaultCategory != null -> selectedDefaultCategory ?: category
                else -> category
            }
            scope.launch {
                if (task == null) {
                    viewModel.createTask(
                        Task(
                            title = title,
                            description = description,
                            priority = priority,
                            category = resolvedCategory,
                            projectId = selectedProjectId,
                            dueDate = dueDate
                        )
                    )
                } else {
                    viewModel.updateTask(
                        task.copy(
                            title = title,
                            description = description,
                            priority = priority,
                            category = resolvedCategory,
                            projectId = selectedProjectId,
                            dueDate = dueDate
                        )
                    )
                }
                navController.navigateUp()
            }
        }
    }

    Scaffold(
        topBar = {
            TaskNoteTopBar(
                title = if (task == null) "Nueva Tarea" else "Editar Tarea",
                onBackPressed = { navController.navigateUp() },
                actions = {
                    TextButton(onClick = {
                        title = ""
                        description = ""
                        priority = Priority.MEDIA
                        category = Category.TRABAJO
                        selectedProjectId = null
                        selectedDefaultCategory = null
                        dueDate = null
                    }) {
                        Text("Limpiar", color = PrimaryBlue)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Título de la tarea") },
                    placeholder = { Text("Ej: Comprar materiales de oficina") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = titleError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        errorBorderColor = PriorityAlta
                    )
                )
                if (titleError) {
                    Text(
                        text = "⚠ El título es requerido",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriorityAlta,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            // Descripción
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Añade detalles sobre lo que necesitas hacer...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue
                    )
                )
            }

            // Prioridad
            item {
                Text(
                    text = "Nivel de Prioridad",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { p ->
                        PriorityPill(
                            priority = p,
                            selected = priority == p,
                            onClick = { priority = p; priorityError = false }
                        )
                    }
                }
                if (priorityError) {
                    Text(
                        text = "⚠ Selecciona un nivel de prioridad",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriorityAlta,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            // Categoría / Proyecto
            item {
                Text(
                    text = "Proyecto",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                var showProjectMenu by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showProjectMenu,
                    onExpandedChange = { showProjectMenu = !showProjectMenu }
                ) {
                    OutlinedTextField(
                        value = when {
                            selectedProjectId != null -> projectOptions.firstOrNull { it.id == selectedProjectId }?.name.orEmpty()
                            selectedDefaultCategory != null -> selectedDefaultCategory!!.name.lowercase().replaceFirstChar { it.titlecase() }
                            else -> "Sin proyecto"
                        },
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProjectMenu)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showProjectMenu,
                        onDismissRequest = { showProjectMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin proyecto") },
                            onClick = {
                                selectedProjectId = null
                                selectedDefaultCategory = null
                                showProjectMenu = false
                            }
                        )
                        projectOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    selectedProjectId = option.id
                                    selectedDefaultCategory = if (option.id == null) option.category else null
                                    showProjectMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Fecha
            item {
                val openDatePicker = {
                    val calendar = Calendar.getInstance()
                    dueDate?.let { calendar.timeInMillis = it }
                    DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                            val selectedDate = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            dueDate = selectedDate.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDatePicker() }
                ) {
                    OutlinedTextField(
                        value = dueDate?.let { dateFormatter.format(Date(it)) } ?: "",
                        onValueChange = { },
                        label = { Text("Fecha de entrega") },
                        placeholder = { Text("Seleccionar fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { openDatePicker() }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar fecha")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue
                        )
                    )
                }
            }

            // Mensaje de error general
            if (showErrorCard) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PriorityAlta.copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            PriorityAlta.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("⚠️", fontSize = 14.sp, color = PriorityAlta)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Completa los campos requeridos para ${if (task == null) "crear" else "actualizar"} la tarea.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PriorityAlta.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Botón Guardar
            item {
                Button(
                    onClick = { validateAndSave() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showErrorCard) PriorityAlta else PrimaryBlue
                    )
                ) {
                    Text(
                        text = if (task == null) "Crear Tarea" else "Guardar Cambios",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class ProjectOption(
    val id: Int?,
    val name: String,
    val category: Category?,
)

// --------------------------------------------
// Componentes auxiliares del formulario
// --------------------------------------------
@Composable
fun RowScope.PriorityPill(
    priority: Priority,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (bgColor, textColor, borderColor) = when (priority) {
        Priority.BAJA -> Triple(
            PriorityBaja.copy(alpha = 0.1f),
            PriorityBaja,
            if (selected) PriorityBaja else PriorityBaja.copy(alpha = 0.3f)
        )
        Priority.MEDIA -> Triple(
            PriorityMedia.copy(alpha = 0.1f),
            PriorityMedia,
            if (selected) PriorityMedia else PriorityMedia.copy(alpha = 0.3f)
        )
        Priority.ALTA -> Triple(
            PriorityAlta.copy(alpha = 0.1f),
            PriorityAlta,
            if (selected) PriorityAlta else PriorityAlta.copy(alpha = 0.3f)
        )
    }

    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) textColor else bgColor,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                when (priority) {
                    Priority.BAJA -> Text("✓", fontSize = 12.sp, color = textColor)
                    Priority.MEDIA -> Text("!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Priority.ALTA -> Text("⚠", fontSize = 12.sp, color = textColor)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = priority.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else textColor
            )
        }
    }
}

@Composable
fun RowScope.ProjectChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (bgColor, textColor, borderColor, icon) = when (category) {
        Category.TRABAJO -> ProjectChipStyle(
            bgColor = CategoryTrabajo.copy(alpha = 0.1f),
            textColor = CategoryTrabajo,
            borderColor = CategoryTrabajo.copy(alpha = 0.25f),
            icon = Icons.Default.Work
        )
        Category.PERSONAL -> ProjectChipStyle(
            bgColor = CategoryPersonal.copy(alpha = 0.1f),
            textColor = CategoryPersonal,
            borderColor = CategoryPersonal.copy(alpha = 0.25f),
            icon = Icons.Default.Home
        )
        Category.COMPRAS -> ProjectChipStyle(
            bgColor = CategoryCompras.copy(alpha = 0.1f),
            textColor = CategoryCompras,
            borderColor = CategoryCompras.copy(alpha = 0.25f),
            icon = Icons.Default.ShoppingCart
        )
        Category.SALUD -> ProjectChipStyle(
            bgColor = CategorySalud.copy(alpha = 0.1f),
            textColor = CategorySalud,
            borderColor = CategorySalud.copy(alpha = 0.25f),
            icon = Icons.Default.Favorite
        )
    }

    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) textColor else bgColor,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Color.White else textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else textColor
            )
        }
    }
}

private data class ProjectChipStyle(
    val bgColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)