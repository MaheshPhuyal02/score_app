package com.tm.score_app.pages

import AppBottomNavigation
import BottomNavItem
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.tm.score_app.pages.fragments.HomePage
import com.tm.score_app.pages.fragments.UserPage
import com.tm.score_app.pages.fragments.WatchPage
import com.tm.score_app.ui.theme.Score_appTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            Score_appTheme {
                var selectedItem by remember { mutableStateOf(BottomNavItem.HOME) }

                Scaffold(
                    bottomBar = {
                        AppBottomNavigation(
                            selectedItem = selectedItem,
                            onItemSelected = { selectedItem = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Color.Black)
                            .fillMaxSize()
                    ) {
                        // Your page content here
                        when (selectedItem) {
                            BottomNavItem.HOME -> HomePage()
                            BottomNavItem.USER -> UserPage(context = this@HomeActivity)
                            BottomNavItem.WATCH -> WatchPage()
                        }
                    }
                }
            }
        }
    }
}