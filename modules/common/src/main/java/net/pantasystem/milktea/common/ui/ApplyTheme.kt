package net.pantasystem.milktea.common.ui

import android.content.Context
import android.view.Menu

interface ApplyTheme {
    operator fun invoke()
}
interface ApplyMenuTint{
    operator fun invoke(context: Context, menu: Menu)
}