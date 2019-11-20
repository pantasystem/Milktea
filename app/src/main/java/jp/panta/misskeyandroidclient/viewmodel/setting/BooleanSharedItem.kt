package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData

class BooleanSharedItem(
    override val key: String,
    @StringRes override val titleStringRes: Int,
    val default: Boolean,
    val choiceType: ChoiceType,
    private val context: Context
) : SharedItem(){
    enum class ChoiceType{
        CHECK_BOX,
        SWITCH
    }

    val enabled = MutableLiveData<Boolean>(true)
    val title = context.getString(titleStringRes)
    val choice = BooleanSharedPreferenceLiveData(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        default = default,
        key = key
    )


}