package com.example.pocketsafe.ui.expense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pocketsafe.ui.theme.PocketSafeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseEntryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketSafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseEntryScreen(
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
} 