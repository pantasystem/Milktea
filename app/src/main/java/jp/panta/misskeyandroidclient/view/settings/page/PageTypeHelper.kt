package jp.panta.misskeyandroidclient.view.settings.page

import android.content.Context
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.account.page.PageType.*
import jp.panta.misskeyandroidclient.model.account.page.PageType


object PageTypeHelper{

    @JvmStatic
    @BindingAdapter("pageType")
    fun TextView.setTextFromPageType(type: PageType?){
        type?: return
        this.text = nameByPageType(context, type)



    }

    fun nameByPageType(context: Context, pageType: PageType): String{

        return when(pageType){
            GLOBAL -> context.getString(R.string.global_timeline)
            SOCIAL -> context.getString(R.string.hybrid_timeline)
            LOCAL -> context.getString(R.string.local_timeline)
            HOME -> context.getString(R.string.home_timeline)
            SEARCH -> context.getString(R.string.search)
            SEARCH_HASH -> context.getString(R.string.tag)
            USER -> context.getString(R.string.user)
            FAVORITE -> context.getString(R.string.favorite)
            FEATURED -> context.getString(R.string.featured)
            DETAIL -> context.getString(R.string.conversation)
            USER_LIST -> context.getString(R.string.list)
            MENTION -> context.getString(R.string.mention)
            NOTIFICATION -> context.getString(R.string.notification)
            ANTENNA -> context.getString(R.string.antenna)
        }
    }
}