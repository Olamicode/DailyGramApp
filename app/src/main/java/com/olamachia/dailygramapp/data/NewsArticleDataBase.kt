package com.olamachia.dailygramapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NewsArticle::class, BreakingNews::class, SearchResult::class],
    version = 1
)
abstract class NewsArticleDataBase: RoomDatabase() {
    abstract fun newsArticleDao(): NewsArticleDao
}