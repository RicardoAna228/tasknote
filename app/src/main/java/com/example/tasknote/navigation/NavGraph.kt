package com.example.tasknote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tasknote.ui.auth.ForgotPasswordScreen
import com.example.tasknote.ui.auth.LoginScreen
import com.example.tasknote.ui.auth.RegisterScreen
import com.example.tasknote.ui.auth.ResetPasswordScreen
import com.example.tasknote.ui.calendar.CalendarScreen
import com.example.tasknote.ui.home.HomeScreen
import com.example.tasknote.ui.notifications.NotificationsScreen
import com.example.tasknote.ui.profile.ProfileScreen
import com.example.tasknote.ui.projects.ProjectsScreen
import com.example.tasknote.ui.tasks.EditTaskScreen
import com.example.tasknote.ui.tasks.NewTaskScreen
import com.example.tasknote.ui.tasks.TaskDetailScreen
import com.example.tasknote.ui.tasks.TaskListScreen
import com.example.tasknote.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object Home : Screen("home")
    object TaskList : Screen("tasks")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Int) = "task_detail/$taskId"
    }
    object NewTask : Screen("new_task")
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: Int) = "edit_task/$taskId"
    }
    object Projects : Screen("projects")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
}

@Composable
fun TaskNoteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    taskViewModel: TaskViewModel,                              // ← agregar
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.ResetPassword.route) { ResetPasswordScreen(navController) }

        composable(Screen.Home.route) {
            HomeScreen(navController, viewModel = taskViewModel)  // ← pasar
        }
        composable(Screen.TaskList.route) {
            TaskListScreen(navController, viewModel = taskViewModel)  // ← pasar
        }
        composable(Screen.TaskDetail.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
            TaskDetailScreen(navController, taskId, viewModel = taskViewModel)  // ← pasar
        }
        composable(Screen.NewTask.route) {
            NewTaskScreen(navController, viewModel = taskViewModel)  // ← pasar
        }
        composable(Screen.EditTask.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
            EditTaskScreen(navController, taskId, viewModel = taskViewModel)  // ← pasar
        }
        composable(Screen.Projects.route) { ProjectsScreen(navController) }
        composable(Screen.Calendar.route) { CalendarScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Notifications.route) { NotificationsScreen(navController) }
    }
}