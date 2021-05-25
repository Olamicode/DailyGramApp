package com.olamachia.dailygramapp.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackBar(
    message: String,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    view: View = requireView(),
    action: () -> Unit
) {
    Snackbar.make(view, message, duration).setAction(
        "Retry"
    ) {
        action()
    }.show()
}

val <T> T.exhaustive: T
    get() = this