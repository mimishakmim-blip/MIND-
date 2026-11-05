package com.example.ui.achievements

import com.example.data.constants.achievementTitle
import com.example.data.constants.achievementDescription
import com.example.i18n.tr
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.constants.categoryLabelAr
import com.example.data.entity.Achievement
import com.example.ui.components.IOSAlert
import com.example.ui.components.IOSCard
import com.example.ui.components.IOSLargeTitle
import com.example.ui.components.IOSProgressRing
import com.example.ui.theme.mindColors
import com.example.ui.theme.mindTypography

fun getAchievementIcon(iconName: String): ImageVector = when (iconName) {
    "star" -> Icons.Default.Star
    "local_fire_department" -> Icons.Default.LocalFireDepartment
    "verified" -> Icons.Default.Verified
    "task_alt" -> Icons.Default.TaskAlt
    "wb_sunny" -> Icons.Default.WbSunny
    "workspace_premium" -> Icons.Default.WorkspacePremium
    "directions_walk" -> Icons.Default.DirectionsWalk
    "route" -> Icons.Default.Route
    "water_drop" -> Icons.Default.WaterDrop
    "fitness_center" -> Icons.Default.FitnessCenter
    "school" -> Icons.Default.School
    "timer" -> Icons.Default.Timer
    "psychology" -> Icons.Default.Psychology
    "wb_twilight" -> Icons.Default.WbTwilight
    "nightlight" -> Icons.Default.Nightlight
    "restaurant" -> Icons.Default.Restaurant
    "local_dining" -> Icons.Default.LocalDining
    "egg" -> Icons.Default.Egg
    "eco" -> Icons.Default.Eco
    "check_circle" -> Icons.Default.CheckCircle
    "block" -> Icons.Default.Block
    "soup_kitchen" -> Icons.Default.SoupKitchen
    "event_available" -> Icons.Default.EventAvailable
    "weekend" -> Icons.Default.Weekend
    "collections_bookmark" -> Icons.Default.CollectionsBookmark
    "note" -> Icons.Default.Note
    "menu_book" -> Icons.Default.MenuBook
    "emoji_events" -> Icons.Default.EmojiEvents
    "stars" -> Icons.Default.Stars
    "apps" -> Icons.Default.Apps
    "explore" -> Icons.Default.Explore
    "schedule" -> Icons.Default.Schedule
    "military_tech" -> Icons.Default.MilitaryTech
    else -> Icons.Default.EmojiEvents
}

