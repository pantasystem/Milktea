package jp.panta.misskeyandroidclient

import android.content.Context
import android.util.TypedValue
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import net.pantasystem.milktea.common.getPreferenceName
import net.pantasystem.milktea.data.model.KeyStore

fun AppCompatActivity.setTheme(){
    val preference = this.getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
    val theme = KeyStore.IntKey.values()[preference.getInt(KeyStore.IntKey.THEME.name, KeyStore.IntKey.THEME.default)]

    if(KeyStore.isNightTheme(theme)){
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }else{
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    when(preference.getInt(KeyStore.IntKey.THEME.name, KeyStore.IntKey.THEME.default)){
        KeyStore.IntKey.THEME_WHITE.default -> setTheme(R.style.AppTheme)
        KeyStore.IntKey.THEME_DARK.default -> setTheme(R.style.AppThemeDark)
        KeyStore.IntKey.THEME_BLACK.default -> setTheme(R.style.AppThemeBlack)
        KeyStore.IntKey.THEME_BREAD.default -> setTheme(R.style.AppThemeBread)
    }

}

fun Context.setMenuTint(menu: Menu){
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorNoteActionButtonTint, typedValue, true)
    0.until(menu.size()).forEach{
        val item = menu.getItem(it)
        item.icon?.setTint(typedValue.data)
    }
}

