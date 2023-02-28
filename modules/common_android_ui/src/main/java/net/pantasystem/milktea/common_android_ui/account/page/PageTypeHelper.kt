package net.pantasystem.milktea.common_android_ui.account.page

import android.content.Context
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.PageType.*


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
            MASTODON_HASHTAG_TIMELINE -> context.getString(R.string.tag)
            MASTODON_LIST_TIMELINE -> context.getString(R.string.list)
            MASTODON_USER_TIMELINE -> context.getString(R.string.user)
            CALCKEY_RECOMMENDED_TIMELINE -> context.getString(R.string.calckey_recomended_timeline)
            CLIP_NOTES -> context.getString(R.string.clip)
        }
    }
}