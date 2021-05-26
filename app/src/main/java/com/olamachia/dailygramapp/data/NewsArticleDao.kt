package com.olamachia.dailygramapp.data

import androidx.paging.PagingSource
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

    @Query("UPDATE news_articles SET isBookmarked = 0")
    suspend fun resetAllBookmarks()

    @Query("DELETE FROM news_articles WHERE updatedAt < :timestampInMillis AND isBookmarked = 0")
    suspend fun deleteAllNonBookmarkedArticlesOlderThan(timestampInMillis: Long)

    @Query("DELETE FROM search_results WHERE searchQuery = :query")
    suspend fun deleteSearchResultsForQuery(query: String)

    @Query("SELECT MAX(queryPosition) FROM search_results WHERE searchQuery = :searchQuery")
    suspend fun getLastQueryPosition(searchQuery: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResult>)

    @Query("SELECT * FROM search_results INNER JOIN news_articles ON articleUrl = url WHERE searchQuery =:query ORDER BY queryPosition")
    fun getSearchResultArticlesPaged(query: String): PagingSource<Int, NewsArticle>
}
