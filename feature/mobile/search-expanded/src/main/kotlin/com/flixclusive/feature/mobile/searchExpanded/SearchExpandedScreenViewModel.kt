package com.flixclusive.feature.mobile.searchExpanded

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastMapNotNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flixclusive.core.datastore.AppSettingsManager
import com.flixclusive.core.util.common.dispatcher.AppDispatchers
import com.flixclusive.core.util.common.dispatcher.Dispatcher
import com.flixclusive.core.util.common.resource.Resource
import com.flixclusive.core.util.common.ui.PagingState
import com.flixclusive.core.util.common.ui.UiText
import com.flixclusive.core.util.log.errorLog
import com.flixclusive.data.provider.ProviderManager
import com.flixclusive.data.tmdb.TMDBRepository
import com.flixclusive.gradle.entities.Status
import com.flixclusive.model.tmdb.FilmSearchItem
import com.flixclusive.model.tmdb.SearchResponseData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchExpandedScreenViewModel @Inject constructor(
    private val tmdbRepository: TMDBRepository,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    providerManager: ProviderManager,
    appSettingsManager: AppSettingsManager
) : ViewModel() {
    private val providers = providerManager.workingApis
    val providerDataList by derivedStateOf {
        providerManager.providerDataList.fastMapNotNull { data ->
            if (
                data.status != Status.Maintenance
                && data.status != Status.Down
                && providerManager.isProviderEnabled(data.name)
            ) return@fastMapNotNull data

            null
        }
    }

    val appSettings = appSettingsManager.appSettings
        .data
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = appSettingsManager.localAppSettings
        )

    val searchResults = mutableStateListOf<FilmSearchItem>()

    private var searchingJob: Job? = null

    var selectedProviderIndex by mutableIntStateOf(0)
        private set

    private var page by mutableIntStateOf(1)
    private var maxPage by mutableIntStateOf(1)
    var canPaginate by mutableStateOf(false)
        private set
    var pagingState by mutableStateOf(PagingState.IDLE)
        private set
    var error by mutableStateOf<UiText?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    internal val currentViewType = mutableStateOf(SearchItemViewType.SearchHistory)

    fun onSearch() {
        if (searchingJob?.isActive == true)
            return

        searchingJob = viewModelScope.launch {
            // Reset pagination
            page = 1
            maxPage = 1
            canPaginate = false
            pagingState = PagingState.IDLE
            searchResults.clear()

            currentViewType.value = SearchItemViewType.Films
            paginateItems()
        }
    }

    fun onChangeProvider(index: Int) {
        selectedProviderIndex = index

        onSearch()
    }

    fun onQueryChange(query: String) {
        searchQuery = query
    }

    fun paginateItems() {
        viewModelScope.launch {
            if (isDonePaginating())
                return@launch

            pagingState = when (page) {
                1 -> PagingState.LOADING
                else -> PagingState.PAGINATING
            }

            when (
                val result = getResponseFromProviderEndpoint()
            ) {
                is Resource.Success -> result.data?.parseResults()
                is Resource.Failure -> {
                    error = result.error
                    pagingState = when (page) {
                        1 -> PagingState.ERROR
                        else -> PagingState.PAGINATING_EXHAUST
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun SearchResponseData<FilmSearchItem>.parseResults() {
        val results = results
            .filterNot { it.posterImage == null }

        maxPage = totalPages
        canPaginate = results.size == 20 || page < maxPage

        if (page == 1) {
            searchResults.clear()
        }

        searchResults.addAll(results)

        pagingState = PagingState.IDLE

        if (canPaginate)
            this@SearchExpandedScreenViewModel.page++
    }

    private fun isDonePaginating(): Boolean {
        return page != 1 && (page == 1 || !canPaginate || pagingState != PagingState.IDLE) || searchQuery.isEmpty()
    }

    private suspend fun getResponseFromProviderEndpoint(): Resource<SearchResponseData<FilmSearchItem>> {
        return if (selectedProviderIndex == 0) {
            tmdbRepository.search(
                page = page,
                query = searchQuery
            )
        } else {
            try {
                val result = withContext(ioDispatcher) {
                    providers[selectedProviderIndex - 1].search(
                        page = page,
                        title = searchQuery,
                    )
                }

                Resource.Success(result)
            } catch (e: Exception) {
                errorLog(e)
                Resource.Failure(e)
                    .also {
                        error = it.error
                    }
            }
        }
    }
}

