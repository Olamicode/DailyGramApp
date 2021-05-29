package com.olamachia.dailygramapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.olamachia.dailygramapp.databinding.ActivityMainBinding
import com.olamachia.dailygramapp.features.bookmarks.BookmarksFragment
import com.olamachia.dailygramapp.features.searchnews.SearchNewsFragment
import com.olamachia.dailygramapp.features.topnews.TopNewsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.lang.IllegalArgumentException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var topNewsFragment: TopNewsFragment
    private lateinit var searchNewsFragment: SearchNewsFragment
    private lateinit var bookmarksFragment: BookmarksFragment

    companion object {
        const val TAG_TOP_NEWS_FRAGMENT = "TAG_TOP_NEWS_FRAGMENT"
        private const val TAG_SEARCH_NEWS_FRAGMENT = "TAG_SEARCH_NEWS_FRAGMENT"
        private const val TAG_BOOKMARKS_FRAGMENT = "TAG_BOOKMARKS_FRAGMENT"
        private const val KEY_SELECTED_INDEX = "KEY_SELECTED_INDEX"
    }

    private val fragments: Array<Fragment>
        get() = arrayOf(
            topNewsFragment,
            searchNewsFragment,
            bookmarksFragment
        )

    private var selectedIndex = 0
    private val selectedFragment get() = fragments[selectedIndex]

    private fun selectFragment(selectedFragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (selectedFragment === fragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index
            } else {
                transaction.detach(fragment)
            }
        }
        transaction.commit()

        title = when (selectedFragment) {
            is TopNewsFragment -> getString(R.string.top_news)
            is BookmarksFragment -> getString(R.string.bookmarks)
            is SearchNewsFragment -> getString(R.string.search)
            else -> ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            topNewsFragment = TopNewsFragment()
            bookmarksFragment = BookmarksFragment()
            searchNewsFragment = SearchNewsFragment()

            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, topNewsFragment, TAG_TOP_NEWS_FRAGMENT)
                add(R.id.fragment_container, bookmarksFragment, TAG_BOOKMARKS_FRAGMENT)
                add(R.id.fragment_container, searchNewsFragment, TAG_SEARCH_NEWS_FRAGMENT)
            }
                .commit()
        } else {
            topNewsFragment =
                supportFragmentManager.findFragmentByTag(TAG_TOP_NEWS_FRAGMENT) as TopNewsFragment
            bookmarksFragment =
                supportFragmentManager.findFragmentByTag(TAG_BOOKMARKS_FRAGMENT) as BookmarksFragment
            searchNewsFragment =
                supportFragmentManager.findFragmentByTag(TAG_SEARCH_NEWS_FRAGMENT) as SearchNewsFragment

            selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
        }

        /** To load up the first fragment with the zeroth index */

        selectFragment(selectedFragment)

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->

            val fragment = when (item.itemId) {
                R.id.nav_top_news -> topNewsFragment
                R.id.nav_bookmarks -> bookmarksFragment
                R.id.nav_search -> searchNewsFragment
                else -> throw IllegalArgumentException("Unexpected ItemId")
            }

            if (fragment === selectedFragment) {
                if (fragment is OnBottomNavigationFragmentReselectedListener) {
                    fragment.onBottomNavigationFragmentReselected()
                }
            }

            selectFragment(fragment)
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        /** Save the selectedIndex in savedInstanceState bundle
         * so that onConfigurationChange the selectedIndex remains
         * selected */

        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }

    interface OnBottomNavigationFragmentReselectedListener {
        fun onBottomNavigationFragmentReselected()
    }
}
