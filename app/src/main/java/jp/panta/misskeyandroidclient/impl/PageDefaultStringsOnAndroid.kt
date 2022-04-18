package jp.panta.misskeyandroidclient.impl

import android.content.Context
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.account.PageDefaultStrings

class PageDefaultStringsOnAndroid(val context: Context) :
    net.pantasystem.milktea.model.account.PageDefaultStrings {
    override val globalTimeline: String
        get() = context.getString(R.string.global_timeline)
    override val homeTimeline: String
        get() = context.getString(R.string.home_timeline)
    override val hybridThrowable: String
        get() = context.getString(R.string.hybrid_timeline)
}