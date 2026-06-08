package com.example.final_for_ind.screens.home_lobby


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.R
import com.example.final_for_ind.screens.component.BottomNavBar
import com.example.final_for_ind.ui.theme.Orange
import com.example.final_for_ind.ui.theme.Purple40


@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    Scaffold(
        bottomBar = { BottomNavBar() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(60.dp)
                        .background(
                            color = Color.LightGray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                )
                {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "person",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )

                }
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .height(30.dp)
                        .width(100.dp)
                ) { }

            }

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Go to setting",
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 30.dp, end = 24.dp)
                    .clickable(onClick = {}),

                )


            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // First Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    // Box 1 (existing)
                    Box(
                        modifier = Modifier
                            .clickable(onClick = {})
                            .background(color = Green, shape = RoundedCornerShape(12.dp))
                            .size(width = 150.dp, height = 150.dp)
                            .border(width = 4.dp, color = White, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {

                           Text(
                               text = "2 Players",
                               color = White,
                               fontSize = 16.sp,
                               fontWeight = FontWeight.Bold,
                               textAlign = TextAlign.End,
                               modifier = Modifier

                           )

                    }

                    // Box 2 (new)
                    Box(
                        modifier = Modifier
                            .clickable(onClick = {})
                            .background(color = Blue, shape = RoundedCornerShape(12.dp))
                            .size(width = 150.dp, height = 150.dp)
                            .border(width = 4.dp, color = White, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "4 Players",
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier

                        )

                    }
                }

                // Second Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    // Box 3 (new)
                    Box(
                        modifier = Modifier
                            .clickable(onClick = {})
                            .background(color = Orange, shape = RoundedCornerShape(12.dp))
                            .size(width = 150.dp, height = 150.dp)
                            .border(width = 4.dp, color = White, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Friends",
                            color = White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier

                        )
                    }

                    // Box 4 (new)
                    Box(
                        modifier = Modifier
                            .clickable(onClick = {})
                            .background(color = Purple40, shape = RoundedCornerShape(12.dp))
                            .size(width = 150.dp, height = 150.dp)
                            .border(width = 4.dp, color = White, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Computer",
                            color = White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier

                        )
                    }
                }
            }
        }
    }
}