package net.pantasystem.milktea.model.account.page


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.pantasystem.milktea.model.account.page.PageType.*
import java.io.Serializable

@Parcelize
data class PageParams(
    val type: PageType = HOME,
    val withFiles: Boolean? = null,
    var excludeNsfw: Boolean? = null,
    var includeLocalRenotes: Boolean? = null,
    var includeMyRenotes: Boolean? = null,
    var includeRenotedMyRenotes: Boolean? = null,
    val listId: String? = null,
    val following: Boolean? = null,
    val visibility: String? = null,
    val noteId: String? = null,
    val tag: String? = null,
    var reply: Boolean? = null,
    var renote: Boolean? = null,
    var poll: Boolean? = null,
    val offset: Int? = null,
    var markAsRead: Boolean? = null,
    val userId: String? = null,
    var includeReplies: Boolean? = null,
    var query: String? = null,
    var host: String? = null,
    val antennaId: String? = null,
    val channelId: String? = null,
    val clipId: String? = null,
) : Serializable, Parcelable {


    // * Global, Local, Hybrid, Home, UserList, Mention, Show, SearchByTag, Featured, Notification, UserTimeline, Search, Antenna
    @Throws(IllegalStateException::class)
    fun toPageable(): Pageable {
        try {
            return when (this.type) {
                HOME -> {
                    Pageable.HomeTimeline(
                        withFiles = withFiles,
                        includeLocalRenotes = includeLocalRenotes,
                        includeMyRenotes = includeMyRenotes,
                        includeRenotedMyRenotes = includeRenotedMyRenotes
                    )
                }
                LOCAL -> {
                    Pageable.LocalTimeline(
                        withFiles = withFiles,
                        excludeNsfw = excludeNsfw
                    )
                }
                SOCIAL -> {
                    Pageable.HybridTimeline(
                        withFiles = withFiles,
                        includeRenotedMyRenotes = includeRenotedMyRenotes,
                        includeMyRenotes = includeMyRenotes,
                        includeLocalRenotes = includeLocalRenotes
                    )
                }
                GLOBAL -> {
                    Pageable.GlobalTimeline(
                        withFiles = withFiles
                    )
                }
                SEARCH -> {
                    Pageable.Search(
                        query = query!!,
                        host = host,
                        userId = userId
                    )
                }
                SEARCH_HASH -> {
                    Pageable.SearchByTag(
                        tag = tag!!,
                        reply = reply,
                        renote = renote,
                        withFiles = withFiles,
                        poll = poll
                    )
                }
                USER -> {
                    Pageable.UserTimeline(
                        userId = userId!!,
                        includeMyRenotes = includeMyRenotes,
                        includeReplies = includeReplies,
                        withFiles = withFiles
                    )
                }
                FAVORITE -> {
                    Pageable.Favorite
                }
                FEATURED -> {
                    Pageable.Featured(offset = offset)
                }
                DETAIL -> {
                    Pageable.Show(
                        noteId = noteId!!
                    )
                }
                USER_LIST -> {
                    Pageable.UserListTimeline(
                        listId = listId!!,
                        withFiles = withFiles,
                        includeMyRenotes = includeMyRenotes,
                        includeLocalRenotes = includeLocalRenotes,
                        includeRenotedMyRenotes = includeRenotedMyRenotes
                    )
                }
                MENTION -> {
                    Pageable.Mention(
                        following = following,
                        visibility = visibility
                    )
                }
                ANTENNA -> {
                    Pageable.Antenna(
                        antennaId = antennaId!!
                    )
                }
                NOTIFICATION -> {
                    Pageable.Notification(
                        following = following,
                        markAsRead = markAsRead
                    )

                }
                GALLERY_POPULAR -> {
                    Pageable.Gallery.Popular
                }
                GALLERY_FEATURED -> {
                    Pageable.Gallery.Featured
                }
                GALLERY_POSTS -> {
                    Pageable.Gallery.Posts
                }
                USERS_GALLERY_POSTS -> {
                    Pageable.Gallery.User(userId!!)
                }
                I_LIKED_GALLERY_POSTS -> {
                    Pageable.Gallery.ILikedPosts
                }
                MY_GALLERY_POSTS -> {
                    Pageable.Gallery.MyPosts
                }
                CHANNEL_TIMELINE -> {
                    Pageable.ChannelTimeline(channelId!!)
                }
                MASTODON_LOCAL_TIMELINE -> {
                    Pageable.Mastodon.LocalTimeline(
                        isOnlyMedia = withFiles
                    )
                }
                MASTODON_PUBLIC_TIMELINE -> {
                    Pageable.Mastodon.PublicTimeline(
                        isOnlyMedia = withFiles,
                    )
                }
                MASTODON_HOME_TIMELINE -> {
                    Pageable.Mastodon.HomeTimeline
                }
                MASTODON_LIST_TIMELINE -> {
                    Pageable.Mastodon.ListTimeline(
                        listId = requireNotNull(listId),
                    )
                }
                MASTODON_USER_TIMELINE -> {
                    Pageable.Mastodon.UserTimeline(
                        userId = requireNotNull(userId),
                        isOnlyMedia = withFiles,
                        excludeReblogs = includeMyRenotes?.not(),
                        excludeReplies = includeReplies?.not()
                    )
                }
                CALCKEY_RECOMMENDED_TIMELINE -> {
                    Pageable.CalckeyRecommendedTimeline
                }
                CLIP_NOTES -> {
                    Pageable.ClipNotes(
                        clipId = requireNotNull(clipId)
                    )
                }
                MASTODON_BOOKMARK_TIMELINE -> {
                    Pageable.Mastodon.BookmarkTimeline
                }
                MASTODON_SEARCH_TIMELINE -> {
                    Pageable.Mastodon.SearchTimeline(
                        query = requireNotNull(query),
                        userId = userId,
                    )
                }
                MASTODON_TAG_TIMELINE -> {
                    Pageable.Mastodon.HashTagTimeline(
                        requireNotNull(tag)
                    )
                }
                MASTODON_TREND_TIMELINE -> {
                    Pageable.Mastodon.TrendTimeline
                }
            }
        } catch (e: NullPointerException) {
            throw IllegalStateException("パラメーターに問題があります: $this")
        }

    }

}