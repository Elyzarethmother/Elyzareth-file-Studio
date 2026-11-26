package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComingSoonNotice
import com.example.ui.theme.MiniPlayerBorderCyan
import com.example.ui.theme.MiniPlayerGradientEnd
import com.example.ui.theme.MiniPlayerGradientStart
import com.example.ui.theme.MiniPlayerInactiveIcon
import com.example.ui.theme.TealAccentCyan

@Composable
fun MiniPlayerPlaceholder(
    onShowComingSoon: (ComingSoonNotice) -> Unit,
    modifier: Modifier = Modifier
) {
    val openMiniPlayerNotice = {
        onShowComingSoon(
            ComingSoonNotice(
                id = "mini_player_placeholder",
                title = "Elyzareth Music Mini-Player",
                stageLabel = "Stage 4 — Media3 Background Playback (Coming Soon)",
                description = "This docked mini-player is an inactive visual placeholder in Stage 1. Media3 ExoPlayer session management and background audio playback are scheduled for Stage 4."
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MiniPlayerGradientStart, MiniPlayerGradientEnd)
                )
            )
            .border(1.dp, MiniPlayerBorderCyan, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = openMiniPlayerNotice)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("mini_player_placeholder")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Mini-Player Placeholder",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Inactive Artwork Placeholder Thumbnail
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF1F4E5B), Color(0xFF3E2E4F), Color(0xFF7B4A3A))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Inactive Track Metadata Labels
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Elyzareth Music — Coming Soon",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("mini_player_title")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "♪ Audio Player • Inactive Placeholder (Stage 4)",
                    color = MiniPlayerInactiveIcon,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("mini_player_subtitle")
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Inactive Transport Controls Visual Shell
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite (Inactive)",
                    tint = MiniPlayerInactiveIcon,
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track (Inactive)",
                    tint = MiniPlayerInactiveIcon,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF136F7D))
                        .border(1.5.dp, TealAccentCyan.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play (Coming Soon)",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track (Inactive)",
                    tint = MiniPlayerInactiveIcon,
                    modifier = Modifier.size(20.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Playlist (Inactive)",
                    tint = MiniPlayerInactiveIcon,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Neutral Static Track Row (No LinearProgressIndicator or fake progress)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "--:--",
                color = MiniPlayerInactiveIcon,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .testTag("mini_player_static_track")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "--:--",
                color = MiniPlayerInactiveIcon,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = MiniPlayerInactiveIcon,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.White.copy(alpha = 0.16f))
            )
        }
    }
}
