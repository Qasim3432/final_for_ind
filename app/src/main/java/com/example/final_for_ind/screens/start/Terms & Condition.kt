package com.example.final_for_ind.screens.start

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onAccept: () -> Unit) {
    var accepted by remember { mutableStateOf(false) }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (accepted) 1f else 0.98f,
        animationSpec = tween(200),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Gavel,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Terms & Conditions",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Please read carefully before continuing",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(32.dp))


            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.9f)
                    .shadow(24.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(24.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        TermItem(
                            number = 1,
                            icon = Icons.Default.Security,
                            text = "App use karne se pehle rules parh lein. Rules na manne pe access restrict ho sakta hai."
                        )

                        Spacer(Modifier.height(20.dp))

                        TermItem(
                            number = 2,
                            icon = Icons.Default.Block,
                            text = "Fake data ya OTP share karna sakhti se mana hai. Aisa karne pe account permanent ban."
                        )

                        Spacer(Modifier.height(20.dp))

                        TermItem(
                            number = 3,
                            icon = Icons.Default.MonetizationOn,
                            text = "Coins sirf gameplay aur rewards se milenge. Koi external purchase ya hack allow nahi."
                        )

                        Spacer(Modifier.height(20.dp))

                        TermItem(
                            number = 4,
                            icon = Icons.Default.CheckCircle,
                            text = "Hamara haq hai account ban karne ka agar koi bhi rules toray jaye. Decision final hoga."
                        )

                        Spacer(Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                .border(
                                    1.dp,
                                    Color(0xFFFFD700).copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⚠️ In sharaait ko manzoor kar ke hi app use karen.",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = { accepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF38ef7d),
                        uncheckedColor = Color.White.copy(alpha = 0.5f),
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = "I have read and accept the Terms & Conditions",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(58.dp)
                    .scale(buttonScale)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = if (accepted)
                            Brush.horizontalGradient(listOf(Color(0xFF38ef7d), Color(0xFF11998e)))
                        else
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Gray.copy(alpha = 0.3f),
                                    Color.Gray.copy(alpha = 0.3f)
                                )
                            ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        2.dp,
                        if (accepted) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Button(
                    onClick = onAccept,
                    enabled = accepted,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues()
                ) {
                    Text(
                        "Continue",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = if (accepted) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun TermItem(number: Int, icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                color = Color(0xFF1A252F),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(12.dp))


        Column(modifier = Modifier.weight(1f)) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsScreenPreview() {
    TermsScreen(onAccept = {})
}