package jp.panta.misskeyandroidclient.view

import androidx.annotation.MainThread

interface TitleSettable {

    @MainThread
    fun setTitle(text: String)
}