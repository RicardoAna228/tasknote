package com.example.tasknote.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tasknote.ui.components.EmptyState
import com.example.tasknote.ui.components.TaskNoteTopBar
import com.example.tasknote.ui.theme.PrimaryBlue
import com.example.tasknote.ui.theme.PriorityBaja
import com.example.tasknote.ui.theme.PriorityMedia

// Modelo de notificación (mock)
data class NotificationItem(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val type: NotificationType,
    val read: Boolean
)

enum class NotificationType {
    SUCCESS, WARNING, INFO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController
) {
    // Datos de ejemplo (en producción vendrían del ViewModel/Repository)
    val notifications = remember {
        listOf(
            NotificationItem(
                id = 1,
                title = "¡Felicidades! Has terminado Revisar Sistema de Diseño",
                description = "Tarea Completada",
                time = "hace 5 min",
                type = NotificationType.SUCCESS,
                read = false
            ),
            NotificationItem(
                id = 2,
                title = "La tarea 'Llamada cliente ABC' vence en 30 minutos.",
                description = "Recordatorio",
                time = "hace 2 horas",
                type = NotificationType.WARNING,
                read = false
            ),
            NotificationItem(
                id = 3,
                title = "Has terminado 'Configuración de Servidores'.",
                description = "Tarea Completada",
                time = "ayer a las 10:20",
                type = NotificationType.SUCCESS,
                read = true
            ),
            NotificationItem(
                id = 4,
                title = "Nueva tarea asignada: 'Documentar API'",
                description = "Nueva tarea",
                time = "ayer a las 15:45",
                type = NotificationType.INFO,
                read = true
            )
        )
    }

    val unreadCount = notifications.count { !it.read }
    val groupedNotifications = notifications.groupBy {
        if (it.time.startsWith("hace") || it.time.startsWith("Hoy")) "HOY" else "AYER"
    }

    Scaffold(
        topBar = {
            TaskNoteTopBar(
                title = "Notificaciones",
                onBackPressed = { navController.navigateUp() },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { /* Marcar todas como leídas */ }
                        ) {
                            Text(
                                "Marcar todo",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Notifications,
                title = "Todo al día",
                description = "No tienes notificaciones nuevas. Te avisaremos cuando haya algo importante."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedNotifications.forEach { (section, items) ->
                    item {
                        Text(
                            text = section,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                top = if (section == "HOY") 8.dp else 16.dp,
                                bottom = 4.dp
                            )
                        )
                    }
                    items(items) { notification ->
                        NotificationCard(notification = notification)
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navegar al detalle de la tarea/recordatorio */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.read) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono según tipo
            val (iconBgColor, iconColor, emoji) = when (notification.type) {
                NotificationType.SUCCESS -> Triple(
                    PriorityBaja.copy(alpha = 0.12f),
                    PriorityBaja,
                    "✅"
                )
                NotificationType.WARNING -> Triple(
                    PriorityMedia.copy(alpha = 0.12f),
                    PriorityMedia,
                    "⏰"
                )
                NotificationType.INFO -> Triple(
                    PrimaryBlue.copy(alpha = 0.12f),
                    PrimaryBlue,
                    "ℹ️"
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (notification.read)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• ${notification.time}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                )
            }
        }
    }
}