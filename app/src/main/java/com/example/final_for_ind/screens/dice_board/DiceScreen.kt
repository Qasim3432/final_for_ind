package com.example.final_for_ind.screens.dice_board

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.final_for_ind.R
import kotlin.random.Random

@Preview
@Composable
fun DiceScreen() {
    DiceRollerScreen(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun DiceRollerScreen(modifier: Modifier) {
    var result by remember { mutableStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }

    // Dice rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 720f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "diceRotation"
    )

    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = "Dice $result",
            modifier = Modifier
                .graphicsLayer { rotationZ = rotation }
                .clickable(enabled = !isRolling) {
                    isRolling = true
                    result = Random.nextInt(1, 7)
                    isRolling = false
                }
        )
    }
}