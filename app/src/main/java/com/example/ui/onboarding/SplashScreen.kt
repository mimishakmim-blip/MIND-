package com.example.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.PreferencesModule
import com.example.ui.theme.MindTheme
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    var startLogoAnim by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startLogoAnim) 1.0f else 0.8f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startLogoAnim) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "logo_alpha"
    )

    // Infinite pulsing glow around the logo
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    LaunchedEffect(Unit) {
        startLogoAnim = true
        delay(300)
        showTitle = true
        delay(400)
        showTagline = true
        delay(1500) // Total delay ~2200ms

        val userPrefs = PreferencesModule.provideUserPreferences(context)
        val profile = userPrefs.getUserProfile()
        if (profile.onboardingCompleted) {
            onNavigateToMain()
        } else {
            onNavigateToWelcome()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo container with pulsating glow
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft radial glow
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha * logoAlpha)
                        .background(
                            Brush.radialGradient(
                                listOf(mindColors.primaryGlow, Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                // 120dp Centered Logo
                Image(
                    painter = painterResource(id = R.drawable.mind_logo),
                    contentDescription = "MIND Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "MIND"
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(animationSpec = tween(400))
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = mindTypography.display.copy(fontWeight = FontWeight.Bold),
                    color = mindColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline: "عقلك، وقتك، حياتك"
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(animationSpec = tween(400))
            ) {
                Text(
                    text = stringResource(id = R.string.app_tagline),
                    style = mindTypography.footnote,
                    color = mindColors.textSecondary
                )
            }
        }
    }
}

@Preview(name = "Splash Screen Dark", showBackground = true)
@Composable
fun SplashScreenPreview() {
    MindTheme(darkTheme = true) {
        SplashScreen(onNavigateToWelcome = {}, onNavigateToMain = {})
    }
}
