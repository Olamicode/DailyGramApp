package com.olamachia.dailygramapp.utils

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackBar(
    message: String,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    view: View = requireView(),
    action: () -> Unit = { }
) {
    Snackbar.make(view, message, duration).setAction(
        "Retry"
    ) {
        action()
    }.show()
}

val <T> T.exhaustive: T
    get() = this

inline fun <T : View> T.showIfOrInvisible(condition: (T) -> Boolean) {
    if (condition(this)) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}

inline fun SearchView.onQueryTextSubmit(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!query.isNullOrBlank()) {
                listener(query)
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }
    })
}

fun Fragment.navigateTo(containerID: Int, fragment: Fragment, tag: String) {
    parentFragmentManager.beginTransaction().apply {
        add(containerID, fragment, tag)
        addToBackStack(null)
            .commit()
    }
}
