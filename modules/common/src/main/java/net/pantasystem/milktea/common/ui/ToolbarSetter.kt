package net.pantasystem.milktea.common.ui

import androidx.appcompat.widget.Toolbar

interface ToolbarSetter {
    fun setToolbar(toolbar: Toolbar, visibleTitle: Boolean = true)
    fun setTitle(resId: Int)
}
