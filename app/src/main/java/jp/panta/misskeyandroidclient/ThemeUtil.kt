package jp.panta.misskeyandroidclient

import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.setTheme(){
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
    when(preference.getInt(KeyStore.IntKey.THEME.name, KeyStore.IntKey.THEME.default)){
        KeyStore.IntKey.THEME_WHITE.default -> setTheme(R.style.AppTheme)
        KeyStore.IntKey.THEME_DARK.default -> setTheme(R.style.AppThemeDark)
        KeyStore.IntKey.THEME_BLACK.default -> setTheme(R.style.AppThemeBlack)
        KeyStore.IntKey.THEME_BREAD.default -> setTheme(R.style.AppThemeBread)
    }

}