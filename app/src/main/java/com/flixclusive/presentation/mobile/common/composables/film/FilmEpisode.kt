package com.flixclusive.presentation.mobile.common.composables.film

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import com.flixclusive.R
import com.flixclusive.domain.model.entities.WatchHistoryItem
import com.flixclusive.domain.model.tmdb.TMDBEpisode
import com.flixclusive.presentation.mobile.utils.ComposeMobileUtils.colorOnMediumEmphasisMobile
import com.flixclusive.presentation.theme.lightGrayElevated
import com.flixclusive.presentation.utils.ImageRequestCreator.buildImageUrl
import com.flixclusive.presentation.utils.ModifierUtils.placeholderEffect

@Composable
fun FilmEpisode(
    modifier: Modifier = Modifier,
    episode: TMDBEpisode,
    watchHistoryItem: WatchHistoryItem?,
    onEpisodeClick: (TMDBEpisode) -> Unit
) {
    val context = LocalContext.current
    val cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.6F)

    val progress by remember(watchHistoryItem) {
        if(watchHistoryItem == null)
            return@remember mutableStateOf(null)

        val episodeProgress = watchHistoryItem
            .episodesWatched
            .find {
                it.episodeId == episode.episodeId
            }

        mutableStateOf(
            if(episodeProgress == null || episodeProgress.durationTime == 0L)
                null
            else
                episodeProgress.watchTime.toFloat() / episodeProgress.durationTime.toFloat()
        )
    }

    Row(
        modifier = modifier
            .height(130.dp)
            .padding(horizontal = 10.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(5)
                clip = true
            }
            .drawBehind {
                drawRect(cardColor)
            }
            .clickable {
                onEpisodeClick(episode)
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(180.dp)
                .padding(10.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(5)
                    clip = true
                }
        ) {
            AsyncImage(
                model = context.buildImageUrl(
                    imagePath = episode.image,
                    imageSize = "w533_and_h300_bestv2"
                ),
                imageLoader = LocalContext.current.imageLoader,
                contentDescription = "An image of episode ${episode.episode}: ${episode.title}",
                placeholder = painterResource(R.drawable.movie_placeholder),
                error = painterResource(R.drawable.movie_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

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
                    modifier = Modifier.scale(1.5F)
                )
            }
        }

        Box(
            modifier = Modifier.weight(1F)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(
                        top = 10.dp,
                        bottom = 10.dp,
                        end = 10.dp
                    )
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1F)
                ) {
                    Text(
                        text = "Episode ${episode.episode}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorOnMediumEmphasisMobile(MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 3.dp)
                    )

                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                progress?.let {
                    LinearProgressIndicator(
                        progress = it,
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.tertiary.copy(0.4F),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                shape = RoundedCornerShape(100)
                                clip = true
                            }
                    )
                }
            }
        }
    }
}


@Composable
fun LoadingFilmEpisode(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(130.dp)
            .padding(horizontal = 10.dp)
            .placeholderEffect()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(180.dp)
                .padding(10.dp)
                .placeholderEffect(color = lightGrayElevated)
        )

        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight()
                .padding(
                    top = 10.dp,
                    bottom = 10.dp,
                    end = 10.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(1F)
                        .height(12.dp)
                        .padding(end = 135.dp)
                        .placeholderEffect(
                            shape = RoundedCornerShape(100),
                            color = lightGrayElevated
                        )
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .padding(end = 20.dp)
                        .placeholderEffect(
                            shape = RoundedCornerShape(100),
                            color = lightGrayElevated
                        )
                )
            }
        }
    }
}