package com.flixclusive.core.ui.tv

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle.Animated

object FadeInAndOutScreenTransition : Animated {
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition = fadeIn()

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition = fadeOut()
}