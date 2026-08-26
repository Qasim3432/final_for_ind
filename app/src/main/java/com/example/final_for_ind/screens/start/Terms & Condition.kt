package com.example.final_for_ind.screens.start

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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

    // 👇 BLACK RED GRADIENT
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A0A), Color(0xFF1A0000))
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (accepted) 1f else 0.98f,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(70.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.4f), Color.Transparent)))
                    .border(2.5.dp, Color(0xFFFFD700), CircleShape), // 👈 GOLDEN BORDER
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(36.dp)) // 👈 GOLDEN
            }

            Spacer(Modifier.height(16.dp))

            Text("Terms & Conditions", color = Color(0xFFFFD700), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp) // 👈 GOLDEN

            Text("Please read carefully before continuing", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(0.9f).shadow(24.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.5.dp, Color(0xFFFFD700)) // 👈 GOLDEN BORDER
            ) {
                Box(
                    modifier = Modifier
                        .background(brush = Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0A0A0A))), shape = RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        TermItem(number = 1, icon = Icons.Default.Security, text = "App use karne se pehle rules parh lein. Rules na manne pe access restrict ho sakta hai.")
                        Spacer(Modifier.height(20.dp))
                        TermItem(number = 2, icon = Icons.Default.Block, text = "Fake data ya OTP share karna sakhti se mana hai. Aisa karne pe account permanent ban.")
                        Spacer(Modifier.height(20.dp))
                        TermItem(number = 3, icon = Icons.Default.MonetizationOn, text = "Coins sirf gameplay aur rewards se milenge. Koi external purchase ya hack allow nahi.")
                        Spacer(Modifier.height(20.dp))
                        TermItem(number = 4, icon = Icons.Default.CheckCircle, text = "Hamara haq hai account ban karne ka agar koi bhi rules toray jaye. Decision final hoga.")
                        Spacer(Modifier.height(24.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f)) // 👈 GOLDEN BG
                                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp)) // 👈 GOLDEN BORDER
                                .padding(12.dp)
                        ) {
                            Text("⚠️ In sharaait ko manzoor kar ke hi app use karen.", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Medium) // 👈 GOLDEN
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.9f).clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2B0000), RoundedCornerShape(12.dp)) // 👈 DARK RED
                    .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(12.dp)) // 👈 GOLDEN
                    .padding(12.dp)
            ) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = { accepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFD32F2F), // 👈 RED
                        uncheckedColor = Color(0xFFFFD700).copy(alpha = 0.5f), // 👈 GOLDEN
                        checkmarkColor = Color.White
                    )
                )
                Text("I have read and accept the Terms & Conditions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(Modifier.height(20.dp))

            // CONTINUE BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f).height(58.dp).scale(buttonScale).clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = if (accepted) Brush.horizontalGradient(listOf(Color(0xFF8B0000), Color(0xFFD32F2F))) // 👈 RED
                        else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp)) // 👈 GOLDEN BORDER
            ) {
                Button(
                    onClick = onAccept,
                    enabled = accepted,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues()
                ) {
                    Text("Continue", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.sp, color = if (accepted) Color.White else Color.White.copy(alpha = 0.5f))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun TermItem(number: Int, icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        // Number Circle
        Box(
            modifier = Modifier
                .size(32.dp).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), CircleShape), // 👈 GOLDEN
            contentAlignment = Alignment.Center
        ) {
            Text(number.toString(), color = Color(0xFF1A0000), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) // 👈 DARK RED TEXT
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp)) // 👈 GOLDEN
            Spacer(Modifier.height(4.dp))
            Text(text = text, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 22.sp)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TermsScreenPreview() {
    TermsScreen(onAccept = {})
}