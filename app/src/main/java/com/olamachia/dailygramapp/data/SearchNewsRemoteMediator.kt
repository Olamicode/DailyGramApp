package com.olamachia.dailygramapp.data

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.olamachia.dailygramapp.api.NewsAPI
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException

const val NEWS_STARTING_PAGE_INDEX = 1

class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsAPI: NewsAPI,
    private val newsArticleDataBase: NewsArticleDataBase,
    private val refreshOnInit: Boolean
) : RemoteMediator<Int, NewsArticle>() {
    private val newsArticleDao = newsArticleDataBase.newsArticleDao()
    private val searchQueryRemoteKeyDao = newsArticleDataBase.searchQueryRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> searchQueryRemoteKeyDao.getRemoteKey(searchQuery).nextPageKey
        }

        try {
            val response = newsAPI.searchNews(searchQuery, page, state.config.pageSize)
            val serverSearchResults = response.articles

            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
            val likedArticles = newsArticleDao.getAllLikedArticles().first()

            val serverSearchArticle = serverSearchResults.map { serverSearchResultArticle ->
                val isBookmarked = bookmarkedArticles.any { bookmarkedArticles ->
                    bookmarkedArticles.url == serverSearchResultArticle.url
                }

                val isLiked = likedArticles.any { likedArticles ->
                    likedArticles.url == likedArticles.url
                }

                NewsArticle(
                    title = serverSearchResultArticle.title,
                    source = serverSearchResultArticle.source.name,
                    url = serverSearchResultArticle.url,
                    thumbnailUrl = serverSearchResultArticle.urlToImage,
                    isLiked = isLiked,
                    isBookmarked = isBookmarked
                )
            }

            newsArticleDataBase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsArticleDao.deleteSearchResultsForQuery(searchQuery)
                }
                val lastQueryPosition = newsArticleDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val searchResult = serverSearchResults.map { article ->
                    SearchResult(searchQuery, article.url, queryPosition++)
                }

                val nextPageKey = page + 1

                newsArticleDao.insertArticles(serverSearchArticle)
                newsArticleDao.insertSearchResults(searchResult)
                searchQueryRemoteKeyDao.insertRemoteKey(SearchQueryRemoteKey(searchQuery, nextPageKey))
            }

            return MediatorResult.Success(endOfPaginationReached = serverSearchResults.isEmpty())
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return if (refreshOnInit) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }
}
