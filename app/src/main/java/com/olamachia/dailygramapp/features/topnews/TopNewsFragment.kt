package com.olamachia.dailygramapp.features.topnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.databinding.FragmentTopNewsBinding
import com.olamachia.dailygramapp.shared.NewsArticleListAdapter
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
            onBookmarkClicked = {
            },
            onLikeClicked = {
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
        }

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
