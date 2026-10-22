package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import com.example.ui.MediMindViewModel
import com.example.ui.NavTab
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.InteractionsScreen
import com.example.ui.screens.MedicinesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.MediMindTheme
import com.example.ui.util.Localization

class MainActivity : ComponentActivity() {

    private val viewModel: MediMindViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            val largeText by viewModel.largeTextEnabled.collectAsState()

            MediMindTheme(darkTheme = isDark) {
                val currentDensity = LocalDensity.current
                val densityOverride = if (largeText) {
                    Density(currentDensity.density, fontScale = currentDensity.fontScale * 1.25f)
                } else currentDensity

                CompositionLocalProvider(LocalDensity provides densityOverride) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MediMindViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val language by viewModel.language.collectAsState()

    // BackHandler returns to TODAY if on other tab
    BackHandler(enabled = currentTab != NavTab.TODAY) {
        viewModel.setTab(NavTab.TODAY)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == NavTab.TODAY,
                    onClick = { viewModel.setTab(NavTab.TODAY) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = Localization.getString("nav_today", language)) },
                    label = { Text(Localization.getString("nav_today", language)) },
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.MEDICINES,
                    onClick = { viewModel.setTab(NavTab.MEDICINES) },
                    icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = Localization.getString("nav_medicines", language)) },
                    label = { Text(Localization.getString("nav_medicines", language)) },
                    modifier = Modifier.testTag("nav_tab_medicines")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.INTERACTIONS,
                    onClick = { viewModel.setTab(NavTab.INTERACTIONS) },
                    icon = { Icon(Icons.Default.Warning, contentDescription = Localization.getString("nav_interactions", language)) },
                    label = { Text(Localization.getString("nav_interactions", language)) },
                    modifier = Modifier.testTag("nav_tab_interactions")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.REPORTS,
                    onClick = { viewModel.setTab(NavTab.REPORTS) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = Localization.getString("nav_reports", language)) },
                    label = { Text(Localization.getString("nav_reports", language)) },
                    modifier = Modifier.testTag("nav_tab_reports")
                )

                NavigationBarItem(
                    selected = currentTab == NavTab.ASSISTANT,
                    onClick = { viewModel.setTab(NavTab.ASSISTANT) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = Localization.getString("nav_assistant", language)) },
                    label = { Text(Localization.getString("nav_assistant", language)) },
                    modifier = Modifier.testTag("nav_tab_assistant")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                when (tab) {
                    NavTab.TODAY -> TodayScreen(viewModel = viewModel)
                    NavTab.MEDICINES -> MedicinesScreen(viewModel = viewModel)
                    NavTab.INTERACTIONS -> InteractionsScreen(viewModel = viewModel)
                    NavTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                    NavTab.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                }
            }
        }
    }
}
