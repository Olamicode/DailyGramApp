package com.olamachia.dailygramapp.shared

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.databinding.TopNewsItemBinding

class NewsArticleViewHolder(
    private val binding: TopNewsItemBinding,
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
                when {
                    newsArticle.isBookmarked -> R.drawable.ic_bookmark_selected
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

}