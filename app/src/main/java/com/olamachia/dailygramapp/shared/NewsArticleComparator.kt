package com.olamachia.dailygramapp.shared

import androidx.recyclerview.widget.DiffUtil
import com.olamachia.dailygramapp.data.NewsArticle

class NewsArticleComparator: DiffUtil.ItemCallback<NewsArticle>() {
    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem == newItem
}