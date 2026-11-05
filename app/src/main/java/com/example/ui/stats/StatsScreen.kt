package com.example.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.IOSButton
import com.example.ui.components.IOSButtonStyle
import com.example.ui.components.IOSSegmentedControl
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(),
    onNavigateToHabits: () -> Unit = {}
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen")
    ) {
        // 1. Title
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.stats_title),
            style = mindTypography.largeTitle.copy(fontWeight = FontWeight.Bold),
            color = mindColors.textPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 2. Period selector: 3 periods ONLY: [اليوم] [الأسبوع] [الشهر] - NO "السنة"
        IOSSegmentedControl(
            options = listOf(
                stringResource(R.string.period_today),
                stringResource(R.string.period_week),
                stringResource(R.string.period_month)
            ),
            selectedIndex = state.period.ordinal,
            onSelect = { index ->
                viewModel.setPeriod(StatsPeriod.entries[index])
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Scrollable content
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 3. Chart with "الأمس" button
            item {
                ChartCard(
                    todayData = state.todayChartData,
                    yesterdayData = state.yesterdayChartData,
                    showYesterday = state.showYesterday,
                    onToggleYesterday = viewModel::toggleYesterday,
                    isEmpty = state.chartEmpty,
                    period = state.period
                )
            }

            // 4. Summary card
            item {
                SummaryCard(
                    todayCompleted = state.todayCompleted,
                    todayTotal = state.todayTotal,
                    yesterdayCompleted = state.yesterdayCompleted,
                    yesterdayTotal = state.yesterdayTotal
                )
            }

            // 5. Stats grid
            item {
                StatsGrid(
                    totalHabits = state.totalHabitsCompleted,
                    totalSteps = state.totalSteps,
                    totalWater = state.totalWater,
                    totalStudyMinutes = state.totalStudyMinutes,
                    totalSleepMinutes = state.totalSleepMinutes,
                    avgHabitPercent = state.avgHabitPercent
                )
            }

            // 6. Habits line chart (for week/month)
            if (state.period != StatsPeriod.TODAY) {
                item {
                    HabitsLineChartCard(
                        data = state.habitsLine,
                        labels = state.chartLabels,
                        isEmpty = state.habitsLineEmpty
                    )
                }
            }

            // 7. Heatmap calendar
            item {
                HeatmapCard(
                    days = state.heatmapDays,
                    title = stringResource(R.string.stats_heatmap)
                )
            }

            // 8. Share button
            item {
                IOSButton(
                    text = stringResource(R.string.stats_share),
                    onClick = { viewModel.shareProgress() },
                    style = IOSButtonStyle.Filled,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Preview(name = "Stats Screen Dark")
@Composable
fun StatsScreenPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0F))
        ) {
            StatsScreen()
        }
    }
}
