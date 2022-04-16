package jp.panta.misskeyandroidclient.ui.settings.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import net.pantasystem.milktea.common.getPreferenceName

class TextSharedItem(
    override val key: String,
    @StringRes override val titleStringRes: Int,
    val default: String,
    val type: InputType,
    val context: Context
) : SharedItem(){

    enum class InputType{
        TEXT, NUMBER
    }

    val title = context.getString(titleStringRes)

    val text = MultiTypeSharedPreferenceLiveData(
        sharedPreferences = context.getSharedPreferences(context.getPreferenceName(), Context.MODE_PRIVATE),
        default = default,
        key = key,
        type = if(type == InputType.TEXT) {
            MultiTypeSharedPreferenceLiveData.Type.STRING
        }else {
            MultiTypeSharedPreferenceLiveData.Type.INTEGER
        }

    )





}