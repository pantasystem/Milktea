package jp.panta.misskeyandroidclient.ui.settings.page

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.pantasystem.milktea.model.account.page.PageType
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageTypeNameMap @Inject constructor(
    @ApplicationContext context: Context
){
    private val contextWrapper = WeakReference(context)
    fun get(type: PageType): String{
        val context = contextWrapper.get()
        return context?.let{
            PageTypeHelper.nameByPageType(it, type)
        }?: type.defaultName
    }

}

