package net.pantasystem.milktea.setting.viewmodel

import android.content.Context
import androidx.annotation.StringRes

class Group(
    @StringRes val titleStringRes: Int?,
    val items: List<Shared>,
    val context: Context
) : Shared {
    val title: String = if(titleStringRes == null){
        ""
    }else{
        context.getString(titleStringRes)
    }
}