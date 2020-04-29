package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.util.getPreferenceName

class BooleanSharedItem(
    override val key: String,
    @StringRes override val titleStringRes: Int,
    val default: Boolean,
    val choiceType: ChoiceType,
    context: Context
) : SharedItem(){
    enum class ChoiceType{
        CHECK_BOX,
        SWITCH
    }

    val enabled = MutableLiveData<Boolean>(true)
    val title = context.getString(titleStringRes)
    val choice = BooleanSharedPreferenceLiveData(
        sharedPreferences = context.getSharedPreferences(context.getPreferenceName(), Context.MODE_PRIVATE),
        default = default,
        key = key
    )


}