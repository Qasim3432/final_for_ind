package com.example.final_for_ind

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.final_for_ind.screens.referral.CreateRoomScreen
import com.example.final_for_ind.screens.referral.InviteScreen
import com.example.final_for_ind.screens.component.DashboardScreen
import com.example.final_for_ind.screens.component.GiftSpinScreen
import com.example.final_for_ind.screens.component.InternationalPayment
import com.example.final_for_ind.screens.component.WithdrawScreen
import com.example.final_for_ind.screens.deposit.DepositScreen
import com.example.final_for_ind.screens.dice_board.LudoGame
import com.example.final_for_ind.screens.home_lobby.FourPlayerDialog // 👈 Updated import
import com.example.final_for_ind.screens.home_lobby.TwoPlayerDialog // 👈 Updated import
import com.example.final_for_ind.screens.home_lobby.HomeScreen
import com.example.final_for_ind.screens.login_frame.First_Screen
import com.example.final_for_ind.screens.login_frame.IntroSec
import com.example.final_for_ind.screens.login_frame.LoginVari
import com.example.final_for_ind.screens.profile.ProfileScreen
import com.example.final_for_ind.screens.referral.JoinReferralScreen
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
                val context = LocalContext.current
                var userCoins by remember { mutableStateOf(1250) }

                NavHost(navController = navController, startDestination = "splash") {

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
                            onJoinCodeClick = { navController.navigate("join") },
                            showLogoutMessage = loggedOut
                        )
                    }

                    composable("join") {
                        JoinReferralScreen(
                            bonusCoins = 50,
                            onReferralApplied = { code ->
                                Log.d("REFERRAL", "Code entered: $code")
                                userCoins += 50
                                Toast.makeText(
                                    context,
                                    "Referral code: $code applied +50 coins",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("intro") {
                                    popUpTo("join") { inclusive = true }
                                }
                            },
                            onSkip = {
                                navController.navigate("intro") {
                                    popUpTo("join") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("intro") {
                        IntroSec(onSubmit = { name, phone ->
                            Log.d("BACKEND_TODO", "Send OTP to $phone for $name")
                            navController.navigate("verify/$phone")
                        })
                    }

                    composable(
                        "verify/{phone}",
                        arguments = listOf(navArgument("phone") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val phone = backStackEntry.arguments?.getString("phone") ?: ""
                        LoginVari(
                            phoneNumber = phone,
                            onVerify = { navController.navigate("terms") })
                    }

                    composable("terms") {
                        TermsScreen(onAccept = {
                            Log.d("BACKEND_TODO", "Save user & mark T&C agreed")
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }

                    composable("home") {
                        var showCoinsDialog by remember { mutableStateOf(false) }

                        HomeScreen(
                            coins = userCoins,
                            onProfileClick = { navController.navigate("profile") },
                            onSettingsClick = { navController.navigate("settings") },
                            onGameModeClick = { mode, bet ->
                                Log.d("GAME_MODE", "Selected: $mode, Bet: $bet")
                                if (bet > 0) {
                                    // Dialog se bet aa gaya, direct game me bhejo
                                    if (userCoins >= bet) {
                                        userCoins -= bet
                                        navController.navigate("game/$mode/$bet") {
                                            popUpTo("home") { inclusive = false }
                                        }
                                    } else {
                                        Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Bet 0 hai to dialog khole ga
                                    if (mode == "2P" || mode == "4P") {
                                        navController.navigate("setup/$mode")
                                    } else {
                                        navController.navigate("game/$mode/0")
                                    }
                                }
                            },
                            onCoinsClick = { showCoinsDialog = true },
                            onNavItemClick = { route ->
                                if (route != "home") navController.navigate(route)
                            },
                            onFriendsClick = { navController.navigate("create_room/Ali Khan") }
                        )

                        if (showCoinsDialog) {
                            AlertDialog(
                                onDismissRequest = { showCoinsDialog = false },
                                containerColor = Color(0xFF2C3E50),
                                title = {
                                    Text(
                                        "Your Coins",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                },
                                text = {
                                    Column {
                                        Text(
                                            "Total: $userCoins 💰",
                                            color = Color.White,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text("• Won: 500 coins", color = Color(0xFF25D366))
                                        Text(
                                            "• Bonus: ${userCoins - 500} coins",
                                            color = Color(0xFF39C12F)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Join premium matches with coins!",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 14.sp
                                        )
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

                    composable(
                        "create_room/{friendName}",
                        arguments = listOf(navArgument("friendName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val friendName =
                            backStackEntry.arguments?.getString("friendName") ?: "Friend"
                        CreateRoomScreen(
                            friendName = friendName,
                            onBack = { navController.popBackStack() },
                            onCreateLink = {
                                Log.d("ROOM", "Room create logic here")
                                Toast.makeText(context, "Room Created!", Toast.LENGTH_SHORT).show()
                            },
                            onShareLink = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Join karo Ludo Room! Link: yourapp://room/ABC123 🎮"
                                    )
                                }
                                startActivity(Intent.createChooser(sendIntent, "Share Room Link"))
                            },
                            onCopyLink = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip =
                                    ClipData.newPlainText("Room Link", "yourapp://room/ABC123")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Link Copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // 👇 UPDATED: Ab mode ke hisaab se dialog khule ga
                    composable(
                        "setup/{mode}",
                        arguments = listOf(navArgument("mode") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: "2P"

                        if (mode == "2P") {
                            TwoPlayerDialog(
                                gameMode = "2P",
                                walletBalance = userCoins,
                                onDismiss = { navController.popBackStack() },
                                onStartGame = { gameMode, bet ->
                                    if (userCoins >= bet) {
                                        userCoins -= bet
                                        navController.navigate("game/$gameMode/$bet") {
                                            popUpTo("setup/$mode") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } else {
                            FourPlayerDialog(
                                walletBalance = userCoins,
                                onDismiss = { navController.popBackStack() },
                                onPlayBet = { bet ->
                                    if (userCoins >= bet) {
                                        userCoins -= bet
                                        navController.navigate("game/4P/$bet") {
                                            popUpTo("setup/$mode") { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    composable("wallet") {
                        DashboardScreen(
                            balance = userCoins,
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
                                userCoins += amount
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("deposit") {
                        DepositScreen(
                            currentBalance = userCoins,
                            onBack = { navController.popBackStack() },
                            onDeposit = { amount ->
                                Log.d("DEPOSIT", "Deposit $amount coins")
                                userCoins += amount
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("withdraw") {
                        WithdrawScreen(
                            currentBalance = userCoins,
                            onBack = { navController.popBackStack() },
                            onWithdraw = { amount ->
                                if (userCoins >= amount) {
                                    Log.d("WITHDRAW", "Withdraw $amount coins")
                                    userCoins -= amount
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Insufficient balance",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }

                    composable("gift") {
                        GiftSpinScreen(
                            currentCoins = userCoins,
                            onBack = { navController.popBackStack() },
                            onRewardClaimed = { coins ->
                                userCoins += coins
                                Log.d("GIFT", "Won $coins coins")
                            }
                        )
                    }

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
                            onNavigateToInvite = { navController.navigate("invite") },
                            onLogout = {
                                Log.d("BACKEND_TODO", "Clear session & navigate to login")
                                navController.navigate("first?loggedOut=true") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("invite") {
                        InviteScreen(
                            userReferralCode = "AB7X9K",
                            invitedCount = 3,
                            targetCount = 5,
                            onShare = { code ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Join karo Ludo App! Mera referral code: $code. 50 coins bonus mile ga 🎁"
                                    )
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "WhatsApp install nahi hai",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            name = "John Doe",
                            coins = userCoins,
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