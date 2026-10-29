package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.service.configureAppCheck
import com.example.ui.JanuMainApp
import com.example.ui.theme.JanuTheme
import com.example.ui.viewmodel.JanuViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JanuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration before Compose setContent or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        viewModel.attachActivity(this)

        setContent {
            JanuTheme {
                JanuMainApp(viewModel = viewModel)
            }
        }
    }
}
