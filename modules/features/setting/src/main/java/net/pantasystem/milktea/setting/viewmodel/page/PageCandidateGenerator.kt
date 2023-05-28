package net.pantasystem.milktea.setting.viewmodel.page

import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import net.pantasystem.milktea.setting.R
import javax.inject.Inject

class PageCandidateGenerator @Inject constructor(
    private val nodeInfoRepository: NodeInfoRepository,
) {

    suspend fun createPageCandidates(account: Account): List<PageCandidate> {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val version = nodeInfo?.type?.getVersion() ?: Version("0")
        val isCalckey = nodeInfo?.type is NodeInfo.SoftwareType.Misskey.Calckey
        return when (account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                listOfNotNull(
                    PageCandidate(
                        account,
                        PageType.HOME,
                        StringSource(R.string.home_timeline)
                    ),
                    PageCandidate(
                        account,
                        PageType.LOCAL,
                        StringSource(R.string.local_timeline)
                    ),
                    PageCandidate(
                        account,
                        PageType.SOCIAL,
                        StringSource(R.string.hybrid_timeline)
                    ),
                    PageCandidate(
                        account,
                        PageType.GLOBAL,
                        StringSource(R.string.global_timeline)
                    ),
                    if (isCalckey) PageCandidate(
                        account,
                        PageType.CALCKEY_RECOMMENDED_TIMELINE,
                        StringSource(R.string.calckey_recomended_timeline)
                    ) else null,
                    if (version >= Version("12")) PageCandidate(
                        account,
                        PageType.ANTENNA,
                        StringSource(R.string.antenna)
                    ) else null,
                    PageCandidate(
                        account,
                        PageType.NOTIFICATION,
                        StringSource(R.string.notification)
                    ),
                    PageCandidate(
                        account,
                        PageType.USER_LIST,
                        StringSource(R.string.user_list)
                    ),
                    PageCandidate(
                        account,
                        PageType.MENTION,
                        StringSource(R.string.mention)
                    ),
                    PageCandidate(
                        account,
                        PageType.FAVORITE,
                        StringSource(R.string.favorite)
                    ),
                    if (version >= Version("12")) PageCandidate(
                        account,
                        PageType.CHANNEL_TIMELINE,
                        StringSource(R.string.channel)
                    ) else null,
                    if (version >= Version("12")) PageCandidate(
                        account,
                        PageType.CLIP_NOTES,
                        StringSource(R.string.clip)
                    ) else null,
                    PageCandidate(
                        account,
                        PageType.SEARCH,
                        StringSource(R.string.search)
                    ),
                    PageCandidate(
                        account,
                        PageType.SEARCH_HASH,
                        StringSource(R.string.tag)
                    ),
                    PageCandidate(
                        account,
                        PageType.FEATURED,
                        StringSource(R.string.featured)
                    ),
                    PageCandidate(
                        account,
                        PageType.USER,
                        StringSource(R.string.user)
                    ),
                    PageCandidate(
                        account,
                        PageType.DETAIL,
                        StringSource(R.string.detail)
                    ),
                ) + if (version >= Version("12.75.0")) {
                    listOf(
                        PageCandidate(
                            account,
                            PageType.GALLERY_FEATURED,
                            StringSource(R.string.featured) + StringSource("(") + StringSource(R.string.gallery) + StringSource(")")
                        ),
                        PageCandidate(
                            account,
                            PageType.GALLERY_POPULAR,
                            StringSource(R.string.popular_posts) + StringSource("(") + StringSource(
                                R.string.gallery) + StringSource(")")
                        ),
                        PageCandidate(
                            account,
                            PageType.GALLERY_POSTS,
                            StringSource(R.string.gallery),
                        ),
                        PageCandidate(
                            account,
                            PageType.MY_GALLERY_POSTS,
                            StringSource(R.string.my_posts) + StringSource("(") + StringSource(R.string.gallery) + StringSource(")"),
                        ),
                        PageCandidate(
                            account,
                            PageType.USERS_GALLERY_POSTS,
                            StringSource(R.string.gallery) + StringSource("(User)")
                        ),
                        PageCandidate(
                            account,
                            PageType.I_LIKED_GALLERY_POSTS,
                            StringSource(R.string.my_liking) + StringSource("(") + StringSource(R.string.gallery) + StringSource(")"),
                        ),
                        PageCandidate(
                            account,
                            PageType.MY_GALLERY_POSTS,
                            StringSource(R.string.my_posts) + StringSource("(") + StringSource(R.string.gallery) + StringSource(")"),
                        )
                    )
                } else {
                    emptyList()
                }
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                listOf(
                    PageCandidate(
                        account,
                        PageType.MASTODON_HOME_TIMELINE,
                        StringSource(R.string.home_timeline)
                    ),
                    PageCandidate(
                        account,
                        PageType.MASTODON_LOCAL_TIMELINE,
                        StringSource(R.string.local_timeline)
                    ),
                    PageCandidate(
                        account,
                        PageType.MASTODON_PUBLIC_TIMELINE,
                        StringSource(R.string.global_timeline),
                    ),
                    PageCandidate(
                        account,
                        PageType.NOTIFICATION,
                        StringSource(R.string.notification)
                    ),
                    PageCandidate(
                        account,
                        PageType.FAVORITE,
                        StringSource(R.string.favorite)
                    ),
//                    PageType.MASTODON_HASHTAG_TIMELINE,
                    PageCandidate(
                        account,
                        PageType.MASTODON_LIST_TIMELINE,
                        StringSource(R.string.list),
                    ),
                    PageCandidate(
                        account,
                        PageType.MASTODON_BOOKMARK_TIMELINE,
                        StringSource(R.string.bookmark)
                    )
                )
            }
        }
    }

}