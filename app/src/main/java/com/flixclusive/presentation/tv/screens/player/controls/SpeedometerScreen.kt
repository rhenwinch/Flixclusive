package com.flixclusive.presentation.tv.screens.player.controls

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.flixclusive.R
import com.flixclusive.presentation.tv.utils.ComposeTvUtils.colorOnMediumEmphasisTv
import com.flixclusive.presentation.tv.utils.ModifierTvUtils.createInitialFocusRestorerModifiers
import com.flixclusive.presentation.tv.utils.ModifierTvUtils.focusOnInitialVisibility
import com.flixclusive.presentation.utils.ModifierUtils.ifElse

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SpeedometerScreen(
    playbackSpeed: Int,
    onPlaybackSpeedChange: (Int) -> Unit
) {
    //val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColor = Color.Black

    val listState = rememberTvLazyListState()
    LaunchedEffect(Unit) {
        listState.scrollToItem(playbackSpeed)
    }

    val focusRestorers = createInitialFocusRestorerModifiers()
    val initialFirstVisibleItem by remember { mutableStateOf(listState.firstVisibleItemIndex) }
    val isItemInitiallyFocused = remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .focusGroup()
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0F to surfaceColor,
                        0.2F to surfaceColor,
                        1F to Color.Transparent
                    )
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TvLazyRow(
            modifier = focusRestorers.parentModifier
                .fillMaxWidth(),
            pivotOffsets = PivotOffsets(0.5F),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            state = listState
        ) {
            items(5) {
                val speedName = "${1F + (it * 0.25F)}x"

                Surface(
                    onClick = { onPlaybackSpeedChange(it) },
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.Transparent,
                        contentColor = colorOnMediumEmphasisTv(),
                        focusedContainerColor = Color.Transparent,
                        focusedContentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .ifElse(
                            initialFirstVisibleItem == it,
                            focusRestorers.childModifier
                        )
                        .ifElse(
                            it == playbackSpeed && !isItemInitiallyFocused.value,
                            Modifier.focusOnInitialVisibility(isItemInitiallyFocused)
                        )
                ) {
                    Text(
                        text = speedName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 65.sp
                        )
                    )
                }
            }
        }

        Text(
            text = "Normal",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 65.sp
            ),
            color = Color.White,
            modifier = Modifier
                .padding(top = 40.dp)
        )
    }
}

@Preview(
    device = Devices.TV_1080p,
    widthDp = 1920,
    heightDp = 1080
)
@Composable
fun ArrowShape() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sample_movie_subtitle_preview),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
        )

        SpeedometerScreen(0, {})
    }
}