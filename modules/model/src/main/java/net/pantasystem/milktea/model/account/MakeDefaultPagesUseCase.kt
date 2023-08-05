package net.pantasystem.milktea.model.account

import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.instance.MetaRepository
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
    private val metaRepository: MetaRepository,
) : UseCase {

    operator suspend fun invoke(account: Account) : List<Page> {
        val nodeInfo = nodeInfoRepository.get(account.getHost())
        val isCalckey = nodeInfo?.type is NodeInfo.SoftwareType.Misskey.Calckey
        val isFirefish = nodeInfo?.type is NodeInfo.SoftwareType.Firefish
        return when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val meta = metaRepository.find(account.normalizedInstanceUri).getOrNull()
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
                if (isCalckey || isFirefish) {
                    defaultPages.add(PageableTemplate(account).calckeyRecommendedTimeline(pageDefaultStrings.recommendedTimeline))
                }
                defaultPages
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
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