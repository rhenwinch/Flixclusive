package com.flixclusive.domain.utils

import com.flixclusive.common.Constants
import com.flixclusive.domain.model.entities.WatchHistoryItem
import com.flixclusive.domain.model.tmdb.TMDBEpisode

object WatchHistoryUtils {
    fun filterWatchedFilms(watchHistoryItem: WatchHistoryItem): Boolean {
        val isTvShow = watchHistoryItem.seasons != null

        var isFinished = true
        if (watchHistoryItem.episodesWatched.isEmpty()) {
            isFinished = false
        } else if(isTvShow) {
            val nextEpisodeToWatch = getNextEpisodeToWatch(watchHistoryItem)
            if(nextEpisodeToWatch.first != null)
                isFinished = false
        } else {
            isFinished = watchHistoryItem.episodesWatched.last().isFinished
        }

        return isFinished
    }

    /**
     * Determines whether the current playback time is considered as finished watching based on the total duration.
     *
     * @param currentTime The current playback time in milliseconds.
     * @param totalTime The total duration of the media in milliseconds.
     * @return `true` if the current playback time is 95% or more of the total duration, `false` otherwise.
     */
    fun isFinishedWatching(currentTime: Long, totalTime: Long): Boolean {
        val percentage = (currentTime.toDouble() / totalTime.toDouble()) * 100
        return percentage >= Constants.FINISH_THRESHOLD
    }


    /**
     * Retrieves the next episode to watch based on the watch history item.
     * If the show has been finished watching, it returns a pair of `null` values to indicate that the show is finished.
     *
     * @param watchHistoryItem The watch history item containing the episodes watched.
     * @return A pair of season number and episode number representing the next episode to watch.
     *         Returns a pair of `null` values if the show has been finished watching.
     */
    fun getNextEpisodeToWatch(watchHistoryItem: WatchHistoryItem): Pair<Int?, Int?> {
        if(watchHistoryItem.episodesWatched.isEmpty())
            return 1 to 1

        val lastEpisodeWatched = watchHistoryItem.episodesWatched.last()
        var seasonNumber = lastEpisodeWatched.seasonNumber!!
        var episodeNumber = lastEpisodeWatched.episodeNumber!!

        if (lastEpisodeWatched.isFinished) {
            val episodeCountMap = watchHistoryItem.episodes
            val isLastEpisodeOfSeason = episodeNumber == getEpisodeCountForSeason(seasonNumber, episodeCountMap)

            if (isLastEpisodeOfSeason) {
                val nextSeasonNumber = seasonNumber + 1

                val isNextSeasonAvailable = episodeCountMap.containsKey(nextSeasonNumber) || nextSeasonNumber < watchHistoryItem.seasons!!
                if (isNextSeasonAvailable) {
                    seasonNumber = nextSeasonNumber
                    episodeNumber = 1
                } else {
                    // The show has been finished watching
                    return null to null
                }
            } else {
                episodeNumber++
            }
        }

        return seasonNumber to episodeNumber
    }

    /**
     * Retrieves the episode count for a given season number.
     *
     * @param seasonNumber The season number.
     * @param episodeCountMap The map containing episode counts for each season.
     * @return The episode count for the given season number.
     *         Returns 0 if the episode count is not available.
     */
    private fun getEpisodeCountForSeason(seasonNumber: Int, episodeCountMap: Map<Int, Int>?): Int = episodeCountMap?.get(seasonNumber) ?: 0

    fun areThereLessThan10SecondsLeftToWatch(currentWatchTime: Long, totalDurationToWatch: Long): Boolean = ((totalDurationToWatch - currentWatchTime) / 1000) <= 10L

    fun getLastWatchTime(
        watchHistoryItem: WatchHistoryItem,
        episodeToWatch: TMDBEpisode?
    ): Long {
        if(watchHistoryItem.episodesWatched.isEmpty())
            return 0L

        val isTvShow = watchHistoryItem.seasons != null
        return when {
            isTvShow -> {
                val episodeToUse = watchHistoryItem.episodesWatched.find {
                    it.seasonNumber == episodeToWatch?.season
                            && it.episodeNumber == episodeToWatch?.episode
                }

                if(episodeToUse?.isFinished == true) 0L
                else episodeToUse?.watchTime ?: 0L
            }
            !watchHistoryItem.episodesWatched.last().isFinished -> {
                watchHistoryItem.episodesWatched.last().watchTime
            }
            else -> 0L
        }
    }

    fun getTotalDuration(
        watchHistoryItem: WatchHistoryItem,
        episodeToWatch: TMDBEpisode?
    ): Long {
        if(watchHistoryItem.episodesWatched.isEmpty())
            return 0L

        val isTvShow = watchHistoryItem.seasons != null
        return when {
            isTvShow -> {
                val episodeToUse = watchHistoryItem.episodesWatched.find {
                    it.seasonNumber == episodeToWatch?.season
                        && it.episodeNumber == episodeToWatch?.episode
                }

                if(episodeToUse?.isFinished == true) 0L
                else episodeToUse?.durationTime ?: 0L
            }
            !watchHistoryItem.episodesWatched.last().isFinished -> {
                watchHistoryItem.episodesWatched.last().durationTime
            }
            else -> 0L
        }
    }
}