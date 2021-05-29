package com.olamachia.dailygramapp.features.searchnews

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.olamachia.dailygramapp.MainActivity
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.databinding.FragmentSearchNewsBinding
import com.olamachia.dailygramapp.features.webview.NewsWebViewFragment
import com.olamachia.dailygramapp.utils.navigateTo
import com.olamachia.dailygramapp.utils.onQueryTextSubmit
import com.olamachia.dailygramapp.utils.showIfOrInvisible
import com.olamachia.dailygramapp.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class SearchNewsFragment :
    Fragment(R.layout.fragment_search_news),
    MainActivity.OnBottomNavigationFragmentReselectedListener {
    private var currentBinding: FragmentSearchNewsBinding? = null
    private val binding get() = currentBinding!!
    private lateinit var newsArticleAdapter: NewsArticlePagingAdapter
    private val viewModel: SearchNewsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBinding = FragmentSearchNewsBinding.bind(view)

        newsArticleAdapter = NewsArticlePagingAdapter(
            onBookmarkClicked = { article ->
                viewModel.onBookmarkClicked(article)
            },
            onItemClick = { article ->
//                val uri = Uri.parse(article.url)
//                val intent = Intent(Intent.ACTION_VIEW, uri)
//                requireActivity().startActivity(intent)

                navigateTo(
                    R.id.fragment_container,
                    NewsWebViewFragment
                        .provideNewsWebViewFragmentWithArg(
                            article.url,
                            article.source, getString(R.string.title_search_news)
                        ),
                    NewsWebViewFragment.NEWS_WEB_VIEW_FRAGMENT_TAG
                )
            },
            onShareClicked = { article ->

                val articleUrl = article.url
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, articleUrl)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            },
            onLikeClicked = { article ->
                viewModel.onLikeClicked(article)
            }
        )

        binding.apply {
            fragmentTopNewsRv.apply {
                adapter = newsArticleAdapter.withLoadStateFooter(
                    NewsArticleLoadStateAdapter(newsArticleAdapter::retry)
                )
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.searchResult.collectLatest { data ->
                    newsArticleAdapter.submitData(data)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.hasCurrentQuery.collect { hasCurrentQuery ->
                    instructionsTv.isVisible = !hasCurrentQuery
                    swipeRefreshLayout.isEnabled = hasCurrentQuery

                    if (!hasCurrentQuery) {
                        fragmentTopNewsRv.isVisible = false
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsArticleAdapter.loadStateFlow
                    .distinctUntilChangedBy { it.source.refresh }
                    .filter { it.source.refresh is LoadState.NotLoading }
                    .collect {
                        if (viewModel.pendingScrollToTopAfterNewQuery) {
                            fragmentTopNewsRv.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterNewQuery = false
                        }
                        if (viewModel.pendingScrollToTopAfterRefresh &&
                            it.mediator?.refresh is LoadState.NotLoading
                        ) {
                            fragmentTopNewsRv.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsArticleAdapter.loadStateFlow.collect { loadState ->
                    when (val refresh = loadState.mediator?.refresh) {
                        is LoadState.Loading -> {
                            retryTv.isVisible = false
                            retryBtn.isVisible = false
                            swipeRefreshLayout.isRefreshing = true
                            noResultsTv.isVisible = false
                            fragmentTopNewsRv.showIfOrInvisible {
                                viewModel.newQueryInProgress && newsArticleAdapter.itemCount > 0
                            }
                            viewModel.refreshInProgress = true
                            viewModel.pendingScrollToTopAfterRefresh = true
                        }

                        is LoadState.NotLoading -> {
                            retryTv.isVisible = false
                            retryBtn.isVisible = false
                            swipeRefreshLayout.isRefreshing = false
                            fragmentTopNewsRv.isVisible = newsArticleAdapter.itemCount > 0

                            val noResult = newsArticleAdapter.itemCount < 1 &&
                                loadState.append.endOfPaginationReached &&
                                loadState.source.append.endOfPaginationReached

                            noResultsTv.isVisible = noResult

                            viewModel.refreshInProgress = false
                            viewModel.newQueryInProgress = false
                        }

                        is LoadState.Error -> {
                            swipeRefreshLayout.isRefreshing = false
                            noResultsTv.isVisible = false
                            fragmentTopNewsRv.isVisible = newsArticleAdapter.itemCount > 0

                            val noCachedResults =
                                newsArticleAdapter.itemCount < 1 && loadState.source.append.endOfPaginationReached

                            retryTv.isVisible = noCachedResults
                            retryBtn.isVisible = noCachedResults

                            val errorMessage = getString(
                                R.string.could_not_load_search_results,
                                refresh.error.localizedMessage
                                    ?: getString(R.string.unknown_error_occurred)
                            )

                            retryTv.text = errorMessage

                            if (viewModel.refreshInProgress) {
                                showSnackBar(errorMessage)
                            }

                            viewModel.refreshInProgress = false
                            viewModel.newQueryInProgress = false
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                newsArticleAdapter.refresh()
            }

            retryBtn.setOnClickListener {
                newsArticleAdapter.retry()
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit { query ->
            viewModel.onSearchQuerySubmit(query)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                newsArticleAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBinding = null
    }

    override fun onBottomNavigationFragmentReselected() {
        binding.fragmentTopNewsRv.scrollToPosition(0)
    }
}
