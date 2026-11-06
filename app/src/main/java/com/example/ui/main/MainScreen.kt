package com.example.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConstrainedWidth
import com.example.ui.components.isWideLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.achievements.AchievementsScreen
import com.example.ui.components.IOSTabBar
import com.example.ui.components.IOSTabItem
import com.example.ui.habits.HabitsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.mindColors

data class TabItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainScreen(
    onNavigateToSleep: () -> Unit = {},
    onNavigateToStudy: () -> Unit = {},
    onNavigateToFitness: () -> Unit = {},
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenAuth: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(2) }
    val mindColors = MaterialTheme.mindColors

    // Back from any tab returns to Home first, like a native app.
    BackHandler(enabled = selectedTab != 2) { selectedTab = 2 }

    val tabContent: @Composable () -> Unit = {
        when (selectedTab) {
            0 -> ConstrainedWidth(820.dp) { StatsScreen(onNavigateToHabits = { selectedTab = 1 }) }
            1 -> ConstrainedWidth(820.dp) { HabitsScreen(onOpenStats = { selectedTab = 0 }) }
            2 -> HomeScreen(
                onNavigateToTab = { selectedTab = it },
                onNavigateToSleep = onNavigateToSleep,
                onNavigateToStudy = onNavigateToStudy,
                onNavigateToFitness = onNavigateToFitness,
                onNavigateToNutrition = onNavigateToNutrition,
                onNavigateToJournal = onNavigateToJournal,
                onNavigateToNotes = onNavigateToNotes
            )
            3 -> AchievementsScreen()
            4 -> ProfileScreen(
                onNavigateToNutrition = onNavigateToNutrition,
                onNavigateToAchievements = { selectedTab = 3 },
                onLogout = onLogout,
                onOpenAuth = onOpenAuth
            )
        }
    }

    if (isWideLayout()) {
        // Landscape phones / tablets: navigation rail on the side, content centered.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(mindColors.background)
                .testTag("main_screen")
        ) {
            MindNavigationRail(
                items = getMainTabs(),
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxHeight().widthIn(max = 1100.dp).fillMaxWidth()) {
                    tabContent()
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize().testTag("main_screen"),
            containerColor = mindColors.background,
            bottomBar = {
                IOSTabBarFloating(
                    items = getMainTabs(),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                tabContent()
            }
        }
    }
}

@Composable
private fun MindNavigationRail(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val colors = MaterialTheme.mindColors
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(88.dp)
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label, fontSize = 11.sp, maxLines = 1) },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.textInverse,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primary,
                    unselectedIconColor = colors.textTertiary,
                    unselectedTextColor = colors.textTertiary
                )
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(0.5.dp)
            .background(colors.separatorThin)
    )
}

@Composable
fun IOSTabBarFloating(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    IOSTabBar(
        items = items.map { IOSTabItem(title = it.label, icon = it.icon) },
        selectedIndex = selectedIndex,
        onSelect = onSelect,
        modifier = modifier
    )
}

@Composable
fun getMainTabs(): List<TabItem> = listOf(
    TabItem(Icons.Default.BarChart, stringResource(R.string.tab_data)),
    TabItem(Icons.Default.CheckCircle, stringResource(R.string.tab_habits)),
    TabItem(Icons.Default.Home, stringResource(R.string.tab_home)),
    TabItem(Icons.Default.EmojiEvents, stringResource(R.string.tab_achievements)),
    TabItem(Icons.Default.Person, stringResource(R.string.tab_account))
)
