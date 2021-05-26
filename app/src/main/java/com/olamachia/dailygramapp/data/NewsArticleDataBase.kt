package com.olamachia.dailygramapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NewsArticle::class, TopNews::class, SearchResult::class, SearchQueryRemoteKey::class],
    version = 1
)
abstract class NewsArticleDataBase : RoomDatabase() {
    abstract fun newsArticleDao(): NewsArticleDao
    abstract fun searchQueryRemoteKeyDao(): SearchQueryRemoteKeyDao
}
