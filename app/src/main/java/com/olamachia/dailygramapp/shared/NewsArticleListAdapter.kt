package com.olamachia.dailygramapp.shared

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.databinding.TopNewsItemBinding

class NewsArticleListAdapter(
    private val onItemClick: (NewsArticle) -> Unit,
    private val onBookmarkClicked: (NewsArticle) -> Unit,
    private val onLikeClicked: (NewsArticle) -> Unit,
    private val onShareClicked: (NewsArticle) -> Unit
) :
    ListAdapter<NewsArticle, NewsArticleViewHolder>(NewsArticleComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val binding = TopNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return NewsArticleViewHolder(
            binding,
            onItemClick = { position ->
                val article = getItem(position)
                if (article != null) {
                    onItemClick(article)
                }
            },
            onBookmarkClicked = { position ->
                val article = getItem(position)
                if (article != null) {
                    onBookmarkClicked(article)
                }
            },
            onLikeClicked = { position ->
                val article = getItem(position)
                if (article != null) {
                    onLikeClicked(article)
                }
            },
            onShareClicked = { position ->
                val article = getItem(position)
                if (article != null) {
                    onShareClicked(article)
                }
            }
        )
    }

    override fun onBindViewHolder(holder: NewsArticleViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }
}
