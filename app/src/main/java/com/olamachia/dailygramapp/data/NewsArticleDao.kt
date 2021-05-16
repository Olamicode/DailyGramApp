package com.olamachia.dailygramapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM top_news INNER JOIN news_articles ON articleUrl = url")
    fun getAllNewsArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopNews(topNews: List<TopNews>)

    @Query("DELETE FROM top_news")
    suspend fun deleteAllTopNews()
}
