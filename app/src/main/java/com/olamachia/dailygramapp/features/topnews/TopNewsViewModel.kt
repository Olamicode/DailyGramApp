package com.olamachia.dailygramapp.features.topnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olamachia.dailygramapp.data.NewsRepository
import com.olamachia.dailygramapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TopNewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
): ViewModel() {

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private val refreshTriggerChannel = Channel<Unit>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    val topNews = refreshTrigger.flatMapLatest { refresh ->
        newsRepository.getTopNews(
            onFetchSuccess = {

            },
            onFetchFailed = { t ->
                viewModelScope.launch {
                    eventChannel.send(Event.ShowErrorMessage(t))
                }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    fun onStart() {
        if (topNews.value !is Resource.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Unit)
            }
        }
    }


    enum class Refresh {
        FORCE, NORMAL
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable): Event()
    }

}