package com.olamachia.dailygramapp.features.searchnews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.olamachia.dailygramapp.data.NewsArticle
import com.olamachia.dailygramapp.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    state: SavedStateHandle
) : ViewModel() {

    private val currentQuery = state.getLiveData<String?>("currentQuery", null)

    val hasCurrentQuery = currentQuery.asFlow().map { it != null }

    private var refreshOnInit = false

    val searchResult = currentQuery.asFlow().flatMapLatest { query ->
        query?.let {
            newsRepository.getSearchResultArticlesPaged(query, refreshOnInit)
        } ?: emptyFlow()
    }.cachedIn(viewModelScope)

    var refreshInProgress = false
    var newQueryInProgress = false

    var pendingScrollToTopAfterRefresh = false
    var pendingScrollToTopAfterNewQuery = false

    fun onSearchQuerySubmit(query: String) {
        refreshOnInit = true
        currentQuery.value = query
        newQueryInProgress = true
        pendingScrollToTopAfterNewQuery = true
    }

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
}
