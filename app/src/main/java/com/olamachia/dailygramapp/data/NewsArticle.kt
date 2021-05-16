package com.olamachia.dailygramapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticle(
    @PrimaryKey
    val url: String,
    val title: String?,
    val source: String,
    val thumbnailUrl: String?,
    val isBookmarked: Boolean,
    val isLiked: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "top_news")
data class TopNews(
    val articleUrl: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = "search_results", primaryKeys = ["searchQuery", "articleUrl"])
data class SearchResult(
    val searchQuery: String,
    val articleUrl: String,
    val queryPosition: Int
)