package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComingSoonNotice
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextDarkSlate
import com.example.ui.theme.TextMutedSlate

@Composable
fun ComingSoonDialog(
    notice: ComingSoonNotice,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("coming_soon_dialog"),
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealPrimary.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Coming Soon • ${notice.stageLabel}",
                        color = TealPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notice.title.replace('\n', ' '),
                    color = TextDarkSlate,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("coming_soon_dialog_title")
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = notice.description,
                    color = TextMutedSlate,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.testTag("coming_soon_dialog_description")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Stage 1 Scope Governance: No file processing, playback, or network action was performed.",
                    color = TextDarkSlate.copy(alpha = 0.78f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("coming_soon_dialog_dismiss")
            ) {
                Text(text = "Got It", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
