package com.example.final_for_ind.screens.home_lobby


import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.final_for_ind.ui.theme.RandomOrange


@Preview(showBackground = true)
@Composable
fun LobbyPoP() {


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .background(color = RandomOrange)
                .size(height = 30.dp, width = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "2 Players", fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Card(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth() // This makes the Row fill the entire Card
                    .padding(top = 50.dp, end = 20.dp ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center // This centers the items horizontally
            ) {
                Box(
                    modifier = Modifier.clickable(onClick = {})
                        .size(40.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Minimize,
                        contentDescription = "person",
                        modifier = Modifier.size(34.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.size(15.dp))


                Box(modifier = Modifier.size(height = 30.dp, width = 170.dp).background(color = DarkGray))

                Spacer(modifier = Modifier.size(15.dp))

                Box(
                    modifier = Modifier.clickable(onClick = {})
                        .size(40.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "person",
                        modifier = Modifier.size(34.dp),
                        tint = Color.Black
                    )
                }


            }

            Spacer(modifier = Modifier.size(15.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(16.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "PLAY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }


        }

    }


}




