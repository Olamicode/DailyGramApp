package com.olamachia.dailygramapp.features.topnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.databinding.FragmentTopNewsBinding
import com.olamachia.dailygramapp.shared.NewsArticleListAdapter
import com.olamachia.dailygramapp.utils.Resource
import com.olamachia.dailygramapp.utils.exhaustive
import com.olamachia.dailygramapp.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TopNewsFragment : Fragment(R.layout.fragment_top_news) {

    private val viewModel: TopNewsViewModel by viewModels()

    private var currentBinding: FragmentTopNewsBinding? = null
    private val binding get() = currentBinding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBinding = FragmentTopNewsBinding.bind(view)

        val newsArticleListAdapter = NewsArticleListAdapter(
            onItemClick = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            onBookmarkClicked = { article ->
                viewModel.onBookmarkClicked(article)
            },
            onLikeClicked = { article ->
                viewModel.onLikeClicked(article)
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
            }
        )

        binding.apply {
            fragmentTopNewsRv.apply {
                adapter = newsArticleListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.topNews.collect {

                    val result = it ?: return@collect

                    swipeRefreshLayout.isRefreshing = result is Resource.Loading
                    fragmentTopNewsRv.isVisible = !result.data.isNullOrEmpty()
                    retryTv.isVisible = result.error != null && result.data.isNullOrEmpty()
                    retryBtn.isVisible = result.error != null && result.data.isNullOrEmpty()
                    retryTv.text = getString(
                        R.string.could_not_refresh,
                        result.error?.localizedMessage ?: getString(R.string.unknown_error_occurred)
                    )

                    newsArticleListAdapter.submitList(result.data)
                }
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            retryBtn.setOnClickListener {
                viewModel.onStart()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.events.collect { event ->
                    when (event) {
                        is TopNewsViewModel.Event.ShowErrorMessage -> {
                            showSnackBar(
                                getString(
                                    R.string.could_not_refresh,
                                    event.error.localizedMessage
                                        ?: getString(R.string.unknown_error_occurred)
                                )
                            )
                        }
                    }.exhaustive
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_top_news, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                binding.swipeRefreshLayout.isRefreshing = true
                viewModel.onManualRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBinding = null
    }
}
