package com.olamachia.dailygramapp.shared

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.databinding.TopNewsItemBinding

class NewsArticleListAdapter():
    ListAdapter<NewsArticle, NewsArticleViewHolder>(NewsArticleComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val binding = TopNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsArticleViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }
}