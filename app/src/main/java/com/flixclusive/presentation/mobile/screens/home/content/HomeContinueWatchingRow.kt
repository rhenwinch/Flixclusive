package com.flixclusive.presentation.mobile.screens.home.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flixclusive.R
import com.flixclusive.domain.model.entities.WatchHistoryItem
import com.flixclusive.domain.model.tmdb.Film
import com.flixclusive.domain.utils.WatchHistoryUtils.getNextEpisodeToWatch
import com.flixclusive.presentation.mobile.common.composables.film.FilmCover
import com.flixclusive.presentation.mobile.main.LABEL_START_PADDING
import com.flixclusive.presentation.mobile.utils.ComposeMobileUtils.colorOnMediumEmphasisMobile
import com.flixclusive.presentation.utils.FormatterUtils.formatMinutes

@Composable
fun HomeContinueWatchingRow(
    modifier: Modifier = Modifier,
    showCardTitle: Boolean,
    dataListProvider: () -> List<WatchHistoryItem>,
    onFilmClick: (Film) -> Unit,
    onSeeMoreClick: (Film) -> Unit,
) {
    if(dataListProvider().isNotEmpty()) {
        Column(
            modifier = modifier
                .padding(top = 25.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.continue_watching),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(start = LABEL_START_PADDING)
            )


            LazyRow {
                itemsIndexed(
                    items = dataListProvider(),
                    key = { i, film ->
                        film.id * i
                    }
                ) { _, item ->
                    ContinueWatchingCard(
                        modifier = Modifier
                            .width(135.dp),
                        showCardTitle = showCardTitle,
                        watchHistoryItem = item,
                        onClick = onFilmClick,
                        onSeeMoreClick = { onSeeMoreClick(item.film) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinueWatchingCard(
    watchHistoryItem: WatchHistoryItem,
    showCardTitle: Boolean,
    onClick: (Film) -> Unit,
    onSeeMoreClick: (Film) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val film = watchHistoryItem.film

    val isTvShow = watchHistoryItem.seasons != null

    if(watchHistoryItem.episodesWatched.isEmpty())
        return

    val lastWatchedEpisode = watchHistoryItem.episodesWatched.last()
    var progress by remember(watchHistoryItem) {
        val percentage = if(lastWatchedEpisode.durationTime == 0L) {
            0F
        } else {
            lastWatchedEpisode.watchTime.toFloat() / lastWatchedEpisode.durationTime.toFloat()
        }

        mutableFloatStateOf(percentage)
    }

    val itemLabel = remember(watchHistoryItem) {
        if(isTvShow) {
            val nextEpisodeWatched = getNextEpisodeToWatch(watchHistoryItem)
            val season = nextEpisodeWatched.first
            val episode = nextEpisodeWatched.second

            val lastEpisodeIsNotSameWithNextEpisodeToWatch = lastWatchedEpisode.episodeNumber != episode

            if(lastEpisodeIsNotSameWithNextEpisodeToWatch)
                progress = 0F

            "S${season} E${episode}"
        } else {
            val watchTime = watchHistoryItem.episodesWatched.last().watchTime
            val watchTimeInSeconds = (watchTime / 1000).toInt()
            val watchTimeInMinutes = watchTimeInSeconds / 60

            formatMinutes(totalMinutes = watchTimeInMinutes)
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )
                )
                .combinedClickable(
                    onClick = { onClick(film) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSeeMoreClick(film)
                    }
                )
        ) {
            FilmCover.Poster(
                imagePath = film.posterImage,
                imageSize = "w220_and_h330_face"
            )


            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = CircleShape
                    )
                    .background(
                        color = colorOnMediumEmphasisMobile(color = Color.Black),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = "A play button icon",
                    modifier = Modifier
                        .size(50.dp),
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0F to Color.Transparent,
                            0.6F to Color.Transparent,
                            0.95F to colorOnMediumEmphasisMobile(
                                MaterialTheme.colorScheme.surface,
                                0.8F
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = itemLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 12.sp
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    LinearProgressIndicator(
                        progress = progress,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(0.4F),
                        modifier = Modifier.clip(MaterialTheme.shapes.large)
                    )
                }
            }

            IconButton(
                onClick = { onSeeMoreClick(film) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.round_more_vert_24),
                    contentDescription = "A vertical see more button icon",
                    tint = Color.White
                )
            }
        }

        if(showCardTitle) {
            Text(
                text = film.title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                color = colorOnMediumEmphasisMobile(emphasis = 0.8F),
                maxLines = 1,
                modifier = Modifier
                    .padding(vertical = 5.dp)
            )
        }
    }
}