@Composable
fun AchievementsScreen(
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = viewModel()
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(mindColors.background)
            .testTag("achievements_screen")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (com.example.ui.components.isWideLayout()) 4 else 2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Large Title
            item(span = { GridItemSpan(maxLineSpan) }) {
                IOSLargeTitle(
                    title = stringResource(R.string.achievements_title)
                )
            }

            // 2. Progress Card (Ring + Count)
            item(span = { GridItemSpan(maxLineSpan) }) {
                val remaining = (state.totalCount - state.unlockedCount).coerceAtLeast(0)
                val progress = if (state.totalCount > 0) state.unlockedCount.toFloat() / state.totalCount else 0f

                IOSCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IOSProgressRing(
                            progress = progress,
                            size = 64.dp,
                            strokeWidth = 7.dp,
                            color = mindColors.primary,
                            trackColor = mindColors.surfaceElevated
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.achievements_progress, state.unlockedCount, state.totalCount),
                                style = mindTypography.headline.copy(fontWeight = FontWeight.Bold),
                                color = mindColors.textPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.achievements_remaining, remaining),
                                style = mindTypography.footnote,
                                color = mindColors.textSecondary
                            )
                        }
                    }
                }
            }

            // 3. Filter Chips [الكل | محقق | مقفل]
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AchievementFilter.values().forEach { filter ->
                        val isSelected = state.filter == filter
                        val label = when (filter) {
                            AchievementFilter.ALL -> stringResource(R.string.achievements_filter_all)
                            AchievementFilter.UNLOCKED -> stringResource(R.string.achievements_filter_unlocked)
                            AchievementFilter.LOCKED -> stringResource(R.string.achievements_filter_locked)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) mindColors.primary else mindColors.surfaceElevated)
                                .border(
                                    0.5.dp,
                                    if (isSelected) Color.Transparent else mindColors.separatorThin,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) mindColors.textInverse else mindColors.textPrimary,
                                style = mindTypography.caption.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // 4. Category Selector [الكل | العادات | اللياقة | الدراسة | التغذية | عام]
            item(span = { GridItemSpan(maxLineSpan) }) {
                val categories = listOf(
                    "ALL" to stringResource(R.string.achievements_category_all),
                    "HABITS" to stringResource(R.string.achievements_category_habits),
                    "FITNESS" to stringResource(R.string.achievements_category_fitness),
                    "STUDY" to stringResource(R.string.achievements_category_study),
                    "NUTRITION" to stringResource(R.string.achievements_category_nutrition),
                    "GENERAL" to stringResource(R.string.achievements_category_general)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { (catKey, catLabel) ->
                        val isSelected = state.category == catKey
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) mindColors.primary.copy(alpha = 0.2f) else mindColors.surfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) mindColors.primary else mindColors.separatorThin,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setCategory(catKey) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isSelected) mindColors.primary else mindColors.textSecondary,
                                style = mindTypography.caption.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            // 5. Grid of 2 columns of achievement cards (NO BLUR, solid IOSCard)
            items(state.filteredAchievements, key = { it.id }) { achievement ->
                AchievementGridCard(
                    achievement = achievement,
                    onClick = { viewModel.showDetail(achievement) }
                )
            }
        }

        // 6. Detail Dialog
        state.selectedAchievement?.let { sel ->
            val isUnlocked = sel.unlockedAt != null
            val icon = getAchievementIcon(sel.iconName)

            IOSAlert(
                title = achievementTitle(sel.id, sel.title),
                message = "${achievementDescription(sel.id, sel.description)}\n\n${stringResource(R.string.achievements_progress, sel.currentProgress, sel.target)}",
                onConfirm = if (isUnlocked) {
                    {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, tr("حققت وسام «${achievementTitle(sel.id, sel.title)}» في تطبيق MIND! 🏆✨", "I earned the “${achievementTitle(sel.id, sel.title)}” badge in MIND! 🏆✨"))
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, tr("مشاركة الإنجاز", "Share achievement")))
                        viewModel.hideDetail()
                    }
                } else {
                    { viewModel.hideDetail() }
                },
                onDismiss = { viewModel.hideDetail() },
                confirmText = if (isUnlocked) stringResource(R.string.achievements_share) else stringResource(R.string.achievements_continue),
                dismissText = if (isUnlocked) stringResource(R.string.achievements_continue) else null
            )
        }
    }
}

@Composable
private fun AchievementGridCard(
    achievement: Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mindColors = MaterialTheme.mindColors
    val mindTypography = MaterialTheme.mindTypography
    val isUnlocked = achievement.unlockedAt != null
    val icon = getAchievementIcon(achievement.iconName)

    val borderColor = if (isUnlocked) mindColors.primary else mindColors.separatorThin
    val iconColor = if (isUnlocked) mindColors.primary else mindColors.textTertiary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(mindColors.surfaceElevated) // Solid background, NO blur
            .border(if (isUnlocked) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(14.dp)
            .testTag("achievement_card_${achievement.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) mindColors.primary.copy(alpha = 0.15f) else mindColors.background)
                    .border(
                        1.dp,
                        if (isUnlocked) mindColors.primary else mindColors.separatorThin,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = achievementTitle(achievement.id, achievement.title),
                style = mindTypography.subhead.copy(fontWeight = FontWeight.Bold),
                color = if (isUnlocked) mindColors.textPrimary else mindColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = achievementDescription(achievement.id, achievement.description),
                style = mindTypography.caption,
                color = mindColors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            if (isUnlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = mindColors.accentGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.achievements_unlocked),
                        color = mindColors.accentGreen,
                        style = mindTypography.caption.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Text(
                    text = "${achievement.currentProgress} / ${achievement.target}",
                    color = mindColors.textTertiary,
                    style = mindTypography.caption.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}
