package com.olamachia.dailygramapp.features.bookmarks

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
import androidx.recyclerview.widget.RecyclerView
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.databinding.FragmentBookmarksBinding
import com.olamachia.dailygramapp.shared.NewsArticleListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {

    private var currentBinding: FragmentBookmarksBinding? = null
    private val binding get() = currentBinding!!
    private val viewModel: BookmarksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentBinding = FragmentBookmarksBinding.bind(view)

        setHasOptionsMenu(true)

        val newsArticleListAdapter = NewsArticleListAdapter(
            onBookmarkClicked = { article ->
                viewModel.onBookmarkClicked(article)
            },
            onItemClick = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
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

        newsArticleListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.apply {
            fragmentTopNewsRv.apply {
                adapter = newsArticleListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.bookmarks.collect { newsArticles ->
                    val bookmarks = newsArticles ?: return@collect

                    fragmentTopNewsRv.isVisible = bookmarks.isNotEmpty()
                    newsArticleListAdapter.submitList(bookmarks)
                    noBookmarksTv.isVisible = bookmarks.isEmpty()
                    noBookmarkNewsIv.isVisible = bookmarks.isEmpty()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bookmarks, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_delete_all_bookmarks -> {
                viewModel.deleteAllBookmark()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBinding = null
    }
}
