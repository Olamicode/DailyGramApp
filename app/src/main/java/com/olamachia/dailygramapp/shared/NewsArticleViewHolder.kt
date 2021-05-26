package com.olamachia.dailygramapp.shared

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.databinding.TopNewsItemBinding

class NewsArticleViewHolder(
    private val binding: TopNewsItemBinding,
    private val onItemClick: (Int) -> Unit,
    private val onBookmarkClicked: (Int) -> Unit,
    private val onLikeClicked: (Int) -> Unit,
    private val onShareClicked: (Int) -> Unit
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

            if (newsArticle.isLiked) {
                newsLikeIv.visibility = View.GONE
                newsLikeLottieIv.visibility = View.VISIBLE
                newsLikeLottieIv.playAnimation()
            } else {
                newsLikeIv.visibility = View.VISIBLE
                newsLikeLottieIv.visibility = View.GONE
            }
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

            newsLikeLottieIv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    newsLikeLottieIv.playAnimation()
                    onLikeClicked(position)
                }
            }

            newsShareIv.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onShareClicked(position)
                }
            }
        }
    }
}
