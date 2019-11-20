package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.SharedPreferences
import androidx.annotation.StringRes

abstract class SharedItem: Shared{
    abstract val key: String
    abstract val titleStringRes: Int

}