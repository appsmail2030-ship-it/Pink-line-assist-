package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.data.local.AppDatabase
import com.example.data.repository.PinkLineRepository
import com.example.ui.PinkLineApp
import com.example.ui.components.AudioAlarmManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PinkLineViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    private val viewModel: PinkLineViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val audioManager = AudioAlarmManager(applicationContext)
        val repository = PinkLineRepository(database, audioManager)
        PinkLineViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MyApplicationTheme {
                PinkLineApp(viewModel = viewModel)
            }
        }
    }
}
