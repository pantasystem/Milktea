package net.pantasystem.milktea.model.account

import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.instance.Meta

interface PageDefaultStrings {
    val homeTimeline: String
    val hybridThrowable: String
    val globalTimeline: String
    val localTimeline: String
}


class PageDefaultStringsJp : PageDefaultStrings {
    override val globalTimeline: String
        get() = "グローバル"
    override val homeTimeline: String
        get() = "ホーム"
    override val hybridThrowable: String
        get() = "ソーシャル"

    override val localTimeline: String
        get() = "ローカル"
}

class MakeDefaultPagesUseCase(
    private val pageDefaultStrings: PageDefaultStrings
) : UseCase {

    operator fun invoke(account: Account, meta: Meta?) : List<Page> {
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
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
                defaultPages
            }
            Account.InstanceType.MASTODON -> {
                listOf(
                    PageableTemplate(account).mastodonHomeTimeline(pageDefaultStrings.homeTimeline),
                    PageableTemplate(account).mastodonLocalTimeline(pageDefaultStrings.localTimeline),
                    PageableTemplate(account).mastodonPublicTimeline(pageDefaultStrings.globalTimeline),
                )
            }
        }.mapIndexed { index, page ->
            page.also {
                page.weight = index
            }
        }

    }
}