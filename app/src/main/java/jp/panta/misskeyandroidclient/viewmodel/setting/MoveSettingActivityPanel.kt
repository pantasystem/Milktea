package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.util.eventbus.EventBus

class MoveSettingActivityPanel<A : AppCompatActivity>(
    @StringRes val titleStringRes: Int,
    val activity: Class<A>,
    val context: Context
): Shared{
    val title = context.getString(titleStringRes)
    val startActivityEventBus: EventBus<Class<A>> = EventBus()


    fun startActivity(){
        startActivityEventBus.event = activity
    }
}