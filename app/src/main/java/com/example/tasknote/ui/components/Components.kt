package com.example.tasknote.ui.components

import com.example.tasknote.data.local.entities.Category
import com.example.tasknote.data.local.entities.Priority
import com.example.tasknote.ui.theme.CategoryCompras
import com.example.tasknote.ui.theme.CategoryPersonal
import com.example.tasknote.ui.theme.CategorySalud
import com.example.tasknote.ui.theme.CategoryTrabajo
import com.example.tasknote.ui.theme.PriorityAlta
import com.example.tasknote.ui.theme.PriorityBaja
import com.example.tasknote.ui.theme.PriorityMedia
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskNoteTopBar(
    title: String,
    onBackPressed: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBackPressed != null) {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun TaskNoteBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        val items = listOf(
            "home" to (Icons.Default.Home to "Inicio"),
            "tasks" to (Icons.Default.Check to "Tareas"),
            "calendar" to (Icons.Default.DateRange to "Calendario"),
            "projects" to (Icons.Default.Info to "Proyectos"),
            "profile" to (Icons.Default.Person to "Perfil")
        )
        items.forEach { (route, iconLabel) ->
            val (icon, label) = iconLabel
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = { onNavigate(route) }
            )
        }
    }
}

@Composable
fun PriorityTag(priority: Priority) {
    val (text, bgColor, textColor) = when (priority) {
        Priority.ALTA -> Triple("Alta", PriorityAlta.copy(alpha = 0.12f), PriorityAlta)
        Priority.MEDIA -> Triple("Media", PriorityMedia.copy(alpha = 0.12f), PriorityMedia)
        Priority.BAJA -> Triple("Baja", PriorityBaja.copy(alpha = 0.12f), PriorityBaja)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CategoryTag(category: Category) {
    val (text, bgColor, textColor) = when (category) {
        Category.TRABAJO -> Triple("Trabajo", CategoryTrabajo.copy(alpha = 0.12f), CategoryTrabajo)
        Category.SALUD -> Triple("Salud", CategorySalud.copy(alpha = 0.12f), CategorySalud)
        Category.COMPRAS -> Triple("Compras", CategoryCompras.copy(alpha = 0.12f), CategoryCompras)
        Category.PERSONAL -> Triple("Personal", CategoryPersonal.copy(alpha = 0.12f), CategoryPersonal)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TaskItem(
    task: com.example.tasknote.data.local.entities.Task,
    onTaskClick: () -> Unit,
    onCheckChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = onCheckChange,
            colors = CheckboxDefaults.colors(checkedColor = PriorityBaja)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (task.dueDate != null) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(task.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                CategoryTag(task.category)
                PriorityTag(task.priority)
            }
        }
        IconButton(onClick = { /* More options */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Más")
        }
    }
}

fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    onAction: (() -> Unit)? = null,
    actionText: String = ""
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionText) }
        }
    }
}