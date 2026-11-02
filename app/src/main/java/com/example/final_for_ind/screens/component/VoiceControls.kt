package com.example.final_for_ind.screens.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.final_for_ind.network.VoiceState

@Composable
fun VoiceCallButton(
    voiceState: VoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = voiceState is VoiceState.Connected
    val isJoining = voiceState is VoiceState.Joining

    val bgColor = when {
        isActive -> Color(0xFF43A047)
        isJoining -> Color(0xFFFFA000)
        else -> Color(0xFFFFD700)
    }

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(bgColor, bgColor.copy(alpha = 0.7f))
                )
            )
            .border(2.dp, Color(0xFF1A0000), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isJoining) {
            CircularProgressIndicator(
                color = Color(0xFF1A0000),
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice",
                tint = Color(0xFF1A0000),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun VoiceControlsPanel(
    voiceState: VoiceState,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = voiceState is VoiceState.Connected,
        enter = fadeIn(tween(200)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(250)
        ),
        exit = fadeOut(tween(200)) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        val connected = voiceState as? VoiceState.Connected ?: return@AnimatedVisibility

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A0000)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (connected.remoteUserCount > 0) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF43A047)))
                } else {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFFA000)))
                }

                VoiceControlIcon(
                    icon = if (connected.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    tint = if (connected.isMuted) Color(0xFFD32F2F) else Color(0xFF43A047),
                    onClick = onToggleMute
                )

                VoiceControlIcon(
                    icon = if (connected.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    tint = if (connected.isSpeakerOn) Color(0xFF42A5F5) else Color.Gray,
                    onClick = onToggleSpeaker
                )

                VoiceControlIcon(
                    icon = Icons.Default.CallEnd,
                    tint = Color(0xFFD32F2F),
                    onClick = onEndCall
                )
            }
        }
    }
}

@Composable
private fun VoiceControlIcon(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF2B0000))
            .border(1.5.dp, tint.copy(alpha = 0.5f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}