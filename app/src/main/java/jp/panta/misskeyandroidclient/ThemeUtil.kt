package jp.panta.misskeyandroidclient

import android.content.Context
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun AppCompatActivity.setTheme(){
    val preference = PreferenceManager.getDefaultSharedPreferences(this)
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
    theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
    0.until(menu.size()).forEach{
        val item = menu.getItem(it)
        item.icon?.setTint(typedValue.data)
    }
}



fun Fragment.getAppTheme(): Int{
    val preference = PreferenceManager.getDefaultSharedPreferences(this.context)

    return when(preference.getInt(KeyStore.IntKey.THEME.name, KeyStore.IntKey.THEME.default)){
        KeyStore.IntKey.THEME_WHITE.default -> R.style.AppTheme
        KeyStore.IntKey.THEME_DARK.default -> R.style.AppThemeDark
        KeyStore.IntKey.THEME_BLACK.default -> R.style.AppThemeBlack
        KeyStore.IntKey.THEME_BREAD.default -> R.style.AppThemeBread
        else -> R.style.AppTheme
    }

}
