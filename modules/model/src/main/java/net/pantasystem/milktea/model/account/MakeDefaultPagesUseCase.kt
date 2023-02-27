package net.pantasystem.milktea.model.account

import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository

interface PageDefaultStrings {
    val homeTimeline: String
    val hybridThrowable: String
    val globalTimeline: String
    val localTimeline: String
    val recommendedTimeline: String
    val media: String
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
    override val recommendedTimeline: String
        get() = "一押し"

    override val media: String
        get() = "メディア"
}

class MakeDefaultPagesUseCase(
    private val pageDefaultStrings: PageDefaultStrings,
    private val nodeInfoRepository: NodeInfoRepository,
) : UseCase {

    operator fun invoke(account: Account, meta: Meta?) : List<Page> {
        val nodeInfo = nodeInfoRepository.get(account.getHost())
        val isCalckey = nodeInfo?.type is NodeInfo.SoftwareType.Misskey.Calckey
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                val isGlobalEnabled = !(meta?.disableGlobalTimeline ?: false)
                val isLocalEnabled = !(meta?.disableLocalTimeline ?: false)
                val defaultPages = ArrayList<Page>()
                defaultPages.add(PageableTemplate(account).homeTimeline(pageDefaultStrings.homeTimeline))
                if (isLocalEnabled) {
                    defaultPages.add(PageableTemplate(account).hybridTimeline(pageDefaultStrings.hybridThrowable))
                }
                if (isLocalEnabled) {
                    defaultPages.add(PageableTemplate(account).hybridTimeline(pageDefaultStrings.media, withFiles = true))
                } else {
                    defaultPages.add(PageableTemplate(account).homeTimeline(pageDefaultStrings.media, withFiles = true))
                }
                if (isGlobalEnabled) {
                    defaultPages.add(PageableTemplate(account).globalTimeline(pageDefaultStrings.globalTimeline))
                }
                if (isCalckey) {
                    defaultPages.add(PageableTemplate(account).calckeyRecommendedTimeline(pageDefaultStrings.recommendedTimeline))
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