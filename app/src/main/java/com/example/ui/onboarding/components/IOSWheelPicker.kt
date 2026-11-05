package com.example.ui.onboarding.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IOSWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    label: String? = null
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val view = LocalView.current
    val itemHeight = 56.dp

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val currentCenterIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)) }
    }

    LaunchedEffect(currentCenterIndex) {
        if (currentCenterIndex != selectedIndex) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            onSelect(currentCenterIndex)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (label != null) {
            Text(
                text = label,
                style = mindTypography.footnote,
                color = mindColors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .testTag("ios_wheel_picker"),
            contentAlignment = Alignment.Center
        ) {
            // Center Selection Highlight Bar / Lens
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(mindColors.surfaceHighlight.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
            )

            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top padding item so the first item can be centered
                item {
                    Spacer(modifier = Modifier.height((height - itemHeight) / 2))
                }

                itemsIndexed(items) { index, itemText ->
                    val distance = index - currentCenterIndex
                    val isCenter = distance == 0
                    val isTop = distance == -1
                    val isBottom = distance == 1

                    val rotationX = when {
                        isTop -> -20f
                        isBottom -> 20f
                        else -> 0f
                    }

                    val alpha = when {
                        isCenter -> 1.0f
                        isTop || isBottom -> 0.5f
                        else -> 0.2f
                    }

                    val fontSize = if (isCenter) 28.sp else 24.sp
                    val fontWeight = if (isCenter) FontWeight.Medium else FontWeight.Normal
                    val textColor = if (isCenter) mindColors.textPrimary else mindColors.textTertiary

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .graphicsLayer {
                                this.rotationX = rotationX
                                cameraDistance = 12 * density
                            }
                            .alpha(alpha),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemText,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bottom padding item so the last item can be centered
                item {
                    Spacer(modifier = Modifier.height((height - itemHeight) / 2))
                }
            }

            // Top fade gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(mindColors.surface, Color.Transparent)
                        )
                    )
            )

            // Bottom fade gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, mindColors.surface)
                        )
                    )
            )
        }
    }
}
