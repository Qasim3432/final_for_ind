package com.example.final_for_ind.screens.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class NavItem(
    val title: String,
    val icon: ImageVector,
    val index: Int
)

@Composable
fun BottomNavBar(
    selectedIndex: Int = 0,
    onItemClick: (Int) -> Unit = {}
) {
    val navItems = listOf(
        NavItem("Home", Icons.Default.Home, 0),
        NavItem("Wallet", Icons.Default.Wallet, 1),
        NavItem("Gift", Icons.Default.CardGiftcard, 3),
        NavItem("Profile", Icons.Default.Person, 2),
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
                    val isSelected = selectedIndex == item.index

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
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
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
                        onClick = { onItemClick(item.index) },
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