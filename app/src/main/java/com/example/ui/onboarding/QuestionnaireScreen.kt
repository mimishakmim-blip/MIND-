package com.example.ui.onboarding

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.IOSCard
import com.example.ui.onboarding.components.IOSWheelPicker
import com.example.ui.onboarding.components.SmallNavButtons
import com.example.ui.theme.MindTheme
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionnaireScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuestionnaireViewModel = viewModel()
) {
    val context = LocalContext.current
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    var savedPage by rememberSaveable { mutableStateOf(0) }
    val pagerState = rememberPagerState(initialPage = savedPage, pageCount = { 6 })

    LaunchedEffect(pagerState.currentPage) {
        savedPage = pagerState.currentPage
    }

    val hoursList = remember { (0..23).map { String.format("%02d", it) } }
    val minutesList = remember { listOf("00", "15", "30", "45") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .statusBarsPadding()
            .testTag("questionnaire_screen")
    ) {
        // TOP: 6 Progress Dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val isCurrent = pagerState.currentPage == index
                val dotSize by animateDpAsState(
                    targetValue = if (isCurrent) 8.dp else 6.dp,
                    animationSpec = spring(),
                    label = "dot_size"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(dotSize)
                        .background(
                            color = if (isCurrent) mindColors.primary else mindColors.textTertiary,
                            shape = CircleShape
                        )
                )
            }
        }

        // Horizontal Pager (6 Pages)
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> PageLanguage(
                    selectedLang = uiState.language,
                    onSelectLanguage = { lang ->
                        viewModel.setLanguage(lang)
                        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                        sharedPrefs.edit().putString("language", lang).apply()
                        (context as? Activity)?.recreate()
                    }
                )
                1 -> PageGoal(
                    selectedGoal = uiState.goal,
                    onSelectGoal = { viewModel.setGoal(it) }
                )
                2 -> PageDailyTime(
                    selectedMinutes = uiState.dailyMinutes,
                    onSelectTime = { viewModel.setDailyMinutes(it) }
                )
                3 -> PageDistraction(
                    selectedDistraction = uiState.distraction,
                    onSelectDistraction = { viewModel.setDistraction(it) }
                )
                4 -> PageWakeTime(
                    hoursList = hoursList,
                    minutesList = minutesList,
                    selectedHourIndex = uiState.wakeHourIndex,
                    selectedMinuteIndex = uiState.wakeMinuteIndex,
                    onSelectHour = { viewModel.setWakeTime(it, uiState.wakeMinuteIndex) },
                    onSelectMinute = { viewModel.setWakeTime(uiState.wakeHourIndex, it) }
                )
                5 -> PageName(
                    name = uiState.userName,
                    onNameChange = { viewModel.setUserName(it) }
                )
            }
        }

        // FIXED BOTTOM: SmallNavButtons
        SmallNavButtons(
            canGoBack = pagerState.currentPage > 0,
            isLastPage = pagerState.currentPage == 5,
            onPrevious = {
                if (pagerState.currentPage > 0) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }
            },
            onValidateNext = {
                when (pagerState.currentPage) {
                    0 -> true // Language selected
                    1 -> uiState.goal.isNotEmpty()
                    2 -> uiState.dailyMinutes > 0
                    3 -> uiState.distraction.isNotEmpty()
                    4 -> true
                    5 -> uiState.userName.trim().isNotEmpty()
                    else -> true
                }
            },
            onNext = {
                if (pagerState.currentPage < 5) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    viewModel.saveAll {
                        onComplete()
                    }
                }
            },
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 1 — LANGUAGE
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PageLanguage(
    selectedLang: String,
    onSelectLanguage: (String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.q_language_title),
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.q_language_subtitle),
            style = mindTypography.body,
            color = mindColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageCard(
                flag = "🇸🇦",
                title = stringResource(id = R.string.q_language_arabic),
                isSelected = selectedLang == "ar",
                modifier = Modifier.weight(1f),
                onClick = { onSelectLanguage("ar") }
            )

            LanguageCard(
                flag = "🇬🇧",
                title = stringResource(id = R.string.q_language_english),
                isSelected = selectedLang == "en",
                modifier = Modifier.weight(1f),
                onClick = { onSelectLanguage("en") }
            )
        }
    }
}

