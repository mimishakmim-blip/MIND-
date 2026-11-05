package com.example.ui.home

import com.example.i18n.tr
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.components.IOSProgressRing
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.IconButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.data.entity.Note
import com.example.di.DatabaseModule
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToTab: (Int) -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToStudy: () -> Unit,
    onNavigateToFitness: () -> Unit,
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showQuickNote by remember { mutableStateOf(false) }

    val homeHeader: @Composable ColumnScope.() -> Unit = {
            // 1. Greeting, Streak Pill & Quick Note Button
            Spacer(Modifier.height(24.dp))
            val displayName = state.userName.ifBlank { stringResource(R.string.home_default_name) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_greeting, displayName),
                    style = mindTypography.largeTitle,
                    color = mindColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.ui.components.StreakPill(
                        streak = state.currentStreak,
                        onClick = { onNavigateToTab(0) }
                    )
                    IconButton(
                        onClick = { showQuickNote = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(mindColors.surfaceElevated)
                            .testTag("home_quick_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.notes_quick),
                            tint = mindColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))


    }
    val homeLeft: @Composable ColumnScope.() -> Unit = {
            // 2. Hero Card (solid yellow gradient)
            HeroCard(
                completed = state.completedToday,
                total = state.totalHabits,
                progress = state.dailyProgress,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 3. Quick Actions Grid (2x2)
            QuickActionsGrid(
                onHabitsClick = { onNavigateToTab(1) },
                onSleepClick = onNavigateToSleep,
                onStudyClick = onNavigateToStudy,
                onFitnessClick = onNavigateToFitness,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Sleep Shortcut Card
            com.example.ui.components.IOSGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                    .clickable { onNavigateToSleep() }
                    .border(0.5.dp, mindColors.separatorThin, androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌙", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = tr("تتبع النوم والراحة", "Sleep & rest tracking"),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                            color = mindColors.textPrimary
                        )
                    }
                    Text(
                        text = tr("تفاصيل النوم ←", "Sleep details ←"),
                        style = mindTypography.caption.copy(fontWeight = FontWeight.Medium),
                        color = mindColors.primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Fitness Shortcut Card
            com.example.ui.components.IOSGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                    .clickable { onNavigateToFitness() }
                    .border(0.5.dp, mindColors.separatorThin, androidx.compose.foundation.shape.RoundedCornerShape(18.dp)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚡", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = tr("اللياقة وحلقات النشاط", "Fitness & activity rings"),
                            style = mindTypography.subhead.copy(fontWeight = FontWeight.SemiBold),
                            color = mindColors.textPrimary
                        )
                    }
                    Text(
                        text = tr("التمارين والخطوات ←", "Workouts & steps ←"),
                        style = mindTypography.caption.copy(fontWeight = FontWeight.Medium),
                        color = mindColors.primary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))


    }
    val homeRight: @Composable ColumnScope.() -> Unit = {

            // 4. Habits Card
            HabitsPreviewCard(
                completed = state.completedToday,
                total = state.totalHabits,
                onClick = { onNavigateToTab(1) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 5. Quote Card
            QuoteCard(
                quote = state.todayQuote,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(16.dp))

            // 6. Activity Card
            ActivityCard(
                steps = state.todaySteps,
                waterCups = state.todayWater,
                calories = 0,
                modifier = Modifier.padding(horizontal = 20.dp)
            )


    }
    val wide = com.example.ui.components.isWideLayout()

    Box(modifier = modifier.fillMaxSize().testTag("home_screen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 16.dp)
        ) {
            homeHeader()
            if (wide) {
                // Landscape / tablet: quick actions on one side, today's overview on the other.
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) { homeLeft() }
                    Column(modifier = Modifier.weight(1f)) { homeRight() }
                }
            } else {
                homeLeft()
                homeRight()
            }
            Spacer(Modifier.height(24.dp))
        }

        if (showQuickNote) {
            QuickNoteSheet(
                onDismiss = { showQuickNote = false },
                onSave = { content ->
                    coroutineScope.launch {
                        val repo = DatabaseModule.provideNoteRepository(context)
                        val checker = DatabaseModule.provideAchievementChecker(context)
                        val title = content.lines().firstOrNull()?.take(30) ?: tr("ملاحظة سريعة", "Quick note")
                        repo.save(
                            Note(
                                title = title,
                                content = content,
                                color = "#E8C547"
                            )
                        )
                        checker.checkAll()
                    }
                    showQuickNote = false
                }
            )
        }
    }
}

