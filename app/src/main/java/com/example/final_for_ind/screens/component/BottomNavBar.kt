package com.example.final_for_ind.screens.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
        containerColor = Color(0xFF1A252F), // tumhari gradient ka end color
        modifier = Modifier.height(80.dp)
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = item.title, fontSize = 12.sp) },
                onClick = { onNavigate(item.route) },
                selected = isSelected,
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD700), // gold highlight
                    selectedTextColor = Color(0xFFFFD700),
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f),
                    indicatorColor = Color(0xFF9B59B6).copy(alpha = 0.2f) // purple highlight
                )
            )
        }
    }
}

data class NavItem(
    val title: String,
    val icon: Int,
    val route: String
)