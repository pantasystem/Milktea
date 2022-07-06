package jp.panta.misskeyandroidclient.ui.main

import androidx.appcompat.widget.Toolbar

interface ToolbarSetter {
    fun setToolbar(toolbar: Toolbar)
    fun setTitle(resId: Int)
}
