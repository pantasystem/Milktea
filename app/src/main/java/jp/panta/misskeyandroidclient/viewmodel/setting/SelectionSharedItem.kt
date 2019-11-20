package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SelectionSharedItem (
    override val key: String,
    override val titleStringRes: Int,
    val default: Int,
    val selectionMap: LinkedHashMap<String, Int>,
    private val context: Context
): SharedItem<Int>(){
    val title = context.getString(titleStringRes)

    val choice = IntSharedPreferenceLiveData(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        default = default,
        key = key
    )

    val choosing: String
        get() {
            val k = choice.value
            return selectionMap.filter{
                it.value == k

            }.keys.first()
        }


}