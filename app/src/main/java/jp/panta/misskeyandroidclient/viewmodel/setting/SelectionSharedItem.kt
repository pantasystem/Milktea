package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.SharedPreferences

class SelectionSharedItem (
    override val key: String,
    override val title: Int,
    val default: Int,
    val selectionMap: LinkedHashMap<String, Int>
): SharedItem<Int>(){

    fun choosing(sharedPreferences: SharedPreferences): String{
        val k = get(sharedPreferences)
        return selectionMap.filter{
            it.value == k

        }.keys.first()
    }

    override fun get(sharedPreferences: SharedPreferences): Int {
        return sharedPreferences.getInt(key, default)
    }

    override fun save(sharedPreferences: SharedPreferences, element: Int) {
        val e =  sharedPreferences.edit()
        e.putInt(key , element)
        e.apply()
    }
}