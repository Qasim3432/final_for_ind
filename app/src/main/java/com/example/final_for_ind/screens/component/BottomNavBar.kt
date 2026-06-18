package com.example.final_for_ind.screens.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.R

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val navItems = listOf(
        NavItem("Deposit", R.drawable.deposit, "deposit"),
        NavItem("Wallet", R.drawable.wallet, "wallet"),
        NavItem("Home", R.drawable.regular_outline_home, "home"),
        NavItem("Gift", R.drawable.gift, "gift"),
        NavItem("Profile", R.drawable.outline_account_circle_24, "profile"),
    )

    NavigationBar(
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = Modifier
            .height(88.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
                        label = "iconColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
                        label = "textColor"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        label = "scale"
                    )


                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = if (isSelected)
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFFD700).copy(alpha = 0.25f),
                                                    Color.Transparent
                                                ),
                                                radius = 80f
                                            )
                                        else Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Transparent
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(scale),
                                    tint = iconColor
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.3.sp,
                                color = textColor
                            )
                        },
                        selected = isSelected,
                        onClick = { onNavigate(item.route) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Transparent,
                            selectedTextColor = Color.Transparent,
                            unselectedIconColor = Color.Transparent,
                            unselectedTextColor = Color.Transparent,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

data class NavItem(
    val title: String,
    val icon: Int,
    val route: String
)