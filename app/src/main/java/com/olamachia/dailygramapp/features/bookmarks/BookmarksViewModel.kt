package com.olamachia.dailygramapp.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    val bookmarks = newsRepository.getAllBookmarkedArticles()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onBookmarkClicked(newsArticle: NewsArticle) {
        val currentlyBookmarked = newsArticle.isBookmarked
        val updatedArticle = newsArticle.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            newsRepository.updateArticle(updatedArticle)
        }
    }

    fun onLikeClicked(newsArticle: NewsArticle) {
        val currentlyLiked = newsArticle.isLiked
        val updatedArticle = newsArticle.copy(isLiked = !currentlyLiked)
        viewModelScope.launch {
            newsRepository.updateArticle(updatedArticle)
        }
    }

    fun deleteAllBookmark() {
        viewModelScope.launch {
            newsRepository.resetAllBookmarks()
        }
    }
}
