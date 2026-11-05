package com.example.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(
    onNavigateToNutrition: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ProfileSettingsScreen(
        modifier = modifier,
        onNavigateToAchievements = onNavigateToAchievements,
        onLogoutSuccess = onLogout,
        onOpenAuth = onOpenAuth
    )
}
