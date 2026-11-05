package com.example.navigation

import kotlinx.coroutines.flow.MutableStateFlow

/** A route requested from outside the app (home-screen widgets); consumed by the nav graph. */
object DeepLink {
    const val EXTRA_ROUTE = "mind_route"
    val route = MutableStateFlow<String?>(null)
}
