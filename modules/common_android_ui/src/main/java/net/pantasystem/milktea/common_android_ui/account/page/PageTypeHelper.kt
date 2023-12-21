package net.pantasystem.milktea.common_android_ui.account.page

import android.content.Context
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.PageType.ANTENNA
import net.pantasystem.milktea.model.account.page.PageType.CALCKEY_RECOMMENDED_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.CHANNEL_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.CLIP_NOTES
import net.pantasystem.milktea.model.account.page.PageType.DETAIL
import net.pantasystem.milktea.model.account.page.PageType.FAVORITE
import net.pantasystem.milktea.model.account.page.PageType.FEATURED
import net.pantasystem.milktea.model.account.page.PageType.GALLERY_FEATURED
import net.pantasystem.milktea.model.account.page.PageType.GALLERY_POPULAR
import net.pantasystem.milktea.model.account.page.PageType.GALLERY_POSTS
import net.pantasystem.milktea.model.account.page.PageType.GLOBAL
import net.pantasystem.milktea.model.account.page.PageType.HOME
import net.pantasystem.milktea.model.account.page.PageType.I_LIKED_GALLERY_POSTS
import net.pantasystem.milktea.model.account.page.PageType.LOCAL
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_BOOKMARK_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_HOME_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_LIST_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_LOCAL_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_MENTION_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_PUBLIC_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_SEARCH_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_TAG_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_TREND_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MASTODON_USER_TIMELINE
import net.pantasystem.milktea.model.account.page.PageType.MENTION
import net.pantasystem.milktea.model.account.page.PageType.MY_GALLERY_POSTS
import net.pantasystem.milktea.model.account.page.PageType.NOTIFICATION
import net.pantasystem.milktea.model.account.page.PageType.SEARCH
import net.pantasystem.milktea.model.account.page.PageType.SEARCH_HASH
import net.pantasystem.milktea.model.account.page.PageType.SOCIAL
import net.pantasystem.milktea.model.account.page.PageType.USER
import net.pantasystem.milktea.model.account.page.PageType.USERS_GALLERY_POSTS
import net.pantasystem.milktea.model.account.page.PageType.USER_LIST


object PageTypeHelper{


    fun nameByPageType(context: Context, pageType: PageType): String{

        return when(pageType){
            GLOBAL -> context.getString(R.string.global_timeline)
            SOCIAL -> context.getString(R.string.hybrid_timeline)
            LOCAL -> context.getString(R.string.local_timeline)
            HOME -> context.getString(R.string.home_timeline)
            SEARCH -> context.getString(R.string.search)
            SEARCH_HASH -> context.getString(R.string.tag)
            USER -> context.getString(R.string.user)
            FAVORITE -> context.getString(R.string.favorite)
            FEATURED -> context.getString(R.string.featured)
            DETAIL -> context.getString(R.string.conversation)
            USER_LIST -> context.getString(R.string.list)
            MENTION -> context.getString(R.string.mention)
            NOTIFICATION -> context.getString(R.string.notification)
            ANTENNA -> context.getString(R.string.antenna)
            GALLERY_FEATURED -> context.getString(R.string.featured) + String.format("(%s)", context.getString(R.string.gallery))
            GALLERY_POPULAR -> context.getString(R.string.popular_posts) + String.format("(%s)", context.getString(R.string.gallery))
            GALLERY_POSTS -> context.getString(R.string.gallery)
            USERS_GALLERY_POSTS -> context.getString(R.string.gallery) + "(User)"
            MY_GALLERY_POSTS -> context.getString(R.string.my_posts) + String.format("(%s)", context.getString(R.string.gallery))
            I_LIKED_GALLERY_POSTS -> context.getString(R.string.my_liking) + String.format("(%s)", context.getString(R.string.gallery))
            CHANNEL_TIMELINE -> context.getString(R.string.channel)
            MASTODON_LOCAL_TIMELINE -> context.getString(R.string.local_timeline)
            MASTODON_PUBLIC_TIMELINE -> context.getString(R.string.global_timeline)
            MASTODON_HOME_TIMELINE -> context.getString(R.string.home_timeline)
            MASTODON_LIST_TIMELINE -> context.getString(R.string.list)
            MASTODON_USER_TIMELINE -> context.getString(R.string.user)
            CALCKEY_RECOMMENDED_TIMELINE -> context.getString(R.string.calckey_recomended_timeline)
            CLIP_NOTES -> context.getString(R.string.clip)
            MASTODON_BOOKMARK_TIMELINE -> context.getString(R.string.bookmark)
            MASTODON_SEARCH_TIMELINE -> context.getString(R.string.search)
            MASTODON_TAG_TIMELINE -> context.getString(R.string.tag)
            MASTODON_TREND_TIMELINE -> context.getString(R.string.featured)
            MASTODON_MENTION_TIMELINE -> context.getString(R.string.mention)
        }
    }
}