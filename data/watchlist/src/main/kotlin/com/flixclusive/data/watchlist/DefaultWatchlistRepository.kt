package com.flixclusive.data.watchlist

import com.flixclusive.core.database.dao.WatchlistDao
import com.flixclusive.core.util.common.dispatcher.AppDispatchers
import com.flixclusive.core.util.common.dispatcher.Dispatcher
import com.flixclusive.model.database.WatchlistItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultWatchlistRepository @Inject constructor(
    private val watchlistDao: WatchlistDao,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : WatchlistRepository {
    override suspend fun insert(item: WatchlistItem) = withContext(ioDispatcher) {
        watchlistDao.insert(item)
    }

    override suspend fun remove(item: WatchlistItem) = withContext(ioDispatcher) {
        watchlistDao.delete(item)
    }

    override suspend fun removeById(itemId: String, ownerId: Int) = withContext(ioDispatcher) {
        watchlistDao.deleteById(itemId, ownerId)
    }

    override suspend fun getWatchlistItemById(itemId: String, ownerId: Int): WatchlistItem? = withContext(ioDispatcher) {
        watchlistDao.getWatchlistItemById(itemId, ownerId)
    }

    override suspend fun getAllItems(ownerId: Int): List<WatchlistItem> = withContext(ioDispatcher) {
        watchlistDao.getAllItems(ownerId)
    }

    override fun getAllItemsInFlow(ownerId: Int): Flow<List<WatchlistItem>> = watchlistDao.getAllItemsInFlow(ownerId)
}