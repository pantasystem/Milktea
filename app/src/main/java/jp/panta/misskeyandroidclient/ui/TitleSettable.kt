package jp.panta.misskeyandroidclient.ui

import androidx.annotation.MainThread

interface TitleSettable {

    @MainThread
    fun setTitle(text: String)
}