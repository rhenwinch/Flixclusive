package com.flixclusive.mobile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flixclusive.core.ui.mobile.KeyEventHandler
import com.flixclusive.core.util.common.resource.Resource
import com.flixclusive.data.configuration.AppConfigurationManager
import com.flixclusive.data.util.InternetMonitor
import com.flixclusive.data.watch_history.WatchHistoryRepository
import com.flixclusive.data.watchlist.WatchlistRepository
import com.flixclusive.domain.provider.SourceLinksProviderUseCase
import com.flixclusive.domain.tmdb.FilmProviderUseCase
import com.flixclusive.model.database.toWatchlistItem
import com.flixclusive.model.provider.SourceData
import com.flixclusive.model.provider.SourceDataState
import com.flixclusive.model.tmdb.Film
import com.flixclusive.model.tmdb.FilmDetails
import com.flixclusive.model.tmdb.common.tv.Episode
import com.flixclusive.model.tmdb.toFilmInstance
import com.flixclusive.provider.util.FlixclusiveWebView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import com.flixclusive.core.util.R as UtilR

@HiltViewModel
internal class MobileAppViewModel @Inject constructor(
    private val configurationManager: AppConfigurationManager,
    private val filmProviderUseCase: FilmProviderUseCase,
    private val sourceLinksProvider: SourceLinksProviderUseCase,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val watchlistRepository: WatchlistRepository,
    internetMonitor: InternetMonitor,
) : ViewModel() {
    private var onFilmLongClickJob: Job? = null
    private var onWatchlistClickJob: Job? = null
    private var onRemoveFromWatchHistoryJob: Job? = null
    private var onPlayClickJob: Job? = null

    val keyEventHandlers = mutableListOf<KeyEventHandler>()
    var isInPipMode by mutableStateOf(false)

    val isConnectedAtNetwork = internetMonitor
        .isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking { internetMonitor.isOnline.first() }
        )

    private val _uiState = MutableStateFlow(MobileAppUiState())
    val uiState: StateFlow<MobileAppUiState> = _uiState.asStateFlow()

    private val _episodeToPlay = MutableStateFlow<Episode?>(null)
    val episodeToPlay = _episodeToPlay.asStateFlow()

    private val _filmToPreview = MutableStateFlow<Film?>(null)
    val filmToPreview = _filmToPreview.asStateFlow()

    val loadedSourceData: SourceData?
        get() = _filmToPreview.value?.identifier?.let {
            sourceLinksProvider.getLinks(
                filmId = it,
                episode = _episodeToPlay.value,
            )
        }

    fun initializeConfigsIfNull() {
        configurationManager.run {
            if(homeCategoriesData == null || searchCategoriesData == null || appConfig == null) {
                initialize()
            }
        }
    }

    fun previewFilm(film: Film) {
        if(onFilmLongClickJob?.isActive == true)
            return

        onFilmLongClickJob = viewModelScope.launch {
            val isInWatchlist = watchlistRepository.getWatchlistItemById(film.identifier) != null
            val isInWatchHistory = watchHistoryRepository.getWatchHistoryItemById(film.identifier) != null

            _filmToPreview.update { film }
            _uiState.update {
                it.copy(
                    isShowingBottomSheetCard = true,
                    isLongClickedFilmInWatchlist = isInWatchlist,
                    isLongClickedFilmInWatchHistory = isInWatchHistory
                )
            }
        }
    }

    fun onBottomSheetClose() {
        _filmToPreview.value = null
        _uiState.update {
            it.copy(isShowingBottomSheetCard = false)
        }
    }

    fun onWatchlistButtonClick() {
        if(onWatchlistClickJob?.isActive == true)
            return

        onWatchlistClickJob = viewModelScope.launch {
            _filmToPreview.value?.let { film ->
                val isInWatchlist = _uiState.value.isLongClickedFilmInWatchlist
                if(isInWatchlist) {
                    watchlistRepository.removeById(film.identifier)
                } else {
                    watchlistRepository.insert(film.toWatchlistItem())
                }

                _uiState.update {
                    it.copy(isLongClickedFilmInWatchlist = !isInWatchlist)
                }
            }
        }
    }

    fun onRemoveButtonClick() {
        if(onRemoveFromWatchHistoryJob?.isActive == true)
            return

        onRemoveFromWatchHistoryJob = viewModelScope.launch {
            val isLongClickedFilmInWatchHistory = _uiState.value.isLongClickedFilmInWatchHistory

            if(isLongClickedFilmInWatchHistory) {
                _filmToPreview.value?.let { film ->
                    _uiState.update {
                        it.copy(isLongClickedFilmInWatchHistory = false)
                    }

                    watchHistoryRepository.deleteById(film.identifier)
                }
            }
        }
    }

    fun onPlayClick(
        film: Film? = null,
        episode: Episode? = null,
        runWebView: (FlixclusiveWebView) -> Unit,
    ) {
        if(onPlayClickJob?.isActive == true)
            return

        onPlayClickJob = viewModelScope.launch {
            updateVideoDataDialogState(SourceDataState.Fetching(UtilR.string.film_data_fetching))

            var filmToShow = film ?: _filmToPreview.value ?: return@launch

            val response = when {
                filmToShow !is FilmDetails -> {
                    filmProviderUseCase(partiallyDetailedFilm = filmToShow)
                }
                else -> Resource.Success(filmToShow)
            }

            val errorFetchingFilm = SourceDataState.Error(UtilR.string.film_data_fetch_failed)
            if(response !is Resource.Success) {
                return@launch updateVideoDataDialogState(errorFetchingFilm)
            }

            filmToShow = response.data
                ?: return@launch updateVideoDataDialogState(errorFetchingFilm)

            val watchHistoryItem = watchHistoryRepository.getWatchHistoryItemById(filmToShow.identifier)
                ?.copy(film = filmToShow.toFilmInstance())
                ?.also { item ->
                    viewModelScope.launch {
                        watchHistoryRepository.insert(item)
                    }
                }

            _filmToPreview.value = filmToShow

            sourceLinksProvider.loadLinks(
                film = filmToShow,
                watchHistoryItem = watchHistoryItem,
                episode = episode,
                runWebView = runWebView,
                onSuccess = { episodeToPlay ->
                    _episodeToPlay.value = episodeToPlay
                }
            ).collectLatest(::updateVideoDataDialogState)
        }
    }

    private fun updateVideoDataDialogState(sourceDataState: SourceDataState) {
        _uiState.update {
            it.copy(sourceDataState = sourceDataState)
        }
    }

    fun onConsumeSourceDataDialog(isForceClosing: Boolean = false) {
        updateVideoDataDialogState(SourceDataState.Idle)
        if(isForceClosing) {
            onPlayClickJob?.cancel() // Cancel job
            onPlayClickJob = null
        }

        _episodeToPlay.value = null
    }

    fun setPlayerModeState(isInPlayer: Boolean) {
        _uiState.update { it.copy(isOnPlayerScreen = isInPlayer) }
    }
}

