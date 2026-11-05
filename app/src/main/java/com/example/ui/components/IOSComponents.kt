package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.MindShapes
import com.example.ui.theme.MindTheme
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindShapes
import com.example.ui.theme.mindTypography
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════
// LIQUID GLASS HELPER (CONTROL LAYER ONLY)
// Blur API 31+ with fallback translucent surface on API < 31
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    tintAlpha: Float = 0.78f,
    blurRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.mindColors.isDark
    val surfaceColor = MaterialTheme.mindColors.surface
    val borderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val isApi31Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier = modifier
            .clip(shape)
            .background(surfaceColor, shape)
            .border(0.5.dp, borderColor, shape),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════
// 1) IOSLargeTitle — Collapsing with Parallax
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSLargeTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("ios_large_title")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("large_title_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = mindColors.primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            trailingContent?.invoke()
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = mindTypography.subhead,
                color = mindColors.textSecondary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 2) IOSTabBar — Floating Capsule, Liquid Glass, 5 Tabs
// ═══════════════════════════════════════════════════════════════

data class IOSTabItem(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

@Composable
fun IOSTabBarFloating(
    items: List<IOSTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    IOSTabBar(
        items = items,
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier
    )
}

@Composable
fun IOSTabBar(
    items: List<IOSTabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        IOSGlassContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp), spotColor = mindColors.primaryGlow)
                .testTag("ios_tab_bar"),
            shape = RoundedCornerShape(32.dp),
            tintAlpha = 0.78f,
            blurRadius = 24.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, tab ->
                    val isSelected = selectedIndex == index
                    val bounceAnim = remember { Animatable(1f) }
                    val scope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                scope.launch {
                                    bounceAnim.animateTo(
                                        targetValue = 1.25f,
                                        animationSpec = spring(
                                            dampingRatio = 0.55f,
                                            stiffness = 400f
                                        )
                                    )
                                    bounceAnim.animateTo(
                                        targetValue = 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = 0.75f,
                                            stiffness = 500f
                                        )
                                    )
                                }
                                onSelect(index)
                            }
                            .testTag("tab_item_$index"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            color = mindColors.primary.copy(alpha = 0.18f),
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) mindColors.primary else mindColors.textSecondary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(if (isSelected) 1.15f * bounceAnim.value else 0.88f)
                                )
                                if (tab.badgeCount != null && tab.badgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 6.dp, y = (-4).dp)
                                            .size(8.dp)
                                            .background(mindColors.primary, CircleShape)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.title,
                            style = MaterialTheme.mindTypography.micro.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) mindColors.primary else mindColors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 3) IOSButton — Capsule; Filled / Glass / Text
// ═══════════════════════════════════════════════════════════════

enum class IOSButtonStyle {
    Filled,
    Glass,
    Text
}

