package com.example.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSCard
import com.example.ui.theme.MindTheme
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val initialY: Float,
    val size: Float,
    val speed: Float,
    val isYellow: Boolean
)

@Composable
fun ReadyScreen(
    onStartJourney: () -> Unit,
    modifier: Modifier = Modifier,
    onboardingViewModel: OnboardingViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val userProfile by onboardingViewModel.userProfile.collectAsState()

    val checkScale = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        checkScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f)
        )
    }

    // 50 Confetti particles animated
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat(),
                initialY = Random.nextFloat(),
                size = Random.nextFloat() * 6f + 4f,
                speed = Random.nextFloat() * 0.4f + 0.2f,
                isYellow = Random.nextBoolean()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti_fall")
    val fallProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .testTag("ready_screen")
    ) {
        // Confetti Canvas (Top 55%)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .align(Alignment.TopCenter)
        ) {
            particles.forEach { p ->
                val currentY = ((p.initialY + fallProgress * p.speed) % 1f) * size.height
                val currentX = p.x * size.width
                val color = if (p.isYellow) mindColors.primary else Color.White

                drawRect(
                    color = color.copy(alpha = 0.85f),
                    topLeft = Offset(currentX, currentY),
                    size = Size(p.size, p.size * 1.5f)
                )
            }
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Big Checkmark in circular primary container (80dp)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(checkScale.value)
                    .clip(CircleShape)
                    .background(mindColors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = mindColors.textInverse,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.ready_title),
                style = mindTypography.largeTitle,
                color = mindColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.ready_subtitle),
                style = mindTypography.body,
                color = mindColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Summary IOSCard (SOLID, NOT glass, #1C1C1F dark / #FFFFFF light)
            IOSCard(modifier = Modifier.fillMaxWidth()) {
                SummaryRow(
                    label = stringResource(id = R.string.ready_summary_language),
                    value = if (userProfile.language == "ar") "العربية" else "English"
                )
                HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorThin)

                SummaryRow(
                    label = stringResource(id = R.string.ready_summary_name),
                    value = userProfile.userName.ifBlank { "—" }
                )
                HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorThin)

                SummaryRow(
                    label = stringResource(id = R.string.ready_summary_goal),
                    value = userProfile.userGoal.ifBlank { "—" }
                )
                HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorThin)

                SummaryRow(
                    label = stringResource(id = R.string.ready_summary_time),
                    value = "${userProfile.userDailyMinutes} " + (if (userProfile.language == "ar") "دقيقة" else "min")
                )
            }
        }

        // Bottom Fixed Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            IOSButton(
                text = stringResource(id = R.string.ready_start),
                onClick = {
                    onboardingViewModel.completeOnboarding(onStartJourney)
                },
                style = IOSButtonStyle.Filled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = mindTypography.footnote,
            color = mindColors.textSecondary
        )
        Text(
            text = value,
            style = mindTypography.body.copy(fontWeight = FontWeight.Medium),
            color = mindColors.textPrimary
        )
    }
}

@Preview(name = "Ready Screen Dark", showBackground = true)
@Composable
fun ReadyScreenPreview() {
    MindTheme(darkTheme = true) {
        ReadyScreen(onStartJourney = {})
    }
}
