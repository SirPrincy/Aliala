package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.business.BusinessScreen
import com.example.ui.business.BusinessViewModel
import com.example.ui.habits.HabitsScreen
import com.example.ui.habits.HabitsViewModel
import com.example.ui.health.HealthScreen
import com.example.ui.health.HealthViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val habitsViewModel: HabitsViewModel by viewModels()
    private val healthViewModel: HealthViewModel by viewModels()
    private val businessViewModel: BusinessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val isDark = when (uiState.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }
            var currentScreen by remember { mutableStateOf("HOME") }

            MyApplicationTheme(darkTheme = isDark) {
                when (currentScreen) {
                    "HABITS" -> {
                        HabitsScreen(
                            viewModel = habitsViewModel,
                            onNavigateBack = { currentScreen = "HOME" },
                            onTabSelected = { tab ->
                                if (tab == 3) {
                                    currentScreen = "BUSINESS"
                                } else {
                                    currentScreen = "HOME"
                                    homeViewModel.onSelectTab(tab)
                                }
                            }
                        )
                    }
                    "HEALTH" -> {
                        HealthScreen(
                            viewModel = healthViewModel,
                            onNavigateBack = { currentScreen = "HOME" },
                            onTabSelected = { tab ->
                                if (tab == 3) {
                                    currentScreen = "BUSINESS"
                                } else {
                                    currentScreen = "HOME"
                                    homeViewModel.onSelectTab(tab)
                                }
                            }
                        )
                    }
                    "BUSINESS" -> {
                        BusinessScreen(
                            viewModel = businessViewModel,
                            onNavigateBack = { currentScreen = "HOME" },
                            onTabSelected = { tab ->
                                if (tab == 3) {
                                    currentScreen = "BUSINESS"
                                } else {
                                    currentScreen = "HOME"
                                    homeViewModel.onSelectTab(tab)
                                }
                            }
                        )
                    }
                    else -> {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToHabits = { currentScreen = "HABITS" },
                            onNavigateToHealth = { currentScreen = "HEALTH" },
                            onNavigateToBusiness = { currentScreen = "BUSINESS" },
                            onNavigateToCatalogue = {
                                currentScreen = "BUSINESS"
                                businessViewModel.openCatalogue()
                            }
                        )
                    }
                }
            }
        }
    }
}