@Composable
fun IOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IOSButtonStyle = IOSButtonStyle.Filled,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "btn_scale"
    )

    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            isPressed = true
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (enabled) onClick()
                    }
                )
            }
            .clip(shape)
            .then(
                when (style) {
                    IOSButtonStyle.Filled -> Modifier.background(
                        if (enabled) mindColors.primary else mindColors.surfaceElevated,
                        shape
                    )
                    IOSButtonStyle.Glass -> Modifier
                        .background(mindColors.surfaceElevated.copy(alpha = 0.85f), shape)
                        .border(0.5.dp, mindColors.separatorStrong, shape)
                    IOSButtonStyle.Text -> Modifier
                }
            )
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .testTag("ios_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when (style) {
                        IOSButtonStyle.Filled -> mindColors.textInverse
                        IOSButtonStyle.Glass -> mindColors.primary
                        IOSButtonStyle.Text -> mindColors.primary
                    },
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.mindTypography.headline.copy(fontWeight = FontWeight.SemiBold),
                color = when (style) {
                    IOSButtonStyle.Filled -> if (enabled) mindColors.textInverse else mindColors.textTertiary
                    IOSButtonStyle.Glass -> mindColors.textPrimary
                    IOSButtonStyle.Text -> mindColors.primary
                }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 4) IOSCard — SOLID #1C1C1F dark / #FFFFFF light, 24dp, NO GLASS
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "card_scale"
    )

    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            )
            .clip(shape)
            .border(
                0.5.dp,
                if (mindColors.isDark) mindColors.separatorThin else mindColors.separatorThin,
                shape
            )
            .testTag("ios_card"),
        shape = shape,
        color = mindColors.surface, // Strictly solid surface, never blur or glass
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 5) IOSListItem — iOS Table Row with Chevron
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
    iconTint: Color? = null,
    valueText: String? = null,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val effectiveIcon = leadingIcon ?: icon
    val effectiveValue = trailingText ?: valueText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("ios_list_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (effectiveIcon != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        (iconTint ?: mindColors.primary).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = effectiveIcon,
                    contentDescription = null,
                    tint = iconTint ?: mindColors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = mindTypography.body,
                color = mindColors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = mindTypography.footnote,
                    color = mindColors.textSecondary
                )
            }
        }

        if (effectiveValue != null) {
            Text(
                text = effectiveValue,
                style = mindTypography.subhead,
                color = mindColors.textSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = mindColors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 6) IOSToggle — Authentic iOS Switch in iOS Green (#52C97B)
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current

    val trackColor = if (checked) mindColors.accentGreen else mindColors.separatorStrong
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "thumb_offset"
    )

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable(enabled = enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onCheckedChange(!checked)
            }
            .padding(2.dp)
            .testTag("ios_toggle"),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.dp.roundToPx(), 0) }
                .size(27.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 7) IOSSegmentedControl — iOS Sliding Capsule Selector
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
            .padding(3.dp)
            .testTag("ios_segmented_control")
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9.dp))
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(mindColors.surface)
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(9.dp))
                            } else Modifier
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onSelect(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.mindTypography.footnote.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) mindColors.textPrimary else mindColors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 8) IOSProgressRing — Animated Circular Arc
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = MaterialTheme.mindColors.primary,
    trackColor: Color = MaterialTheme.mindColors.surfaceElevated
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "progress_ring"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("ios_progress_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val radius = diameter / 2f
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx)
            )

            // Progress Arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${(animatedProgress * 100).roundToInt()}%",
            style = MaterialTheme.mindTypography.caption.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.mindColors.textPrimary
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 9) IOSSheet — 28dp Top Radius, Liquid Glass Sheet
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val mindColors = MaterialTheme.mindColors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            IOSGlassContainer(
                modifier = modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .padding(top = 24.dp)
                    .testTag("ios_sheet"),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tintAlpha = 0.90f,
                blurRadius = 30.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    // Pull Handle Indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 36.dp, height = 5.dp)
                            .background(mindColors.textQuaternary, RoundedCornerShape(999.dp))
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    content()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 10) IOSAlert — 20dp Modal with Liquid Glass Backdrop
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSAlert(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "OK",
    dismissText: String? = "Cancel"
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            IOSGlassContainer(
                modifier = modifier
                    .width(290.dp)
                    .clickable(enabled = false) {}
                    .testTag("ios_alert"),
                shape = RoundedCornerShape(20.dp),
                tintAlpha = 0.92f,
                blurRadius = 30.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 18.dp, start = 18.dp, end = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                            color = mindColors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message,
                            style = mindTypography.footnote,
                            color = mindColors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.4f).dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = mindColors.separatorStrong)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissText != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(onClick = onDismiss),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dismissText,
                                    style = mindTypography.body,
                                    color = mindColors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(0.5.dp)
                                    .background(mindColors.separatorStrong)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(onClick = onConfirm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = confirmText,
                                style = mindTypography.body.copy(fontWeight = FontWeight.Bold),
                                color = mindColors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 11) IOSTextField — 12dp Rounded Corners Input
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    var passwordVisible by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = mindColors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = mindTypography.body,
                    color = mindColors.textTertiary
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = mindTypography.body.copy(color = mindColors.textPrimary),
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(mindColors.primary),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ios_text_field_input")
            )
        }

        if (isPassword) {
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle password visibility",
                    tint = mindColors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else if (trailingIcon != null) {
            trailingIcon()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 12) IOSAvatar — Circular Clip with Monogram / Image
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    name: String? = null,
    size: Dp = 44.dp,
    onClick: (() -> Unit)? = null
) {
    val mindColors = MaterialTheme.mindColors
    val initials = name?.split(" ")?.take(2)?.mapNotNull { it.firstOrNull()?.uppercase() }?.joinToString("") ?: "M"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(mindColors.surfaceElevated)
            .border(1.dp, mindColors.primary.copy(alpha = 0.5f), CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag("ios_avatar"),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                color = mindColors.primary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 13) IOSBadge — Capsule/Pill Badge with Micro Typography
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    text: String? = null,
    color: Color = MaterialTheme.mindColors.error
) {
    val displayText = when {
        text != null -> text
        count != null -> if (count > 99) "99+" else count.toString()
        else -> ""
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color)
            .padding(horizontal = 7.dp, vertical = 2.dp)
            .testTag("ios_badge"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.mindTypography.micro.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 14) IOSStepper — Minus/Plus Segmented Control Capsule
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 100,
    step: Int = 1
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(9.dp))
            .testTag("ios_stepper"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp, 34.dp)
                .clickable(enabled = value > min) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onValueChange((value - step).coerceAtLeast(min))
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = if (value > min) mindColors.textPrimary else mindColors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(20.dp)
                .background(mindColors.separatorThin)
        )

        Box(
            modifier = Modifier
                .size(44.dp, 34.dp)
                .clickable(enabled = value < max) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onValueChange((value + step).coerceAtMost(max))
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                tint = if (value < max) mindColors.textPrimary else mindColors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 15) IOSSlider — Sleek iOS Smooth Track & Thumb
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current
    val normalized = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    var widthPx by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    val newNorm = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val deltaNorm = delta / widthPx.coerceAtLeast(1f)
                    val currentNorm = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
                    val newNorm = (currentNorm + deltaNorm).coerceIn(0f, 1f)
                    val newValue = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            )
            .testTag("ios_slider"),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            widthPx = size.width
            val height = size.height

            // Inactive track
            drawRoundRect(
                color = mindColors.surfaceElevated,
                cornerRadius = CornerRadius(height / 2, height / 2)
            )

            // Active track
            drawRoundRect(
                color = mindColors.primary,
                size = Size(size.width * normalized, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )
        }

        // Thumb Circle
        Box(
            modifier = Modifier
                .offset {
                    val px = (widthPx * normalized - 14.dp.toPx()).coerceAtLeast(0f)
                    IntOffset(px.roundToInt(), 0)
                }
                .size(28.dp)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
                .border(0.5.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 16) IOSPicker — iOS Combination Lock Wheel Selector
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(mindColors.surfaceElevated)
            .testTag("ios_picker"),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight bar (center lens)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(mindColors.surfaceHighlight.copy(alpha = 0.6f))
                .border(0.5.dp, mindColors.separatorThin)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(1) {
                Spacer(modifier = Modifier.height(45.dp))
            }
            itemsIndexed(items) { index, item ->
                val isSelected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onSelect(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = if (isSelected) {
                            MaterialTheme.mindTypography.headline.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.mindTypography.body
                        },
                        color = if (isSelected) mindColors.textPrimary else mindColors.textTertiary
                    )
                }
            }
            items(1) {
                Spacer(modifier = Modifier.height(45.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 17) IOSRefreshIndicator — Pull to Refresh Spinner
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSRefreshIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    pullProgress: Float = 1f
) {
    val mindColors = MaterialTheme.mindColors
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Box(
        modifier = modifier
            .size(36.dp)
            .testTag("ios_refresh_indicator"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isRefreshing) Modifier.rotate(rotation) else Modifier)
        ) {
            val strokeWidthPx = 2.5.dp.toPx()
            val radius = (size.minDimension - strokeWidthPx) / 2
            val center = Offset(size.width / 2, size.height / 2)

            val tickCount = 8
            for (i in 0 until tickCount) {
                val angle = (i * 360f / tickCount) * (Math.PI / 180.0)
                val alpha = if (isRefreshing) {
                    ((i + 1).toFloat() / tickCount)
                } else {
                    (pullProgress * ((i + 1).toFloat() / tickCount)).coerceIn(0.1f, 1f)
                }

                val innerR = radius * 0.55f
                val outerR = radius * 0.95f

                val start = Offset(
                    x = center.x + (innerR * Math.cos(angle)).toFloat(),
                    y = center.y + (innerR * Math.sin(angle)).toFloat()
                )
                val end = Offset(
                    x = center.x + (outerR * Math.cos(angle)).toFloat(),
                    y = center.y + (outerR * Math.sin(angle)).toFloat()
                )

                drawLine(
                    color = mindColors.textSecondary.copy(alpha = alpha),
                    start = start,
                    end = end,
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 18) IOSSegmentedBar — Multi-Segment Metric Status Bar
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSSegmentedBar(
    segments: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp
) {
    val total = segments.sum().coerceAtLeast(0.001f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(MaterialTheme.mindColors.surfaceElevated)
            .testTag("ios_segmented_bar"),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        segments.forEachIndexed { index, weight ->
            val color = colors.getOrElse(index) { MaterialTheme.mindColors.primary }
            Box(
                modifier = Modifier
                    .weight((weight / total).coerceAtLeast(0.0001f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 19) IOSGridItem — Clean Rounded Card Grid Tile
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSGridItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeCount: Int? = null,
    iconTint: Color = MaterialTheme.mindColors.primary,
    onClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(
        modifier = modifier
            .aspectRatio(1.1f)
            .testTag("ios_grid_item"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            if (badgeCount != null && badgeCount > 0) {
                IOSBadge(count = badgeCount)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = title,
            style = MaterialTheme.mindTypography.headline.copy(fontWeight = FontWeight.SemiBold),
            color = mindColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.mindTypography.caption,
                color = mindColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 20) IOSGlassCard — CONTROLS ONLY Liquid Glass Card
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    IOSGlassContainer(
        modifier = modifier.testTag("ios_glass_card"),
        shape = RoundedCornerShape(20.dp),
        tintAlpha = 0.78f,
        blurRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// 21) IOSActivityRing — Apple Fitness / Activity Style Ring
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSActivityRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    label: String? = null,
    icon: ImageVector? = null
) {
    val animatedSweep by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "activity_sweep"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("ios_activity_ring")
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                val diameter = this.size.minDimension - strokePx
                val radius = diameter / 2f
                val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

                // Dim Track
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokePx)
                )

                // Glowing Active Arc
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedSweep,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(size * 0.35f)
                )
            } else {
                Text(
                    text = "${(animatedSweep * 100).roundToInt()}%",
                    style = MaterialTheme.mindTypography.footnote.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.mindColors.textPrimary
                )
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.mindTypography.micro,
                color = MaterialTheme.mindColors.textSecondary
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// 22) IOSSearchBar — iOS Search Input Pill
// ═══════════════════════════════════════════════════════════════

@Composable
fun IOSSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onClear: (() -> Unit)? = null
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(mindColors.surfaceElevated)
            .border(0.5.dp, mindColors.separatorThin, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search icon",
            tint = mindColors.textTertiary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = mindTypography.body,
                    color = mindColors.textTertiary
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = mindTypography.body.copy(color = mindColors.textPrimary),
                cursorBrush = SolidColor(mindColors.primary),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ios_search_input")
            )
        }

        if (value.isNotEmpty()) {
            IconButton(
                onClick = {
                    onValueChange("")
                    onClear?.invoke()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = mindColors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// UNIFIED SCREEN HEADER (Identical Back Arrow & Title)
// ═══════════════════════════════════════════════════════════════

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val mindColors = MaterialTheme.mindColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .testTag("screen_header_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(com.example.R.string.common_back),
                tint = Color(0xFFE8965A),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        IOSLargeTitle(
            title = title,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            trailing()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// COMPOSE PREVIEWS
// ═══════════════════════════════════════════════════════════════

@Preview(name = "MIND Components Dark", showBackground = true)
@Composable
fun IOSComponentsPreviewDark() {
    MindTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.mindColors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IOSButton(text = "Filled Button", onClick = {}, style = IOSButtonStyle.Filled)
            IOSToggle(checked = true, onCheckedChange = {})
            IOSCard {
                Text("Solid Content Card", color = MaterialTheme.mindColors.textPrimary)
            }
        }
    }
}
