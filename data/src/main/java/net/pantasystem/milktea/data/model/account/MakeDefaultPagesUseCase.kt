package net.pantasystem.milktea.data.model.account

import android.content.Context
import net.pantasystem.milktea.data.model.UseCase
import net.pantasystem.milktea.data.model.account.page.Page
import net.pantasystem.milktea.data.model.account.page.PageableTemplate
import net.pantasystem.milktea.data.model.instance.Meta

interface PageDefaultStrings {
    val homeTimeline: String
    val hybridThrowable: String
    val globalTimeline: String
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