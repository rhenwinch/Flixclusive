package com.flixclusive.core.ui.film

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flixclusive.core.datastore.AppSettingsManager
import com.flixclusive.core.util.common.resource.Resource
import com.flixclusive.core.util.common.ui.UiText
import com.flixclusive.core.util.film.FilmType
import com.flixclusive.data.watch_history.WatchHistoryRepository
import com.flixclusive.domain.database.ToggleWatchlistStatusUseCase
import com.flixclusive.domain.tmdb.FilmProviderUseCase
import com.flixclusive.domain.tmdb.SeasonProviderUseCase
import com.flixclusive.model.database.toWatchlistItem
import com.flixclusive.model.tmdb.Film
import com.flixclusive.model.tmdb.FilmDetails
import com.flixclusive.model.tmdb.TvShow
import com.flixclusive.model.tmdb.common.tv.Season
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.flixclusive.core.util.R as UtilR

abstract class BaseFilmScreenViewModel(
    private val partiallyDetailedFilm: Film,
    watchHistoryRepository: WatchHistoryRepository,
    private val seasonProvider: SeasonProviderUseCase,
    private val filmProvider: FilmProviderUseCase,
    private val toggleWatchlistStatusUseCase: ToggleWatchlistStatusUseCase,
    appSettingsManager: AppSettingsManager
) : ViewModel() {
    val appSettings = appSettingsManager.appSettings
        .data
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = appSettingsManager.localAppSettings
        )

    private val filmId: String = partiallyDetailedFilm.identifier

    private var initializeJob: Job? = null
    private var onSeasonChangeJob: Job? = null
    private var onWatchlistClickJob: Job? = null

    private val _uiState = MutableStateFlow(FilmUiState())
    val uiState = _uiState.asStateFlow()

    private val _film = MutableStateFlow<Film?>(null)
    val film = _film.asStateFlow()

    val watchHistoryItem = watchHistoryRepository
        .getWatchHistoryItemByIdInFlow(filmId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private val _currentSeasonSelected = MutableStateFlow<Resource<Season>>(Resource.Loading)
    val currentSeasonSelected = _currentSeasonSelected.asStateFlow()

    var selectedSeasonNumber by mutableIntStateOf(value = 1)

    init {
        initializeData(film = partiallyDetailedFilm)
    }

    fun initializeData(film: Film = partiallyDetailedFilm) {
        val isSameFilm = filmId == _film.value?.identifier
                && _uiState.value.errorMessage == null
                && !_uiState.value.isLoading

        if (initializeJob?.isActive == true || isSameFilm)
            return

        initializeJob = viewModelScope.launch {
            if (film is FilmDetails) {
                film.onInitializeSuccess()
                return@launch
            }

            _uiState.update { FilmUiState() }
            when (
                val result = filmProvider(partiallyDetailedFilm = film)
            ) {
                is Resource.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error
                                ?: UiText.StringResource(UtilR.string.error_film_message)
                        )
                    }
                }

                Resource.Loading -> Unit
                is Resource.Success -> result.data?.onInitializeSuccess()
            }
        }
    }

    private suspend fun Film.onInitializeSuccess() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null
            )
        }

        _film.update { this@onInitializeSuccess }
        isFilmInWatchlist()

        if (filmType == FilmType.TV_SHOW) {
            val seasonToInitialize =
                if (watchHistoryItem.value?.episodesWatched.isNullOrEmpty()) 1
                else watchHistoryItem.value!!.episodesWatched.last().seasonNumber!!

            onSeasonChange(seasonToInitialize) // Initialize first season
        }
    }

    private suspend fun isFilmInWatchlist() {
        _film.value?.let { film ->
            _uiState.update {
                it.copy(isFilmInWatchlist = toggleWatchlistStatusUseCase.isInWatchlist(film.identifier))
            }
        }
    }

    fun onSeasonChange(seasonNumber: Int) {
        if (onSeasonChangeJob?.isActive == true || selectedSeasonNumber == seasonNumber && _currentSeasonSelected.value is Resource.Success)
            return

        onSeasonChangeJob = viewModelScope.launch {
            selectedSeasonNumber = seasonNumber

            if (_film.value?.isFromTmdb != true) {
                val tvShow = _film.value as TvShow
                val season = tvShow.seasons
                    .find { it.number == seasonNumber }

                if (season != null)
                    _currentSeasonSelected.value = Resource.Success(season)

                return@launch
            }

            seasonProvider.asFlow(
                id = _film.value!!.identifier,
                seasonNumber = seasonNumber
            ).collectLatest {
                _currentSeasonSelected.value = it
            }
        }
    }

    fun onWatchlistButtonClick() {
        if (onWatchlistClickJob?.isActive == true)
            return

        onWatchlistClickJob = viewModelScope.launch {
            _film.value?.toWatchlistItem()?.let { film ->
                val isInWatchlist = toggleWatchlistStatusUseCase(film)
                _uiState.update {
                    it.copy(isFilmInWatchlist = isInWatchlist)
                }
            }
        }
    }
}