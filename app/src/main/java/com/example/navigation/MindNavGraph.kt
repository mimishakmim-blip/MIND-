package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ui.onboarding.AuthScreen
import com.example.ui.components.ConstrainedWidth
import com.example.ui.theme.mindColors
import androidx.compose.foundation.layout.fillMaxSize
import com.example.ui.components.AchievementObserver
import com.example.ui.fitness.FitnessScreen
import com.example.ui.journal.JournalScreen
import com.example.ui.main.MainScreen
import com.example.ui.notes.NotesScreen
import com.example.ui.nutrition.NutritionScreen
import com.example.ui.onboarding.PermissionsScreen
import com.example.ui.onboarding.ProfileSetupScreen
import com.example.ui.onboarding.QuestionnaireScreen
import com.example.ui.onboarding.ReadyScreen
import com.example.ui.onboarding.SplashScreen
import com.example.ui.onboarding.WelcomeScreen
import com.example.ui.sleep.SleepScreen
import com.example.ui.study.StudyScreen

@Composable
fun MindNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    val pendingRoute by DeepLink.route.collectAsState()
    val backEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(pendingRoute, backEntry) {
        val target = pendingRoute
        if (target != null && backEntry?.destination?.route == Routes.MAIN) {
            navController.navigate(target)
            DeepLink.route.value = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.mindColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(250))
        }
    ) {
        constrained(Routes.SPLASH) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        constrained(Routes.WELCOME) {
            WelcomeScreen(
                onStartQuestionnaire = {
                    navController.navigate(Routes.QUESTIONNAIRE)
                },
            )
        }

        composable(
            Routes.AUTH,
            arguments = listOf(navArgument("onboarding") { type = NavType.BoolType })
        ) { entry ->
            val fromOnboarding = entry.arguments?.getBoolean("onboarding") ?: true
            ConstrainedWidth {
                AuthScreen(
                    onboarding = fromOnboarding,
                    onBack = { navController.popBackStack() },
                    onSkip = { navController.navigate(Routes.READY) },
                    onDone = { restored ->
                        when {
                            !fromOnboarding -> navController.popBackStack()
                            restored -> navController.navigate(Routes.MAIN) { popUpTo(0) { inclusive = true } }
                            else -> navController.navigate(Routes.READY)
                        }
                    }
                )
            }
        }

        constrained(Routes.QUESTIONNAIRE) {
            QuestionnaireScreen(
                onComplete = {
                    navController.navigate(Routes.PROFILE_SETUP)
                }
            )
        }

        constrained(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPermissions = {
                    navController.navigate(Routes.PERMISSIONS)
                }
            )
        }

        constrained(Routes.PERMISSIONS) {
            PermissionsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToReady = {
                    navController.navigate(Routes.auth(true))
                }
            )
        }

        constrained(Routes.READY) {
            ReadyScreen(
                onStartJourney = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToSleep = {
                    navController.navigate(Routes.SLEEP)
                },
                onNavigateToStudy = {
                    navController.navigate(Routes.STUDY)
                },
                onNavigateToFitness = {
                    navController.navigate(Routes.FITNESS)
                },
                onNavigateToNutrition = {
                    navController.navigate(Routes.NUTRITION)
                },
                onNavigateToJournal = {
                    navController.navigate(Routes.JOURNAL)
                },
                onNavigateToNotes = {
                    navController.navigate(Routes.NOTES)
                },
                onOpenAuth = { navController.navigate(Routes.auth(false)) },
                onLogout = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        constrained(Routes.SLEEP) {
            SleepScreen(onBack = { navController.popBackStack() })
        }

        constrained(Routes.STUDY) {
            StudyScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNotes = { navController.navigate(Routes.NOTES) },
                onNavigateToJournal = { navController.navigate(Routes.JOURNAL) }
            )
        }

        constrained(Routes.NUTRITION) {
            NutritionScreen(onBack = { navController.popBackStack() })
        }

        constrained(Routes.JOURNAL) {
            JournalScreen(onBack = { navController.popBackStack() })
        }

        constrained(Routes.NOTES) {
            NotesScreen(onBack = { navController.popBackStack() })
        }

        constrained(Routes.FITNESS) {
            FitnessScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNutrition = { navController.navigate(Routes.NUTRITION) }
            )
        }
    }

    AchievementObserver()
    }
}


/** Same as [composable] but keeps the screen at a readable width on landscape / tablets. */
private fun NavGraphBuilder.constrained(route: String, content: @Composable () -> Unit) {
    composable(route) { ConstrainedWidth { content() } }
}
