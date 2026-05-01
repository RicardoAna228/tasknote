package com.example.tasknote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
//import androidx.core.view.WindowCompat
import com.example.tasknote.navigation.TaskNoteNavHost
import com.example.tasknote.ui.theme.TaskNoteTheme
import com.example.tasknote.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {

    private val repository by lazy { (application as TaskNoteApplication).repository }

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TaskNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TaskNoteNavHost(taskViewModel = taskViewModel) // ← único cambio
                    TaskNoteNavHost(
                        modifier = Modifier.fillMaxSize(),
                        taskViewModel = taskViewModel
                    )
                }
            }
        }
    }
}