@Composable
fun QuickNoteSheet(
    onDismiss: () -> Unit,
    onSave: (content: String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    var text by remember { mutableStateOf("") }

    com.example.ui.components.IOSSheet(
        onDismiss = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.notes_quick),
                style = mindTypography.title.copy(fontWeight = FontWeight.Bold),
                color = mindColors.textPrimary
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(mindColors.surfaceElevated)
                    .border(1.dp, mindColors.separatorThin, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.notes_hint),
                        style = mindTypography.body,
                        color = mindColors.textTertiary
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = mindTypography.body.copy(color = mindColors.textPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(mindColors.primary),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                com.example.ui.components.IOSButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = onDismiss,
                    style = com.example.ui.components.IOSButtonStyle.Text,
                    modifier = Modifier.weight(1f)
                )
                com.example.ui.components.IOSButton(
                    text = stringResource(R.string.common_save),
                    onClick = {
                        if (text.isNotBlank()) {
                            onSave(text.trim())
                        }
                    },
                    style = com.example.ui.components.IOSButtonStyle.Filled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
private fun HeroCard(
    completed: Int,
    total: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "hero_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        mindColors.primary,
                        mindColors.primaryDark
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.home_daily_goal),
                    color = mindColors.textInverse,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_habits_count, completed, total),
                    color = mindColors.textInverse.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            IOSProgressRing(
                progress = animatedProgress,
                size = 100.dp,
                strokeWidth = 8.dp,
                color = mindColors.textInverse,
                trackColor = mindColors.textInverse.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onHabitsClick: () -> Unit,
    onSleepClick: () -> Unit,
    onStudyClick: () -> Unit,
    onFitnessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Refresh,
                title = stringResource(R.string.qa_habits),
                onClick = onHabitsClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.Bedtime,
                title = stringResource(R.string.qa_sleep),
                onClick = onSleepClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Timer,
                title = stringResource(R.string.qa_pomodoro),
                onClick = onStudyClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.FitnessCenter,
                title = stringResource(R.string.qa_fitness),
                onClick = onFitnessClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val view = LocalView.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "qa_scale"
    )

    Box(
        modifier = modifier
            .height(100.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(mindColors.surface)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                pressed = true
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = mindColors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                color = mindColors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HabitsPreviewCard(
    completed: Int,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    IOSCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = mindColors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_habits_card),
                    color = mindColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_habits_completed, completed, total),
                    color = mindColors.textSecondary,
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft 
                              else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = mindColors.textTertiary
            )
        }
    }
}

@Composable
private fun QuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.home_quote_title),
                color = mindColors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "\"${quote.text}\"",
                color = mindColors.textPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                color = mindColors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ActivityCard(
    steps: Int,
    waterCups: Int,
    calories: Int,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors

    IOSCard(modifier = modifier) {
        Column {
            Text(
                text = stringResource(R.string.home_activity),
                color = mindColors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActivityStat(
                    icon = Icons.Default.DirectionsWalk,
                    value = steps.toString(),
                    label = stringResource(R.string.home_steps),
                    color = mindColors.accentBlue
                )
                ActivityStat(
                    icon = Icons.Default.WaterDrop,
                    value = "$waterCups/8",
                    label = stringResource(R.string.home_cups),
                    color = mindColors.accentBlue
                )
                ActivityStat(
                    icon = Icons.Default.LocalFireDepartment,
                    value = calories.toString(),
                    label = stringResource(R.string.home_calories),
                    color = mindColors.accentOrange
                )
            }
        }
    }
}

@Composable
private fun ActivityStat(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    val mindColors = MaterialTheme.mindColors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            color = mindColors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            color = mindColors.textSecondary,
            fontSize = 12.sp
        )
    }
}
