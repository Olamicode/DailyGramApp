package com.olamachia.dailygramapp.data

import androidx.room.withTransaction
import com.olamachia.dailygramapp.api.NewsAPI
import com.olamachia.dailygramapp.utils.Resource
import com.olamachia.dailygramapp.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsAPI: NewsAPI,
    private val newsArticleDataBase: NewsArticleDataBase
) {
    private val newsArticleDao = newsArticleDataBase.newsArticleDao()

    fun getTopNews(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllNewsArticles()
            },

            fetch = {
                val response = newsAPI.getTopNews()
                response.articles
            },

            saveFetchResult = { serverNewsArticles ->

                val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
                val likedArticles = newsArticleDao.getAllLikedArticles().first()

                val newsArticles = serverNewsArticles.map { serverNewsArticle ->

                    val isBookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                        bookmarkedArticle.url == serverNewsArticle.url
                    }

                    val isLiked = likedArticles.any { likedArticle ->
                        likedArticle.url == serverNewsArticle.url
                    }

                    NewsArticle(
                        url = serverNewsArticle.url,
                        title = serverNewsArticle.title,
                        source = serverNewsArticle.source.name,
                        thumbnailUrl = serverNewsArticle.urlToImage,
                        isBookmarked = isBookmarked,
                        isLiked = isLiked,
                    )
                }

                val topNews = newsArticles.map { article ->
                    TopNews(article.url)
                }

                newsArticleDataBase.withTransaction {
                    newsArticleDao.deleteAllTopNews()
                    newsArticleDao.insertArticles(newsArticles)
                    newsArticleDao.insertTopNews(topNews)
                }
            },

            shouldFetch = { cachedArticles ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedCachedArticles = cachedArticles.sortedBy { article ->
                        article.updatedAt
                    }

                    val oldestTimeStamp = sortedCachedArticles.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimeStamp == null ||
                        oldestTimeStamp < System.currentTimeMillis() -
                        TimeUnit.MINUTES.toMillis(60)

                    needsRefresh
                }
            },

            onFetchSuccess = onFetchSuccess,

            onFetchFailed = { t ->

                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }

        )

    suspend fun updateArticle(newsArticle: NewsArticle) {
        newsArticleDao.updateArticle(newsArticle)
    }

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun resetAllBookmarks() {
        newsArticleDao.resetAllBookmarks()
    }

    suspend fun deleteAllNonBookmarkedArticlesOlderThan(timestampInMillis: Long) {
        newsArticleDao.deleteAllNonBookmarkedArticlesOlderThan(timestampInMillis)
    }
}
