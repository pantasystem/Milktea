package jp.panta.misskeyandroidclient.model

import androidx.room.*
import androidx.room.ForeignKey.NO_ACTION
import jp.panta.misskeyandroidclient.model.core.Account

@Entity(tableName = "page", foreignKeys = [ForeignKey(childColumns = ["accountId"], parentColumns = ["id"], entity = Account::class, onDelete = NO_ACTION, onUpdate = NO_ACTION)])
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

){
    @PrimaryKey(autoGenerate = true) var id: Long? = null

    @Ignore
    fun pageable(): Pageable?{
        return when{
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

    abstract class Timeline : Pageable

    data class GlobalTimeline(
        @ColumnInfo(name = "with_files") var withFiles: Boolean? = null,
        val type: PageType = PageType.GLOBAL
    ): Pageable, Timeline(){

    }
    data class LocalTimeline(
        @ColumnInfo(name = "with_files") var withFiles: Boolean? = null,
        @ColumnInfo(name = "exclude_nsfw") var excludeNsfw: Boolean? = null,
        val type: PageType = PageType.LOCAL
    ) : Timeline()

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
        val type: PageType = PageType.SOCIAL
    ) : Timeline()

    data class HomeTimeline(
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        val type: PageType = PageType.HOME
    ) : Timeline()

    data class UserListTimeline(
        val listId: String,
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null,
        val type: PageType = PageType.USER_LIST
    ) : Timeline()

    data class Mention(
        val following: Boolean?, val
        visibility: String? = null,
        val type: PageType = PageType.MENTION
    ) : Timeline()
    data class Show(
        val noteId: String,
        val type: PageType = PageType.DETAIL
    ) : Pageable
    data class SearchByTag(
        val tag: String, var reply: Boolean? = null, var renote: Boolean? = null, var withFiles: Boolean? = null, var poll: Boolean? = null,
        val type: PageType = PageType.SEARCH_HASH
    ) : Timeline()
    data class Featured(
        val offset: Int?,
        val type: PageType = PageType.FEATURED
    ) : Timeline()
    data class Notification(var following: Boolean? = null, var markAsRead: Boolean? = null, val type: PageType = PageType.NOTIFICATION) : Pageable
    data class UserTimeline(
        val userId: String, var includeReplies: Boolean = true, var includeMyRenotes: Boolean? = true, var withFiles: Boolean? = null,
        val type: PageType = PageType.USER
    ) : Timeline()
    data class Search(
        var query: String, var host: String? = null, var userId: String? = null,
        val type: PageType = PageType.SEARCH
    ) : Timeline()
    data class Antenna(
        val antennaId: String,
        val type: PageType = PageType.ANTENNA
    ) : Timeline()

    class Favorite(val type: PageType = PageType.FAVORITE) : Timeline()
}

