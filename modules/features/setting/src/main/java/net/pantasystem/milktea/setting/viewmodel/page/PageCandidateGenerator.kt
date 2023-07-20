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

    suspend fun createPageCandidates(
        related: Account,
        currentAccount: Account?,
    ): List<PageCandidate> {

        val isSameAccount = related.accountId == currentAccount?.accountId || currentAccount == null
        val restrictionTypes = setOf(
            PageType.SEARCH,
            PageType.SEARCH_HASH,
            PageType.USER,
            PageType.DETAIL,
        )
        return when (related.instanceType) {
            Account.InstanceType.MISSKEY -> {
                generateMisskeyTabs(related)
            }
            Account.InstanceType.FIREFISH -> {
                generateFirefishTabs(related)
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                generateMastodonTabs(related)
            }
        }.filter {
            isSameAccount || !restrictionTypes.contains(it.type)
        }
    }

    private fun generateMastodonTabs(related: Account): List<PageCandidate> {
        return listOf(
            PageCandidate(
                related,
                PageType.MASTODON_HOME_TIMELINE,
                StringSource(R.string.home_timeline)
            ),
            PageCandidate(
                related,
                PageType.MASTODON_LOCAL_TIMELINE,
                StringSource(R.string.local_timeline)
            ),
            PageCandidate(
                related,
                PageType.MASTODON_PUBLIC_TIMELINE,
                StringSource(R.string.global_timeline),
            ),
            PageCandidate(
                related,
                PageType.NOTIFICATION,
                StringSource(R.string.notification)
            ),
            PageCandidate(
                related,
                PageType.FAVORITE,
                StringSource(R.string.favorite)
            ),
//                    PageType.MASTODON_HASHTAG_TIMELINE,
            PageCandidate(
                related,
                PageType.MASTODON_LIST_TIMELINE,
                StringSource(R.string.list),
            ),
            PageCandidate(
                related,
                PageType.MASTODON_BOOKMARK_TIMELINE,
                StringSource(R.string.bookmark)
            )
        )
    }

    private fun generateFirefishTabs(related: Account): List<PageCandidate> {
        return listOfNotNull(
            PageCandidate(
                related,
                PageType.HOME,
                StringSource(R.string.home_timeline)
            ),
            PageCandidate(
                related,
                PageType.LOCAL,
                StringSource(R.string.local_timeline)
            ),
            PageCandidate(
                related,
                PageType.SOCIAL,
                StringSource(R.string.hybrid_timeline)
            ),
            PageCandidate(
                related,
                PageType.GLOBAL,
                StringSource(R.string.global_timeline)
            ),
            PageCandidate(
                related,
                PageType.CALCKEY_RECOMMENDED_TIMELINE,
                StringSource(R.string.calckey_recomended_timeline)
            ),
            PageCandidate(
                related,
                PageType.ANTENNA,
                StringSource(R.string.antenna)
            ),
            PageCandidate(
                related,
                PageType.NOTIFICATION,
                StringSource(R.string.notification)
            ),
            PageCandidate(
                related,
                PageType.USER_LIST,
                StringSource(R.string.user_list)
            ),
            PageCandidate(
                related,
                PageType.MENTION,
                StringSource(R.string.mention)
            ),
            PageCandidate(
                related,
                PageType.FAVORITE,
                StringSource(R.string.favorite)
            ),
            PageCandidate(
                related,
                PageType.CHANNEL_TIMELINE,
                StringSource(R.string.channel)
            ),
            PageCandidate(
                related,
                PageType.CLIP_NOTES,
                StringSource(R.string.clip)
            ),
            PageCandidate(
                related,
                PageType.SEARCH,
                StringSource(R.string.search)
            ),
            PageCandidate(
                related,
                PageType.SEARCH_HASH,
                StringSource(R.string.tag)
            ),
            PageCandidate(
                related,
                PageType.FEATURED,
                StringSource(R.string.featured)
            ),
            PageCandidate(
                related,
                PageType.USER,
                StringSource(R.string.user)
            ),
            PageCandidate(
                related,
                PageType.DETAIL,
                StringSource(R.string.detail)
            ),
            PageCandidate(
                related,
                PageType.GALLERY_FEATURED,
                StringSource(R.string.featured) + StringSource("(") + StringSource(R.string.gallery) + StringSource(
                    ")"
                )
            ),
            PageCandidate(
                related,
                PageType.GALLERY_POPULAR,
                StringSource(R.string.popular_posts) + StringSource("(") + StringSource(
                    R.string.gallery
                ) + StringSource(")")
            ),
            PageCandidate(
                related,
                PageType.GALLERY_POSTS,
                StringSource(R.string.gallery),
            ),
            PageCandidate(
                related,
                PageType.MY_GALLERY_POSTS,
                StringSource(R.string.my_posts) + StringSource("(") + StringSource(R.string.gallery) + StringSource(
                    ")"
                ),
            ),
            PageCandidate(
                related,
                PageType.USERS_GALLERY_POSTS,
                StringSource(R.string.gallery) + StringSource("(User)")
            ),
            PageCandidate(
                related,
                PageType.I_LIKED_GALLERY_POSTS,
                StringSource(R.string.my_liking) + StringSource("(") + StringSource(R.string.gallery) + StringSource(
                    ")"
                ),
            ),
        )
    }


    private suspend fun generateMisskeyTabs(related: Account): List<PageCandidate> {
        val nodeInfo = nodeInfoRepository.find(related.getHost()).getOrNull()
        val version = nodeInfo?.type?.getVersion() ?: Version("0")
        val isCalckey = nodeInfo?.type is NodeInfo.SoftwareType.Misskey.Calckey
        return listOfNotNull(
            PageCandidate(
                related,
                PageType.HOME,
                StringSource(R.string.home_timeline)
            ),
            PageCandidate(
                related,
                PageType.LOCAL,
                StringSource(R.string.local_timeline)
            ),
            PageCandidate(
                related,
                PageType.SOCIAL,
                StringSource(R.string.hybrid_timeline)
            ),
            PageCandidate(
                related,
                PageType.GLOBAL,
                StringSource(R.string.global_timeline)
            ),
            if (isCalckey) PageCandidate(
                related,
                PageType.CALCKEY_RECOMMENDED_TIMELINE,
                StringSource(R.string.calckey_recomended_timeline)
            ) else null,
            if (version >= Version("12")) PageCandidate(
                related,
                PageType.ANTENNA,
                StringSource(R.string.antenna)
            ) else null,
            PageCandidate(
                related,
                PageType.NOTIFICATION,
                StringSource(R.string.notification)
            ),
            PageCandidate(
                related,
                PageType.USER_LIST,
                StringSource(R.string.user_list)
            ),
            PageCandidate(
                related,
                PageType.MENTION,
                StringSource(R.string.mention)
            ),
            PageCandidate(
                related,
                PageType.FAVORITE,
                StringSource(R.string.favorite)
            ),
            if (version >= Version("12")) PageCandidate(
                related,
                PageType.CHANNEL_TIMELINE,
                StringSource(R.string.channel)
            ) else null,
            if (version >= Version("12")) PageCandidate(
                related,
                PageType.CLIP_NOTES,
                StringSource(R.string.clip)
            ) else null,
            PageCandidate(
                related,
                PageType.SEARCH,
                StringSource(R.string.search)
            ),
            PageCandidate(
                related,
                PageType.SEARCH_HASH,
                StringSource(R.string.tag)
            ),
            PageCandidate(
                related,
                PageType.FEATURED,
                StringSource(R.string.featured)
            ),
            PageCandidate(
                related,
                PageType.USER,
                StringSource(R.string.user)
            ),
            PageCandidate(
                related,
                PageType.DETAIL,
                StringSource(R.string.detail)
            ),
        ) + if (version >= Version("12.75.0")) {
            listOf(
                PageCandidate(
                    related,
                    PageType.GALLERY_FEATURED,
                    StringSource(R.string.featured) + StringSource("(") + StringSource(R.string.gallery) + StringSource(
                        ")"
                    )
                ),
                PageCandidate(
                    related,
                    PageType.GALLERY_POPULAR,
                    StringSource(R.string.popular_posts) + StringSource("(") + StringSource(
                        R.string.gallery
                    ) + StringSource(")")
                ),
                PageCandidate(
                    related,
                    PageType.GALLERY_POSTS,
                    StringSource(R.string.gallery),
                ),
                PageCandidate(
                    related,
                    PageType.MY_GALLERY_POSTS,
                    StringSource(R.string.my_posts) + StringSource("(") + StringSource(R.string.gallery) + StringSource(
                        ")"
                    ),
                ),
                PageCandidate(
                    related,
                    PageType.USERS_GALLERY_POSTS,
                    StringSource(R.string.gallery) + StringSource("(User)")
                ),
                PageCandidate(
                    related,
                    PageType.I_LIKED_GALLERY_POSTS,
                    StringSource(R.string.my_liking) + StringSource("(") + StringSource(R.string.gallery) + StringSource(
                        ")"
                    ),
                ),

                )
        } else {
            emptyList()
        }
    }

}