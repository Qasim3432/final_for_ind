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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.final_for_ind.R
import com.example.final_for_ind.ui.theme.LightBrown


@Preview(showBackground = true)
@Composable
fun BottomNavBar() {
//  Bottom Nav Items
    val navItems = listOf(
        NavItem(title = "Deposit", icon = R.drawable.deposit),
        NavItem(title = "Wallet", icon = R.drawable.wallet),
        NavItem(title = "Home", icon = R.drawable.regular_outline_home),
        NavItem(title = "Gift", icon = R.drawable.gift),
        NavItem(title = "Profile", icon = R.drawable.outline_account_circle_24),

        )
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(height = 100.dp)
    ) {

        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,


                        )
                },
                label = { Text(text = item.title) },
                modifier = Modifier.size(size = 30.dp),
                onClick = { },
                selected = true,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = LightBrown.copy(alpha = 0.01f)
                )

            )

        }
    }


}

data class NavItem(
    val title: String,
    val icon: Int
)