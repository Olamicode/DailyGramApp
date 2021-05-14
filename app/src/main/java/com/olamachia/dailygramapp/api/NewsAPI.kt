package com.olamachia.dailygramapp.api

import com.olamachia.dailygramapp.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Headers

interface NewsAPI {
    companion object {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = BuildConfig.NEWS_API_ACCESS_KEY
    }

    @Headers("X-Api-Key: $API_KEY")
    @GET("top-headlines?country=us&pageSize=100")
    suspend fun getBreakingNews(): NewsResponse


}