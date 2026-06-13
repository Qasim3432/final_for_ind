package com.example.final_for_ind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.final_for_ind.screens.component.WithdrawScreen
import com.example.final_for_ind.screens.deposit.DepositScreen
import com.example.final_for_ind.screens.dice_board.LudoGame
import com.example.final_for_ind.screens.login_frame.IntroSec
import com.example.final_for_ind.ui.theme.Final_for_indTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Final_for_indTheme {
                IntroSec()

            }
        }
    }
}
