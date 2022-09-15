package net.pantasystem.milktea.user

import androidx.annotation.MainThread

interface TitleSettable {

    @MainThread
    fun setTitle(text: String)
}