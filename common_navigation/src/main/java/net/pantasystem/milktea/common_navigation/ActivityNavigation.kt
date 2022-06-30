package net.pantasystem.milktea.common_navigation

import android.content.Intent

interface ActivityNavigation<T> {

    fun newIntent(args: T): Intent
}