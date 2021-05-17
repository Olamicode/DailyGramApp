package com.olamachia.dailygramapp.data

import android.util.Log
import android.util.TimeUtils
import androidx.room.withTransaction
import com.olamachia.dailygramapp.api.NewsAPI
import com.olamachia.dailygramapp.utils.Resource
import com.olamachia.dailygramapp.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
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
                Log.d("GETTOPNEWS", response.articles.toString())
                response.articles
            },

            saveFetchResult = { serverNewsArticles ->

                val newsArticles = serverNewsArticles.map { serverNewsArticle ->

                    NewsArticle(
                        url = serverNewsArticle.url,
                        title = serverNewsArticle.title,
                        source = serverNewsArticle.source.name,
                        thumbnailUrl = serverNewsArticle.urlToImage,
                        isBookmarked = true,
                        isLiked = true,
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
}
