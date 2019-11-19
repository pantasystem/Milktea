package jp.panta.misskeyandroidclient.viewmodel.setting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import jp.panta.misskeyandroidclient.MainActivity

class MoveSettingActivityPanel<A : AppCompatActivity>(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val activity: Class<A>
): Shared{
    val a = MainActivity::class
}