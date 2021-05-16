package com.olamachia.dailygramapp.data

import android.util.Log
import androidx.room.withTransaction
import com.olamachia.dailygramapp.api.NewsAPI
import com.olamachia.dailygramapp.utils.Resource
import com.olamachia.dailygramapp.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsAPI: NewsAPI,
    private val newsArticleDataBase: NewsArticleDataBase
) {
    private val newsArticleDao = newsArticleDataBase.newsArticleDao()

    fun getTopNews(
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

                val newsArticles = serverNewsArticles.map {  serverNewsArticle ->

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
                    newsArticleDao.insertArticles(newsArticles)
                    newsArticleDao.insertTopNews(topNews)
                }

            },

            shouldFetch =  {
                true
            },

            onFetchSuccess = onFetchSuccess,

            onFetchFailed = { t ->

                if ( t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }

        )
}