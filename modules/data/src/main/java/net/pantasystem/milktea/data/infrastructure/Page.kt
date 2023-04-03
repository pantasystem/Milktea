@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.data.infrastructure

import androidx.room.*
import net.pantasystem.milktea.data.infrastructure.core.Account
import net.pantasystem.milktea.model.account.page.Pageable

@Deprecated("model.account.pages.Pageへ移行")
@Entity(
    tableName = "page",
    foreignKeys = [ForeignKey(
        childColumns = ["accountId"],
        parentColumns = ["id"],
        entity = Account::class,
        onDelete = ForeignKey.NO_ACTION,
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [
        Index("accountId")
    ]
)
data class Page(
    @ColumnInfo(name = "accountId")
    var accountId: String?,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "pageNumber")
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

    abstract class Timeline : PageableOld

    data class GlobalTimeline(
        @ColumnInfo(name = "with_files")
        var withFiles: Boolean? = null,

        @ColumnInfo(name = "type")
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

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "withFiles")
        var withFiles: Boolean? = null,

        @ColumnInfo(name = "includeLocalRenotes")
        var includeLocalRenotes: Boolean? = null,

        @ColumnInfo(name = "includeMyRenotes")
        var includeMyRenotes: Boolean? = null,

        @ColumnInfo(name = "includeRenotedMyRenotes")
        var includeRenotedMyRenotes: Boolean? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "withFiles")
        var withFiles: Boolean? = null,

        @ColumnInfo(name = "includeLocalRenotes")
        var includeLocalRenotes: Boolean? = null,

        @ColumnInfo(name = "includeMyRenotes")
        var includeMyRenotes: Boolean? = null,

        @ColumnInfo(name = "includeRenotedMyRenotes")
        var includeRenotedMyRenotes: Boolean? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "listId")
        val listId: String,

        @ColumnInfo(name = "withFiles")
        var withFiles: Boolean? = null,

        @ColumnInfo(name = "includeLocalRenotes")
        var includeLocalRenotes: Boolean? = null,

        @ColumnInfo(name = "includeMyRenotes")
        var includeMyRenotes: Boolean? = null,

        @ColumnInfo(name = "includeRenotedMyRenotes")
        var includeRenotedMyRenotes: Boolean? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "following")
        val following: Boolean?,

        @ColumnInfo(name = "visibility")
        val visibility: String? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "noteId")
        val noteId: String,

        @ColumnInfo(name = "type")
        override val type: PageType = PageType.DETAIL
    ) : PageableOld {
        override fun toPageable(): Pageable {
            return Pageable.Show(
                noteId = noteId
            )
        }
    }

    data class SearchByTag(
        @ColumnInfo(name = "tag")
        val tag: String,

        @ColumnInfo(name = "reply")
        var reply: Boolean? = null,

        @ColumnInfo(name = "renote")
        var renote: Boolean? = null,

        @ColumnInfo(name = "withFiles")
        var withFiles: Boolean? = null,

        @ColumnInfo(name = "poll")
        var poll: Boolean? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "offset")
        val offset: Int?,

        @ColumnInfo(name = "type")
        override val type: PageType = PageType.FEATURED
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Featured(
                offset = offset
            )
        }
    }

    data class Notification(
        @ColumnInfo(name = "following")
        var following: Boolean? = null,

        @ColumnInfo(name = "markAsRead")
        var markAsRead: Boolean? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "userId")
        val userId: String,

        @ColumnInfo(name = "includeReplies")
        var includeReplies: Boolean = true,

        @ColumnInfo(name = "includeMyRenotes")
        var includeMyRenotes: Boolean? = true,

        @ColumnInfo(name = "withFiles")
        var withFiles: Boolean? = null,

        @ColumnInfo(name = "type")
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
        @ColumnInfo(name = "query")
        var query: String,

        @ColumnInfo(name = "host")
        var host: String? = null,

        @ColumnInfo(name = "userId")
        var userId: String? = null,

        @ColumnInfo(name = "type")
        override val type: PageType = PageType.SEARCH
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Search(
                query = query, host = host, userId = userId
            )
        }
    }

    data class Antenna(
        @ColumnInfo(name = "antennaId")
        val antennaId: String,

        @ColumnInfo(name = "type")
        override val type: PageType = PageType.ANTENNA
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Antenna(
                antennaId = antennaId
            )
        }
    }

    class Favorite(
        @ColumnInfo(name = "type")
        override val type: PageType = PageType.FAVORITE
    ) : Timeline() {
        override fun toPageable(): Pageable {
            return Pageable.Favorite
        }
    }
}