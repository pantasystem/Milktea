@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure

import androidx.room.*
import androidx.room.ForeignKey.NO_ACTION
import net.pantasystem.milktea.data.infrastructure.core.Account
import net.pantasystem.milktea.model.account.page.Pageable

@Deprecated("model.account.pages.Pageへ移行")
@Entity(
    tableName = "page",
    foreignKeys = [ForeignKey(
        childColumns = ["accountId"],
        parentColumns = ["id"],
        entity = Account::class,
        onDelete = NO_ACTION,
        onUpdate = NO_ACTION
    )],
    indices = [
        Index("accountId")
    ]
)
data class Page(
    var accountId: String?,
    var title: String,
    var pageNumber: Int?,
    @Embedded(prefix = "global_timeline_") val globalTimeline: GlobalTimeline? = null,
    @Embedded(prefix = "local_timeline_") val localTimeline: LocalTimeline? = null,
    @Embedded(prefix = "hybrid_timeline_") val hybridTimeline: HybridTimeline? = null,
    @Embedded(prefix = "home_timeline_") val homeTimeline: HomeTimeline? = null,
    @Embedded(prefix = "user_list_timeline_") val userListTimeline: UserListTimeline? = null,
    @Embedded(prefix = "mention_") val mention: Mention? = null,
    @Embedded(prefix = "show_") val show: Show? = null,
    @Embedded(prefix = "tag_") val searchByTag: SearchByTag? = null,
    @Embedded(prefix = "featured_") val featured: Featured? = null,
    @Embedded(prefix = "notification_") val notification: Notification? = null,
    @Embedded(prefix = "user_") val userTimeline: UserTimeline? = null,
    @Embedded(prefix = "search_") val search: Search? = null,
    @Embedded(prefix = "favorite_") val favorite: Favorite? = null,
    @Embedded(prefix = "antenna_") val antenna: Antenna? = null

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @Suppress("DEPRECATION")
    @Ignore
    fun pageable(): PageableOld? {
        return when {
            globalTimeline != null -> globalTimeline
            localTimeline != null -> localTimeline
            hybridTimeline != null -> hybridTimeline
            homeTimeline != null -> homeTimeline
            userListTimeline != null -> userListTimeline
            mention != null -> mention
            show != null -> show
            searchByTag != null -> searchByTag
            featured != null -> featured
            notification != null -> notification
            userTimeline != null -> userTimeline
            search != null -> search
            favorite != null -> favorite
            antenna != null -> antenna
            else -> null
        }
    }

    @Ignore
    fun toPage(): net.pantasystem.milktea.model.account.page.Page? {
        this.pageable()?.toPageable()?.let {
            return net.pantasystem.milktea.model.account.page.Page(0L, this.title, 0, it)
        }
        return null
    }

    abstract class Timeline : PageableOld

    data class GlobalTimeline(
        @ColumnInfo(name = "with_files") var withFiles: Boolean? = null,
        override val type: PageType = PageType.GLOBAL
    ) : PageableOld, Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.GlobalTimeline(
                withFiles = withFiles
            )
        }

    }

    data class LocalTimeline(
        @ColumnInfo(name = "with_files") var withFiles: Boolean? = null,
        @ColumnInfo(name = "exclude_nsfw") var excludeNsfw: Boolean? = null,
        override val type: PageType = PageType.LOCAL
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.LocalTimeline(
                withFiles = withFiles,
                excludeNsfw = excludeNsfw
            )
        }
    }

    /**
     * @param includeLocalRenotes 全体設定に従う場合はnullそうでない場合はパラメーターが指定される
     * @param includeMyRenotes 上記と同じく
     * @param includeRenotedMyRenotes 上記と同じく
     */
    data class HybridTimeline(
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        override val type: PageType = PageType.SOCIAL
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.HybridTimeline(
                withFiles = withFiles,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes,
                includeRenotedMyRenotes = includeRenotedMyRenotes
            )
        }
    }

    data class HomeTimeline(
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        override val type: PageType = PageType.HOME
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.HomeTimeline(
                withFiles = withFiles,
                includeRenotedMyRenotes = includeRenotedMyRenotes,
                includeLocalRenotes = includeLocalRenotes,
                includeMyRenotes = includeMyRenotes

            )
        }
    }

    data class UserListTimeline(
        val listId: String,
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        override val type: PageType = PageType.USER_LIST
    ) : Timeline() {

        override fun toPageable(): Pageable {
            return Pageable.UserListTimeline(
                listId = listId,
                withFiles = withFiles,
                includeMyRenotes = includeMyRenotes,
                includeLocalRenotes = includeLocalRenotes,
                includeRenotedMyRenotes = includeRenotedMyRenotes
            )
        }
    }

    data class Mention(
        val following: Boolean?, val
        visibility: String? = null,
        override val type: PageType = PageType.MENTION
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Mention(
                following = following,
                visibility = visibility
            )
        }
    }

    data class Show(
        val noteId: String,
        override val type: PageType = PageType.DETAIL
    ) : PageableOld {
        override fun toPageable(): Pageable {
            return Pageable.Show(
                noteId = noteId
            )
        }
    }

    data class SearchByTag(
        val tag: String,
        var reply: Boolean? = null,
        var renote: Boolean? = null,
        var withFiles: Boolean? = null,
        var poll: Boolean? = null,
        override val type: PageType = PageType.SEARCH_HASH
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.SearchByTag(
                tag = tag,
                reply = reply,
                renote = renote,
                withFiles = withFiles,
                poll = poll
            )
        }
    }

    data class Featured(
        val offset: Int?,
        override val type: PageType = PageType.FEATURED
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Featured(
                offset = offset
            )
        }
    }

    data class Notification(
        var following: Boolean? = null,
        var markAsRead: Boolean? = null,
        override val type: PageType = PageType.NOTIFICATION
    ) : PageableOld {
        override fun toPageable(): Pageable {
            return Pageable.Notification(
                following = following,
                markAsRead = markAsRead
            )
        }
    }

    data class UserTimeline(
        val userId: String,
        var includeReplies: Boolean = true,
        var includeMyRenotes: Boolean? = true,
        var withFiles: Boolean? = null,
        override val type: PageType = PageType.USER
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.UserTimeline(
                userId = userId,
                includeMyRenotes = includeMyRenotes,
                includeReplies = includeReplies,
                withFiles = withFiles
            )
        }
    }

    data class Search(
        var query: String, var host: String? = null, var userId: String? = null,
        override val type: PageType = PageType.SEARCH
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Search(
                query = query, host = host, userId = userId
            )
        }
    }

    data class Antenna(
        val antennaId: String,
        override val type: PageType = PageType.ANTENNA
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Antenna(
                antennaId = antennaId
            )
        }
    }

    class Favorite(override val type: PageType = PageType.FAVORITE) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Favorite
        }
    }
}