package com.example.final_for_ind

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.final_for_ind.screens.home_lobby.HomeScreen
import com.example.final_for_ind.screens.dice_board.LudoGame
import com.example.final_for_ind.screens.home_lobby.GameSetupDialog
import com.example.final_for_ind.screens.login_frame.First_Screen
import com.example.final_for_ind.screens.login_frame.IntroSec
import com.example.final_for_ind.screens.login_frame.LoginVari
import com.example.final_for_ind.screens.profile.ProfileScreen
import com.example.final_for_ind.screens.start.SettingScreen
import com.example.final_for_ind.screens.start.SplashScreen
import com.example.final_for_ind.screens.start.TermsScreen
import com.example.final_for_ind.screens.deposit.DepositScreen
import com.example.final_for_ind.screens.component.DashboardScreen
import com.example.final_for_ind.screens.component.GiftSpinScreen
import com.example.final_for_ind.screens.component.WithdrawScreen
import com.example.final_for_ind.screens.component.InternationalPayment
import com.example.final_for_ind.ui.theme.Final_for_indTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Final_for_indTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

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

                    composable(
                        "first?loggedOut={loggedOut}",
                        arguments = listOf(navArgument("loggedOut") {
                            type = NavType.BoolType
                            defaultValue = false
                        })
                    ) { backStackEntry ->
                        val loggedOut = backStackEntry.arguments?.getBoolean("loggedOut") ?: false
                        First_Screen(
                            onPlayClick = { navController.navigate("intro") },
                            onJoinCodeClick = {
                                Toast.makeText(context, "Coming Soon", Toast.LENGTH_SHORT).show()
                            },
                            showLogoutMessage = loggedOut
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
                            onVerify = { navController.navigate("terms") }
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
                        var showCoinsDialog by remember { mutableStateOf(false) }

                        HomeScreen(
                            coins = 1250,
                            onProfileClick = { navController.navigate("profile") },
                            onSettingsClick = { navController.navigate("settings") },
                            onGameModeClick = { mode, _ ->
                                Log.d("GAME_MODE", "Selected: $mode")
                                if (mode == "2P" || mode == "4P") {
                                    navController.navigate("setup/$mode")
                                } else {
                                    navController.navigate("game/$mode/0")
                                }
                            },
                            onCoinsClick = { showCoinsDialog = true },
                            onNavItemClick = { route ->
                                if (route != "home") navController.navigate(route)
                            }
                        )

                        if (showCoinsDialog) {
                            AlertDialog(
                                onDismissRequest = { showCoinsDialog = false },
                                containerColor = Color(0xFF2C3E50),
                                title = {
                                    Text("Your Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                },
                                text = {
                                    Column {
                                        Text("Total: 1250 💰", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(12.dp))
                                        Text("• Won: 500 coins", color = Color(0xFF25D366))
                                        Text("• Bonus: 750 coins", color = Color(0xFFF39C12))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Join premium matches with coins!",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 14.sp)
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showCoinsDialog = false }) {
                                        Text("OK", color = Color.White)
                                    }
                                }
                            )
                        }
                    }

                    // Ye naya route add kiya lobby ke liye
                    composable(
                        "setup/{mode}",
                        arguments = listOf(navArgument("mode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: "2P"
                        GameSetupDialog(
                            mode = mode,
                            coins = 1250,
                            onDismiss = { navController.popBackStack() },
                            onStartGame = { m, bet ->
                                navController.navigate("game/$m/$bet") {
                                    popUpTo("setup/$mode") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("wallet") {
                        DashboardScreen(
                            balance = 1250,
                            onNavItemClick = { route ->
                                if (route != "wallet") navController.navigate(route)
                            },
                            onAddCoinsByUSDT = { navController.navigate("international_payment") },
                            onDeposit = { navController.navigate("deposit") },
                            onWithdraw = { navController.navigate("withdraw") }
                        )
                    }

                    composable("international_payment") {
                        InternationalPayment(
                            onBack = { navController.popBackStack() },
                            onProceed = { amount, method ->
                                Log.d("USDT", "Proceed $amount USDT via $method")
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("deposit") {
                        DepositScreen(
                            currentBalance = 1250,
                            onBack = { navController.popBackStack() },
                            onDeposit = { amount ->
                                Log.d("DEPOSIT", "Deposit $amount coins")
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("withdraw") {
                        WithdrawScreen(
                            currentBalance = 1250,
                            onBack = { navController.popBackStack() },
                            onWithdraw = { amount ->
                                Log.d("WITHDRAW", "Withdraw $amount coins")
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("gift") {
                        GiftSpinScreen(
                            currentCoins = 1250,
                            onBack = { navController.popBackStack() },
                            onRewardClaimed = { coins -> Log.d("GIFT", "Won $coins coins") }
                        )
                    }

                    // Game route ab mode + bet lega
                    composable(
                        "game/{mode}/{bet}",
                        arguments = listOf(
                            navArgument("mode") { type = NavType.StringType },
                            navArgument("bet") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: "2P"
                        val bet = backStackEntry.arguments?.getInt("bet") ?: 0
                        LudoGame(mode = mode, betAmount = bet)
                    }

                    composable("settings") {
                        SettingScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToProfile = { navController.navigate("profile") },
                            onLogout = {
                                Log.d("BACKEND_TODO", "Clear session & navigate to login")
                                navController.navigate("first?loggedOut=true") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            name = "John Doe",
                            coins = 1250,
                            onBack = { navController.popBackStack() },
                            onBetHistory = { Log.d("BACKEND_TODO", "Open Bet History Screen") },
                            onLogout = {
                                Log.d("BACKEND_TODO", "Clear session & navigate to login")
                                navController.navigate("first?loggedOut=true") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}