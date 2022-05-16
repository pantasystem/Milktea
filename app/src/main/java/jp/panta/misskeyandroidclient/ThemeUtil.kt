package jp.panta.misskeyandroidclient

import android.content.Context
import android.util.TypedValue
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import net.pantasystem.milktea.common.getPreferenceName
import net.pantasystem.milktea.data.infrastructure.settings.Keys
import net.pantasystem.milktea.data.infrastructure.settings.from
import net.pantasystem.milktea.data.infrastructure.settings.str
import net.pantasystem.milktea.data.infrastructure.settings.toInt
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.Theme
import net.pantasystem.milktea.model.setting.isNightTheme

fun AppCompatActivity.setTheme(){
    val preference = this.getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
    val theme = Theme.from(preference.getInt(Keys.ThemeType.str(), DefaultConfig.config.theme.toInt()))
    if(theme.isNightTheme()){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }else{
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    when(theme) {
        is Theme.Dark -> setTheme(R.style.AppThemeDark)
        Theme.Black -> setTheme(R.style.AppThemeBlack)
        Theme.Bread -> setTheme(R.style.AppThemeBread)
        Theme.White -> setTheme(R.style.AppTheme)
    }

}

fun Context.setMenuTint(menu: Menu){
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.normalIconTint, typedValue, true)
    0.until(menu.size()).forEach{
        val item = menu.getItem(it)
        item.icon?.setTint(typedValue.data)
    }
}

