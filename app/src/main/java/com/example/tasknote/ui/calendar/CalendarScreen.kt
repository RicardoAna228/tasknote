package com.example.tasknote.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tasknote.data.local.entities.Task
import com.example.tasknote.navigation.Screen
import com.example.tasknote.ui.components.EmptyState
import com.example.tasknote.ui.components.TaskItem
import com.example.tasknote.ui.components.TaskNoteBottomNavigation
import com.example.tasknote.ui.components.TaskNoteTopBar
import com.example.tasknote.ui.theme.PrimaryBlue
import com.example.tasknote.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val allTasks by viewModel.allTasks.collectAsState()

    // Estado para el mes/año actual
    val calendar = Calendar.getInstance()
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    // Día seleccionado
    var selectedDate by remember { mutableStateOf<Date?>(null) }

    // Tareas del día seleccionado
    val tasksForSelectedDate = remember(allTasks, selectedDate) {
        if (selectedDate != null) {
            val cal = Calendar.getInstance()
            allTasks.filter { task ->
                task.dueDate?.let { dueDate ->
                    cal.time = Date(dueDate)
                    val taskCal = Calendar.getInstance().apply { time = Date(dueDate) }
                    val selCal = Calendar.getInstance().apply { time = selectedDate }
                    taskCal.get(Calendar.YEAR) == selCal.get(Calendar.YEAR) &&
                            taskCal.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR)
                } ?: false
            }
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TaskNoteTopBar(
                title = "Calendario",
                onBackPressed = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        BadgedBox(badge = { if (allTasks.any { !it.completed }) Badge() }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                }
            )
        },
        bottomBar = {
            TaskNoteBottomNavigation (
                currentRoute = Screen.Calendar.route,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cabecera del calendario con mes/año y navegación
            item {
                CalendarHeader(
                    month = currentMonth,
                    year = currentYear,
                    onPreviousMonth = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    },
                    onNextMonth = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }
                )
            }

            // Calendario mensual
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Días de la semana
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Cuadrícula de días
                        MonthGrid(
                            month = currentMonth,
                            year = currentYear,
                            tasks = allTasks,
                            selectedDate = selectedDate,
                            onDateSelected = { date -> selectedDate = date }
                        )
                    }
                }
            }

            // Tareas del día seleccionado
            item {
                if (selectedDate != null) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tareas del ${formatSelectedDate(selectedDate)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (tasksForSelectedDate.isNotEmpty()) {
                                TextButton(
                                    onClick = { /* Navegar a lista de tareas filtrada por fecha */ }
                                ) {
                                    Text("Ver todo", color = PrimaryBlue)
                                }
                            }
                        }

                        if (tasksForSelectedDate.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.DateRange,
                                title = "Día libre 🎉",
                                description = "No tienes tareas para el ${formatSelectedDate(selectedDate)}. ¿Quieres agregar algo?",
                                onAction = { navController.navigate(Screen.NewTask.route) },
                                actionText = "+ Añadir tarea para este día"
                            )
                        } else {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    tasksForSelectedDate.forEachIndexed { index, task ->
                                        TaskItem(
                                            task = task,
                                            onTaskClick = {
                                                navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                            },
                                            onCheckChange = { completed ->
                                                viewModel.updateTask(task.copy(completed = completed))
                                            }
                                        )
                                        if (index < tasksForSelectedDate.size - 1) {
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
                } else {
                    // Mensaje cuando no hay día seleccionado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecciona un día para ver las tareas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    month: Int,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "Mes anterior",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "${monthNames[month]} $year",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Mes siguiente",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: Int,
    year: Int,
    tasks: List<Task>,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Domingo, 2 = Lunes, ...
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Mapa de días con tareas
    val tasksByDay = remember(tasks, month, year) {
        val map = mutableMapOf<Int, Boolean>()
        val cal = Calendar.getInstance()
        tasks.forEach { task ->
            task.dueDate?.let { dueDate ->
                cal.time = Date(dueDate)
                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                    map[cal.get(Calendar.DAY_OF_MONTH)] = true
                }
            }
        }
        map
    }

    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    // Construir cuadrícula de 6 filas x 7 columnas
    var dayCounter = 1
    val rows = 6

    Column {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOffset = cellIndex - (firstDayOfWeek - 1) // Ajuste para que Lunes sea primer día (suponiendo locale español)
                    val adjustedOffset = if (firstDayOfWeek == Calendar.SUNDAY) {
                        cellIndex - (firstDayOfWeek - 1) // Domingo es 1, restamos 0
                    } else {
                        cellIndex - (firstDayOfWeek - 2) // Lunes sería 2, restamos 1
                    }

                    // Simplificamos: usamos firstDayOfWeek directamente
                    val startOffset = when (firstDayOfWeek) {
                        Calendar.SUNDAY -> 0
                        Calendar.MONDAY -> 1
                        Calendar.TUESDAY -> 2
                        Calendar.WEDNESDAY -> 3
                        Calendar.THURSDAY -> 4
                        Calendar.FRIDAY -> 5
                        Calendar.SATURDAY -> 6
                        else -> 0
                    }
                    val dayNumber = cellIndex - startOffset + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = Calendar.getInstance().apply {
                            set(year, month, dayNumber)
                        }.time

                        val isToday = isCurrentMonth && dayNumber == todayDay
                        val isSelected = selectedDate?.let {
                            val selCal = Calendar.getInstance().apply { time = it }
                            selCal.get(Calendar.YEAR) == year &&
                                    selCal.get(Calendar.MONTH) == month &&
                                    selCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                        } ?: false

                        val hasTasks = tasksByDay[dayNumber] == true

                        CalendarCell(
                            day = dayNumber,
                            isToday = isToday,
                            isSelected = isSelected,
                            hasTasks = hasTasks,
                            onClick = { onDateSelected(date) }
                        )
                    } else {
                        // Celda vacía (día de mes anterior/siguiente)
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CalendarCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> PrimaryBlue
                    isSelected -> PrimaryBlue.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> Color.White
                    isSelected -> PrimaryBlue
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (hasTasks && !isToday) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                )
            } else if (hasTasks && isToday) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

private fun formatSelectedDate(date: Date?): String {
    if (date == null) return ""
    val format = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
    return format.format(date)
}