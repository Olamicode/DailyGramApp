package com.olamachia.dailygramapp.api

data class NewsArticleDTO(
    val source: Source,
    val title: String?,
    val url: String,
    val urlToImage: String
)

data class Source(
    val id: String?,
    val name: String
)
