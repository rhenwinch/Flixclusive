package com.flixclusive.feature.tv.home.component.watched

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.flixclusive.core.ui.tv.component.FilmCardHeight
import com.flixclusive.core.ui.tv.component.FilmPadding
import com.flixclusive.core.ui.tv.util.LabelStartPadding
import com.flixclusive.core.ui.tv.util.createDefaultFocusRestorerModifier
import com.flixclusive.core.ui.tv.util.focusOnMount
import com.flixclusive.core.ui.tv.util.useLocalDrawerWidth
import com.flixclusive.core.util.exception.safeCall
import com.flixclusive.model.database.WatchHistoryItem
import kotlinx.coroutines.launch
import com.flixclusive.core.util.R as UtilR

internal const val HOME_WATCHED_FILMS_FOCUS_KEY_FORMAT = "watchedRow=%d, watchedColumn=%d"

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeContinueWatchingRow(
    modifier: Modifier = Modifier,
    items: List<WatchHistoryItem>,
    onPlayClick: () -> Unit,
) {
    val listState = rememberTvLazyListState()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = listState.firstVisibleItemIndex > 0) {
        scope.launch {
            safeCall {
                listState.animateScrollToItem(0)
            }
        }
    }

    Column(
        modifier = modifier
            .focusGroup()
            .heightIn(min = FilmPadding.bottom + 18.dp + FilmCardHeight)
    ) {
        Text(
            text = stringResource(id = UtilR.string.continue_watching),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 40.sp
            ),
            modifier = Modifier
                .padding(start = LabelStartPadding.start + useLocalDrawerWidth())
                .padding(
                    bottom = FilmPadding.bottom,
                    top = 18.dp
                )
        )

        TvLazyRow(
            modifier = createDefaultFocusRestorerModifier(),
            state = listState,
            pivotOffsets = PivotOffsets(parentFraction = 0.07F),
            contentPadding = PaddingValues(
                start = LabelStartPadding.start + useLocalDrawerWidth()
            )
        ) {
            items(count = Int.MAX_VALUE) { i ->
                val columnIndex = i % items.size
                val key = String.format(HOME_WATCHED_FILMS_FOCUS_KEY_FORMAT, 0, columnIndex)

                WatchedFilmCard(
                    modifier = Modifier
                        .focusOnMount(itemKey = key),
                    watchHistoryItem = items[columnIndex],
                    onClick = onPlayClick,
                )
            }
        }

    }
}