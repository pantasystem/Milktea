package net.pantasystem.milktea.setting.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import net.pantasystem.milktea.common.getPreferenceName

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

    val enabled = MutableLiveData(true)
    val title = context.getString(titleStringRes)
    val choice = BooleanSharedPreferenceLiveData(
        sharedPreferences = context.getSharedPreferences(context.getPreferenceName(), Context.MODE_PRIVATE),
        default = default,
        key = key
    )


}