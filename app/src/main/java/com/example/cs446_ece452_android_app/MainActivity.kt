package com.example.cs446_ece452_android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cs446_ece452_android_app.ui.screens.DestinationInputScreen
import com.example.cs446_ece452_android_app.ui.theme.CS446ECE452_Android_appTheme
import com.example.cs446_ece452_android_app.ui.screens.LoginScreen
import com.example.cs446_ece452_android_app.ui.screens.MapScreen
import com.example.cs446_ece452_android_app.ui.screens.ProfileScreen
import com.example.cs446_ece452_android_app.ui.screens.ResetPassword
import com.example.cs446_ece452_android_app.ui.screens.SavedRoutes
import com.example.cs446_ece452_android_app.ui.screens.SignupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            window.statusBarColor = getColor(R.color.black)

            CS446ECE452_Android_appTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "Login"
                ) {
                    composable(route = "Login") {
                        LoginScreen(navController)
                    }
                    composable(route = "Resetpassword") {
                        ResetPassword(navController)
                    }
                    composable(route = "Signup") {
                        SignupScreen(navController)
                    }
                    composable(route = "routes") {
                        SavedRoutes(navController = navController)
                    }
                    composable(route = "Profile") {
                        ProfileScreen(navController = navController)
                    }
                    composable(route = "DestinationInput") {
                        DestinationInputScreen(navController = navController)
                    }
                    composable(route = "Map") {
                        MapScreen(navController = navController)
                    }
                }
            }
        }
    }
}
