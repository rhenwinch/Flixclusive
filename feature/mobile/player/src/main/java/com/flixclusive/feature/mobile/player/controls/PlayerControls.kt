package com.flixclusive.feature.mobile.player.controls

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.flixclusive.core.ui.player.PlayerSnackbarMessageType
import com.flixclusive.core.ui.player.PlayerUiState
import com.flixclusive.core.ui.player.util.PlayerUiUtil.rememberLocalPlayerManager
import com.flixclusive.core.util.common.resource.Resource
import com.flixclusive.core.util.common.ui.UiText
import com.flixclusive.feature.mobile.player.R
import com.flixclusive.feature.mobile.player.controls.dialogs.audio_and_subtitle.PlayerAudioAndSubtitleDialog
import com.flixclusive.feature.mobile.player.controls.dialogs.servers.PlayerServersDialog
import com.flixclusive.feature.mobile.player.controls.dialogs.settings.PlayerSettingsDialog
import com.flixclusive.feature.mobile.player.controls.episodes.EpisodesScreen
import com.flixclusive.feature.mobile.player.controls.gestures.GestureDirection
import com.flixclusive.feature.mobile.player.controls.gestures.SeekerAndSliderGestures
import com.flixclusive.model.database.WatchHistoryItem
import com.flixclusive.model.datastore.AppSettings
import com.flixclusive.model.provider.SourceLink
import com.flixclusive.model.provider.Subtitle
import com.flixclusive.model.tmdb.common.tv.Season
import com.flixclusive.model.tmdb.common.tv.Episode
import com.flixclusive.provider.ProviderApi
import com.flixclusive.core.ui.player.R as PlayerR
import com.flixclusive.core.util.R as UtilR

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun PlayerControls(
    visibilityProvider: () -> Boolean,
    appSettings: AppSettings,
    areControlsLocked: Boolean,
    isEpisodesSheetOpened: MutableState<Boolean>,
    isAudiosAndSubtitlesDialogOpened: MutableState<Boolean>,
    isPlayerSettingsDialogOpened: MutableState<Boolean>,
    isServersDialogOpened: MutableState<Boolean>,
    watchHistoryItem: WatchHistoryItem?,
    servers: List<SourceLink>,
    isLastEpisode: Boolean,
    providerApis: List<ProviderApi>,
    availableSeasons: Int?,
    stateProvider: () -> PlayerUiState,
    seasonDataProvider: () -> Resource<Season?>,
    currentEpisodeSelected: Episode?,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    showControls: (Boolean) -> Unit,
    toggleControlLock: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSnackbarToggle: (UiText, PlayerSnackbarMessageType) -> Unit,
    onSeasonChange: (Int) -> Unit,
    onProviderChange: (String) -> Unit,
    onVideoServerChange: (Int) -> Unit,
    onResizeModeChange: (Int) -> Unit,
    onPanelChange: (Int) -> Unit,
    onEpisodeClick: (Episode?) -> Unit,
    addSubtitle: (Subtitle) -> Unit,
    toggleVideoTimeReverse: () -> Unit,
) {
    val player by rememberLocalPlayerManager()

    val isVisible by rememberUpdatedState(visibilityProvider())
    val state by rememberUpdatedState(stateProvider())
    val seasonData by rememberUpdatedState(seasonDataProvider())

    val volumeIconId = remember(state.volume) {
        when {
            state.volume > 0.8F -> R.drawable.volume_up_black_24dp
            state.volume < 0.4F && state.volume > 0F -> R.drawable.volume_down_black_24dp
            state.volume == 0F -> R.drawable.volume_off_black_24dp
            else -> R.drawable.volume_up_black_24dp
        }
    }

    fun triggerSnackbar(
        message: String,
        @StringRes messageFormat: Int,
        type: PlayerSnackbarMessageType,
    ) {
        onSnackbarToggle(
            UiText.StringResource(messageFormat, message),
            type
        )
    }

    BackHandler {
        if (
            isEpisodesSheetOpened.value
            || isAudiosAndSubtitlesDialogOpened.value
            || isPlayerSettingsDialogOpened.value
            || isServersDialogOpened.value
            || areControlsLocked
        ) {
            isEpisodesSheetOpened.value = false
            isAudiosAndSubtitlesDialogOpened.value = false
            isPlayerSettingsDialogOpened.value = false
            isServersDialogOpened.value = false

            if (areControlsLocked)
                showControls(true)

            return@BackHandler
        }

        onBack()
    }

    LockControls(
        areControlsVisible = isVisible,
        shouldLockControls = areControlsLocked,
        onVisibilityChange = { toggleControlLock(it) },
        showPlaybackControls = showControls
    )

    if (!areControlsLocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val isSeeking = remember { mutableStateOf(false) }

            if(!isSeeking.value) {
                // Left gestures
                SeekerAndSliderGestures(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight(0.85F),
                    direction = GestureDirection.Left,
                    areControlsVisible = isVisible,
                    seekerIconId = PlayerR.drawable.round_keyboard_double_arrow_left_24,
                    seekAction = player::seekBack,
                    sliderValue = state.screenBrightness,
                    sliderIconId = R.drawable.round_wb_sunny_24,
                    slideAction = onBrightnessChange,
                    showControls = showControls
                )

                // Right gestures
                SeekerAndSliderGestures(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(0.85F),
                    direction = GestureDirection.Right,
                    areControlsVisible = isVisible,
                    seekerIconId = PlayerR.drawable.round_keyboard_double_arrow_right_24,
                    seekAction = player::seekForward,
                    sliderValue = state.volume,
                    sliderIconId = volumeIconId,
                    slideAction = onVolumeChange,
                    showControls = showControls
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (!isSeeking.value) {
                        TopControls(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .animateEnterExit(
                                    enter = slideInVertically(
                                        initialOffsetY = { -it }
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { -it }
                                    )
                                ),
                            currentEpisodeSelected = currentEpisodeSelected,
                            onNavigationIconClick = onBack,
                            onServersClick = {
                                isServersDialogOpened.value = true
                            },
                            onPlayerSettingsClick = {
                                isPlayerSettingsDialogOpened.value = true
                            }
                        )

                        CenterControls(
                            modifier = Modifier.align(Alignment.Center),
                            seekIncrementMs = appSettings.preferredSeekAmount,
                            showControls = showControls
                        )
                    }

                    BottomControls(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .animateEnterExit(
                                enter = slideInVertically(
                                    initialOffsetY = { it }
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { it }
                                )
                            ),
                        isSeeking = isSeeking,
                        isPlayerTimeReversed = appSettings.isPlayerTimeReversed,
                        isTvShow = currentEpisodeSelected != null,
                        isLastEpisode = isLastEpisode,
                        showControls = showControls,
                        onAudioAndDisplayClick = {
                            isAudiosAndSubtitlesDialogOpened.value = true
                        },
                        onEpisodesClick = {
                            isEpisodesSheetOpened.value = true

                            player.run {
                                if(isPlaying) {
                                    playWhenReady = true
                                    pause()
                                }
                            }
                        },
                        onNextEpisodeClick = onEpisodeClick,
                        toggleVideoTimeReverse = toggleVideoTimeReverse,
                        onLockClick = {
                            toggleControlLock(true)
                            showControls(true)
                        }
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isEpisodesSheetOpened.value,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        EpisodesScreen(
            seasonData = seasonData,
            availableSeasons = availableSeasons!!,
            currentEpisodeSelected = currentEpisodeSelected!!,
            watchHistoryItem = watchHistoryItem,
            onEpisodeClick = onEpisodeClick,
            onSeasonChange = onSeasonChange,
            onClose = {
                isEpisodesSheetOpened.value = false

                player.run {
                    if (playWhenReady && !isPlaying) {
                        play()
                    }
                }
            },
        )
    }

    AnimatedVisibility(
        visible = isAudiosAndSubtitlesDialogOpened.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PlayerAudioAndSubtitleDialog(
            showSnackbar = { message, formatter, type ->
                triggerSnackbar(
                    message,
                    formatter,
                    type
                )
            },
            addSubtitle = addSubtitle,
            onDismissSheet = {
                isAudiosAndSubtitlesDialogOpened.value = false
            }
        )
    }

    AnimatedVisibility(
        visible = isPlayerSettingsDialogOpened.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PlayerSettingsDialog(
            state = state,
            showSnackbar = { message, formatter, type ->
                triggerSnackbar(
                    message,
                    formatter,
                    type
                )
            },
            onResizeModeChange = onResizeModeChange,
            onPanelChange = onPanelChange,
            onDismissSheet = {
                isPlayerSettingsDialogOpened.value = false
            }
        )
    }

    AnimatedVisibility(
        visible = isServersDialogOpened.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        PlayerServersDialog(
            state = state,
            servers = servers,
            providerApis = providerApis,
            onProviderChange = { provider ->
                onProviderChange(provider)
                triggerSnackbar(
                    provider,
                    UtilR.string.provider_snackbar_message,
                    PlayerSnackbarMessageType.Provider
                )
            },
            onVideoServerChange = { i, message ->
                onVideoServerChange(i)
                triggerSnackbar(
                    message,
                    UtilR.string.server_snackbar_message,
                    PlayerSnackbarMessageType.Server
                )
            },
            onDismissSheet = {
                isServersDialogOpened.value = false
            }
        )
    }
}