package com.olamachia.dailygramapp.shared

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.databinding.TopNewsItemBinding

class NewsArticleViewHolder(
    private val binding: TopNewsItemBinding,
    private val onItemClick: (Int) -> Unit,
    private val onBookmarkClicked: (Int) -> Unit,
    private val onLikeClicked: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(newsArticle: NewsArticle) {
        binding.apply {
            Glide.with(itemView)
                .load(newsArticle.thumbnailUrl)
                .error(R.drawable.image_placeholder)
                .into(newsArticleIv)

            newsTitleTv.text = newsArticle.title
            newsChannelTv.text = newsArticle.source

            newsBookmarkIv.setImageResource(
                when (newsArticle.isBookmarked) {
                    true -> R.drawable.ic_bookmark_selected
                    else -> R.drawable.ic_bookmark_unselected
                }
            )
            newsLikeIv.setImageResource(
                when {
                    newsArticle.isLiked -> R.drawable.ic_like_selected
                    else -> R.drawable.ic_like_unselected
                }
            )

        }
    }

    init {
        binding.apply {
            root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }

            newsBookmarkIv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookmarkClicked(position)
                }
            }

            newsLikeIv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLikeClicked(position)
                }
            }

        }

    }

}