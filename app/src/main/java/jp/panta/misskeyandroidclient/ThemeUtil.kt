package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.setting.Theme
import net.pantasystem.milktea.model.setting.isNightTheme

fun Activity.setTheme() {
    val config = EntryPointAccessors.fromApplication(
            applicationContext,
            BindingProvider::class.java
        ).configRepository().get().getOrNull() ?: return

    val theme = config.theme
    if (theme.isNightTheme()) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    } else {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    when (theme) {
        is Theme.Dark -> setTheme(R.style.AppThemeDark)
        Theme.Black -> setTheme(R.style.AppThemeBlack)
        Theme.Bread -> setTheme(R.style.AppThemeBread)
        Theme.White -> setTheme(R.style.AppTheme)
        Theme.ElephantDark -> setTheme(R.style.AppThemeMastodonDark)
    }

}

fun Context.setMenuTint(menu: Menu) {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.normalIconTint, typedValue, true)
    0.until(menu.size()).forEach {
        val item = menu.getItem(it)
        item.icon?.setTint(typedValue.data)
    }
}

class ApplyThemeImpl(
    val activity: Activity,
) : ApplyTheme {
    override fun invoke() {
        activity.setTheme()
    }
}

class ApplyMenuTintImpl : ApplyMenuTint {
    override fun invoke(context: Context, menu: Menu) {
        context.setMenuTint(menu)
    }
}