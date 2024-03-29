package com.flixclusive.core.ui.common.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Color.onMediumEmphasis(
    emphasis: Float = 0.6F
) = copy(alpha = emphasis)