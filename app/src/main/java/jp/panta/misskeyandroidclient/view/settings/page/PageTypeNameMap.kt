package jp.panta.misskeyandroidclient.view.settings.page

import android.content.Context
import jp.panta.misskeyandroidclient.model.PageType
import java.lang.ref.WeakReference

class PageTypeNameMap (
    context: Context
){
    private val contextWrapper = WeakReference(context)
    fun get(type: PageType): String{
        val context = contextWrapper.get()
        return context?.let{
            PageTypeHelper.nameByPageType(it, type)
        }?: type.defaultName
    }

}

