package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.SharedPreferences
import androidx.annotation.StringRes

class BooleanSharedItem(
    override val key: String,
    @StringRes override val title: Int,
    val default: Boolean,
    val choiceType: ChoiceType
) : SharedItem<Boolean>(){
    enum class ChoiceType{
        CHECK_BOX,
        SWITCH
    }

    override fun get(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun save(sharedPreferences: SharedPreferences, element: Boolean) {
        val e = sharedPreferences.edit()
        e.putBoolean(key, element)
        e.apply()
    }
}