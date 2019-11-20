package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import jp.panta.misskeyandroidclient.MainActivity

class MoveSettingActivityPanel<A : AppCompatActivity>(
    @StringRes val titleStringRes: Int,
    val activity: Class<A>,
    val context: Context
): Shared{
    val title = context.getString(titleStringRes)
}