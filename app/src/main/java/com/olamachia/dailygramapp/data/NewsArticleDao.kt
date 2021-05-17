package com.olamachia.dailygramapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM top_news INNER JOIN news_articles ON articleUrl = url")
    fun getAllNewsArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopNews(topNews: List<TopNews>)

    @Update
    suspend fun updateArticle(newsArticle: NewsArticle)

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isLiked = 1")
    fun getAllLikedArticles(): Flow<List<NewsArticle>>

    @Query("DELETE FROM top_news")
    suspend fun deleteAllTopNews()
}
