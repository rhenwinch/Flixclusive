package com.flixclusive.presentation.mobile.screens.player.controls.episodes_sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flixclusive.R
import com.flixclusive.domain.model.entities.WatchHistoryItem
import com.flixclusive.domain.model.tmdb.TMDBEpisode
import com.flixclusive.presentation.mobile.common.composables.film.FilmCover
import com.flixclusive.presentation.mobile.utils.ComposeMobileUtils.colorOnMediumEmphasisMobile


fun LazyListScope.sheetEpisodeDetails(
    episode: TMDBEpisode,
    isEpisodeCurrentlyBeingWatched: Boolean,
    watchHistoryItem: WatchHistoryItem?,
    onEpisodeClick: (TMDBEpisode) -> Unit,
) {
    item {
        val episodeProgress by remember(watchHistoryItem) {
            if (watchHistoryItem == null)
                return@remember mutableStateOf(null)

            val episodeProgress = watchHistoryItem
                .episodesWatched
                .find {
                    it.episodeId == episode.episodeId
                }

            mutableStateOf(
                if (episodeProgress == null || episodeProgress.durationTime == 0L)
                    null
                else
                    episodeProgress.watchTime.toFloat() / episodeProgress.durationTime.toFloat()
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .width(280.dp)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(5)
                        clip = true
                    }
                    .clickable(enabled = !isEpisodeCurrentlyBeingWatched) {
                        onEpisodeClick(episode)
                    }
            ) {
                FilmCover.Backdrop(
                    imagePath = episode.image,
                    imageSize = "w533_and_h300_bestv2",
                    contentDescription = "An image of episode ${episode.episode}: ${episode.title}",
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isEpisodeCurrentlyBeingWatched) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .matchParentSize()
                            .drawBehind {
                                drawRect(Color.Black.copy(0.4F))
                            }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = stringResource(R.string.play_button),
                            modifier = Modifier.scale(1.5F),
                            tint = Color.White
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 3.dp)
            ) {
                Text(
                    text = "Season ${episode.season} - Episode ${episode.episode}",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "\"${episode.title}\"",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }

            episodeProgress?.let {
                LinearProgressIndicator(
                    progress = it,
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(0.4F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                )
            }
        }
    }

    item {
        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 25.dp)
        ) {
            Text(
                text = episode.description,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Justify,
                color = colorOnMediumEmphasisMobile(emphasis = 0.8F),
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}