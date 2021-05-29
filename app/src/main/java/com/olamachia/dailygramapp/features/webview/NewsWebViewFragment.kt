package com.olamachia.dailygramapp.features.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.olamachia.dailygramapp.R
import com.olamachia.dailygramapp.databinding.FragmentNewsWebViewBinding

class NewsWebViewFragment : Fragment(R.layout.fragment_news_web_view) {

    private var currentBinding: FragmentNewsWebViewBinding? = null
    private val binding get() = currentBinding!!

    companion object {
        const val NEWS_WEB_VIEW_FRAGMENT_TAG = "NewsWebViewFragment"
        const val ARTICLE_URL = "article_url"
        const val ARTICLE_SOURCE = "article_source"
        const val PREVIOUS_SCREEN_TITLE = "previous_screen_title"

        fun provideNewsWebViewFragmentWithArg(data: String, source: String, screenTitle: String):
            NewsWebViewFragment {
                val newsWebViewFragment = NewsWebViewFragment()
                val bundle = Bundle()
                bundle.putString(ARTICLE_URL, data)
                bundle.putString(ARTICLE_SOURCE, source)
                bundle.putString(PREVIOUS_SCREEN_TITLE, screenTitle)
                newsWebViewFragment.arguments = bundle
                return newsWebViewFragment
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentBinding = FragmentNewsWebViewBinding.bind(view)

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.visibility = View.GONE

        val articleUrl = arguments?.getString(ARTICLE_URL) ?: getString(R.string.google_url)
        val articleSource = arguments?.getString(ARTICLE_SOURCE) ?: getString(R.string.app_name)
        val previousScreenTitle = arguments?.getString(PREVIOUS_SCREEN_TITLE) ?: getString(R.string.app_name)

        requireActivity().title = articleSource

        loadLink(articleUrl)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Do custom work here

                        // if you want onBackPressed() to be called as normal afterwards
                        if (isEnabled) {
                            isEnabled = false

                            if (parentFragmentManager.backStackEntryCount > 0) {
                                requireActivity().title = previousScreenTitle
                                parentFragmentManager.popBackStackImmediate()
                                bottomNavigationView.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            )
    }

    private fun loadLink(url: String) {

        binding.apply {
            val webSetting = webView.settings
            webSetting.builtInZoomControls = true
            webView.webViewClient = WebViewClient()
            webSetting.javaScriptEnabled = true
            webView.loadUrl(url)
        }
    }

    inner class WebViewClient : android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            binding.apply {
                webView.webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)

                        if (newProgress < 100 && !progressBar.isVisible) {
                            progressBar.isVisible = true
                        }

                        progressBar.progress = newProgress

                        if (newProgress == 100) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                Runnable {
                                    progressBar.isVisible = false
                                },
                                2000
                            )
                        }
                    }
                }
            }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            if (request != null) {
                view?.loadUrl(request.url.toString())
            }
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBinding = null
    }
}
