package jp.panta.misskeyandroidclient.ui.settings.page

import android.content.Context
import net.pantasystem.milktea.model.account.page.PageType
import java.lang.ref.WeakReference

class PageTypeNameMap (
    context: Context
){
    private val contextWrapper = WeakReference(context)
    fun get(type: net.pantasystem.milktea.model.account.page.PageType): String{
        val context = contextWrapper.get()
        return context?.let{
            PageTypeHelper.nameByPageType(it, type)
        }?: type.defaultName
    }

}

