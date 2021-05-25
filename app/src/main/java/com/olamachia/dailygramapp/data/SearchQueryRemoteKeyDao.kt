package com.olamachia.dailygramapp.data

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

interface SearchQueryRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKey(remoteKey: SearchQueryRemoteKey)

    @Query("SELECT * FROM search_query_remote_keys WHERE searchQuery = :searchQuery")
    fun getRemoteKey(searchQuery: String): SearchQueryRemoteKey

}