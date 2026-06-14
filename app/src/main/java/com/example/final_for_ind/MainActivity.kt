package com.example.final_for_ind

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.final_for_ind.screens.home_lobby.HomeScreen
import com.example.final_for_ind.screens.dice_board.LudoGame
import com.example.final_for_ind.screens.login_frame.First_Screen
import com.example.final_for_ind.screens.login_frame.IntroSec
import com.example.final_for_ind.screens.login_frame.LoginVari
import com.example.final_for_ind.screens.start.SettingScreen
import com.example.final_for_ind.screens.start.SplashScreen
import com.example.final_for_ind.screens.start.TermsScreen
import com.example.final_for_ind.ui.theme.Final_for_indTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Final_for_indTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen {
                            navController.navigate("first") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }

                    composable("first") {
                        First_Screen(
                            onPlayClick = { navController.navigate("intro") },
                            onJoinCodeClick = { navController.navigate("intro") }
                        )
                    }

                    composable("intro") {
                        IntroSec(
                            onSubmit = { name, phone ->
                                Log.d("BACKEND_TODO", "Send OTP to $phone for $name")
                                navController.navigate("verify/$phone")
                            }
                        )
                    }

                    composable(
                        "verify/{phone}",
                        arguments = listOf(navArgument("phone") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val phone = backStackEntry.arguments?.getString("phone") ?: ""
                        LoginVari(
                            phoneNumber = phone,
                            onVerify = {
                                navController.navigate("terms")
                            }
                        )
                    }

                    composable("terms") {
                        TermsScreen(
                            onAccept = {
                                Log.d("BACKEND_TODO", "Save user & mark T&C agreed")
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            coins = 1250,
                            onProfileClick = {
                                Log.d("BACKEND_TODO", "Open Profile Screen")
                            },
                            onSettingsClick = { navController.navigate("settings") },
                            onGameModeClick = { mode ->
                                Log.d("GAME_MODE", "Selected: $mode")
                                navController.navigate("game")
                            }
                        )
                    }

                    composable("game") { LudoGame() }
                    composable("settings") { SettingScreen(onBack = { navController.popBackStack() }) }
                }
            }
        }
    }
}