package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.EpochDatabase
import com.example.data.repository.LifeRepository
import com.example.ui.EpochApp
import com.example.ui.EpochViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dao = EpochDatabase.getInstance(applicationContext).epochDao()
        val repository = LifeRepository(dao)

        setContent {
            val viewModel: EpochViewModel = viewModel(
                factory = EpochViewModel.Factory(repository)
            )
            EpochApp(viewModel = viewModel)
        }
    }
}