@Composable
private fun LanguageCard(
    flag: String,
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier
            .height(160.dp)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) mindColors.primary else mindColors.separatorThin,
                shape = shape
            )
            .clickable(onClick = onClick),
        shape = shape,
        color = mindColors.surface,
        shadowElevation = if (isSelected) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = flag, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = mindTypography.headline,
                color = if (isSelected) mindColors.primary else mindColors.textPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 2 — GOAL
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PageGoal(
    selectedGoal: String,
    onSelectGoal: (String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val goals = listOf(
        stringResource(id = R.string.q_goal_organize),
        stringResource(id = R.string.q_goal_habits),
        stringResource(id = R.string.q_goal_fitness),
        stringResource(id = R.string.q_goal_study),
        stringResource(id = R.string.q_goal_all)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.q_goal_title),
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        goals.forEach { goal ->
            val isSelected = selectedGoal == goal
            SelectableOptionCard(
                text = goal,
                isSelected = isSelected,
                onClick = { onSelectGoal(goal) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 3 — DAILY TIME
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PageDailyTime(
    selectedMinutes: Int,
    onSelectTime: (Int) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val options = listOf(
        15 to stringResource(id = R.string.q_time_15),
        30 to stringResource(id = R.string.q_time_30),
        60 to stringResource(id = R.string.q_time_60),
        90 to stringResource(id = R.string.q_time_over)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.q_time_title),
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { (minutes, label) ->
            val isSelected = selectedMinutes == minutes
            SelectableOptionCard(
                text = label,
                isSelected = isSelected,
                onClick = { onSelectTime(minutes) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 4 — DISTRACTION
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PageDistraction(
    selectedDistraction: String,
    onSelectDistraction: (String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val distractions = listOf(
        stringResource(id = R.string.q_distract_social),
        stringResource(id = R.string.q_distract_youtube),
        stringResource(id = R.string.q_distract_games),
        stringResource(id = R.string.q_distract_procrastination)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.q_distract_title),
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        distractions.forEach { distraction ->
            val isSelected = selectedDistraction == distraction
            SelectableOptionCard(
                text = distraction,
                isSelected = isSelected,
                onClick = { onSelectDistraction(distraction) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 5 — WAKE TIME (COMBINATION LOCK)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PageWakeTime(
    hoursList: List<String>,
    minutesList: List<String>,
    selectedHourIndex: Int,
    selectedMinuteIndex: Int,
    onSelectHour: (Int) -> Unit,
    onSelectMinute: (Int) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography

    val currentHourStr = hoursList.getOrElse(selectedHourIndex) { "07" }
    val currentMinStr = minutesList.getOrElse(selectedMinuteIndex) { "00" }
    val previewText = stringResource(id = R.string.q_wake_preview, "$currentHourStr:$currentMinStr")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.q_wake_title),
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SOLID Card with 24dp corners & 24dp padding
        IOSCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Wheel: Hours
                IOSWheelPicker(
                    items = hoursList,
                    selectedIndex = selectedHourIndex,
                    onSelect = onSelectHour,
                    label = stringResource(id = R.string.q_wake_hours),
                    modifier = Modifier.weight(1f)
                )

                // 1dp Vertical Divider
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(1.dp)
                        .background(mindColors.separatorThin)
                )

                // Right Wheel: Minutes
                IOSWheelPicker(
                    items = minutesList,
                    selectedIndex = selectedMinuteIndex,
                    onSelect = onSelectMinute,
                    label = stringResource(id = R.string.q_wake_minutes),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = previewText,
            style = mindTypography.footnote,
            color = mindColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// PAGE 6 — NAME
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PageName(
    name: String,
    onNameChange: (String) -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.q_name_title),
            style = mindTypography.largeTitle,
            color = mindColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (name.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.q_name_hint),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = mindColors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }

            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                cursorBrush = SolidColor(mindColors.primary),
                textStyle = mindTypography.display.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = mindColors.textPrimary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("questionnaire_name_input")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = mindColors.primary,
            modifier = Modifier.width(220.dp)
        )
    }
}

@Composable
private fun SelectableOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) mindColors.primary else mindColors.separatorThin,
                shape = shape
            )
            .clickable(onClick = onClick),
        shape = shape,
        color = mindColors.surface,
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = mindTypography.body.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) mindColors.primary else mindColors.textPrimary
            )
        }
    }
}

@Preview(name = "Questionnaire Screen Dark", showBackground = true)
@Composable
fun QuestionnaireScreenPreview() {
    MindTheme(darkTheme = true) {
        QuestionnaireScreen(onComplete = {})
    }
}
