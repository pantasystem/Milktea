package net.pantasystem.milktea.data.model.account

import android.content.ContentResolver
import android.content.Context
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.UseCase
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.page.PageableTemplate
import net.pantasystem.milktea.data.model.UseCase

interface PageDefaultStrings {
    val homeTimeline: String
    val hybridThrowable: String
    val globalTimeline: String
}

class PageDefaultStringsOnAndroid(val context: Context) : PageDefaultStrings {
    override val globalTimeline: String
        get() = context.getString(R.string.global_timeline)
    override val homeTimeline: String
        get() = context.getString(R.string.home_timeline)
    override val hybridThrowable: String
        get() = context.getString(R.string.hybrid_timeline)
}

class PageDefaultStringsJp : PageDefaultStrings {
    override val globalTimeline: String
        get() = "グローバル"
    override val homeTimeline: String
        get() = "ホーム"
    override val hybridThrowable: String
        get() = "ソーシャル"
}

class MakeDefaultPagesUseCase(
    private val pageDefaultStrings: PageDefaultStrings
) : UseCase {

    operator fun invoke(account: Account, meta: Meta?) : List<Page> {
        val isGlobalEnabled = !(meta?.disableGlobalTimeline ?: false)
        val isLocalEnabled = !(meta?.disableLocalTimeline ?: false)
        val defaultPages = ArrayList<Page>()
        defaultPages.add(PageableTemplate(account).homeTimeline(pageDefaultStrings.homeTimeline))
        if (isLocalEnabled) {
            defaultPages.add(PageableTemplate(account).hybridTimeline(pageDefaultStrings.hybridThrowable))
        }
        if (isGlobalEnabled) {
            defaultPages.add(PageableTemplate(account).globalTimeline(pageDefaultStrings.globalTimeline))
        }
        return defaultPages.mapIndexed { index, page ->
            page.also {
                page.weight = index
            }
        }
    }
}