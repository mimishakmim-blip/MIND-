package com.example.ui.profile.components

import com.example.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSCard
import com.example.ui.theme.mindColors
import java.util.Calendar

@Composable
fun ProfileHeaderCard(
    name: String,
    memberSince: Long,
    avatarUri: String?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(mindColors.primary.copy(alpha = 0.15f))
                    .border(2.dp, mindColors.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "M",
                        color = mindColors.primary,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = name.ifBlank { tr("صديقي", "friend") },
                color = mindColors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.profile_member_since,
                    formatMemberSince(memberSince)
                ),
                color = mindColors.textSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            IOSButton(
                text = stringResource(R.string.profile_edit),
                onClick = onEdit,
                style = IOSButtonStyle.Glass,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

private fun formatMemberSince(millis: Long): String {
    if (millis == 0L) return tr("اليوم", "Today")
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val monthNames = listOf(
        tr("يناير", "January"), tr("فبراير", "February"), tr("مارس", "March"), tr("أبريل", "April"), tr("مايو", "May"), tr("يونيو", "June"),
        tr("يوليو", "July"), tr("أغسطس", "August"), tr("سبتمبر", "September"), tr("أكتوبر", "October"), tr("نوفمبر", "November"), tr("ديسمبر", "December")
    )
    val month = monthNames[cal.get(Calendar.MONTH)]
    val year = cal.get(Calendar.YEAR)
    return "$month $year"
}
