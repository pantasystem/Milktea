package jp.panta.misskeyandroidclient.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Page(
    var accountId: String?,
    var title: String,
    var pageNumber: Int?,
    @Embedded(prefix = "global_timeline") val globalTimeline: GlobalTimeline? = null,
    @Embedded(prefix = "local_timeline") val localTimeline: LocalTimeline? = null,
    @Embedded(prefix = "hybrid_timeline") val hybridTimeline: HybridTimeline? = null,
    @Embedded(prefix = "home_timeline") val homeTimeline: HomeTimeline? = null,
    @Embedded(prefix = "user_list_timeline") val userListTimeline: UserListTimeline? = null,
    @Embedded(prefix = "mention") val mention: Mention? = null,
    @Embedded(prefix = "show") val show: Show? = null,
    @Embedded(prefix = "tag") val searchByTag: SearchByTag? = null,
    @Embedded(prefix = "featured") val featured: Featured? = null,
    @Embedded(prefix = "notification") val notification: Notification? = null,
    @Embedded(prefix = "user") val userTimeline: UserTimeline? = null,
    @Embedded(prefix = "search") val search: Search? = null,
    @Embedded(prefix = "favorite") val favorite: Favorite? = null,
    @Embedded(prefix = "antenna") val antenna: Antenna? = null

){
    @PrimaryKey(autoGenerate = true) var id: Long? = null

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

    data class GlobalTimeline(var withFiles: Boolean? = null): Pageable, Timeline()
    data class LocalTimeline(var withFiles: Boolean? = null, var excludeNsfw: Boolean? = null) : Timeline()

    /**
     * @param includeLocalRenotes 全体設定に従う場合はnullそうでない場合はパラメーターが指定される
     * @param includeMyRenotes 上記と同じく
     * @param includeRenotedMyRenotes 上記と同じく
     */
    data class HybridTimeline(
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null
    ) : Timeline()

    data class HomeTimeline(
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null
    ) : Timeline()

    data class UserListTimeline(
        val listId: String,
        var withFiles: Boolean? = null,
        var includeLocalRenotes: Boolean? = null,
        var includeMyRenotes: Boolean? = null,
        var includeRenotedMyRenotes: Boolean? = null
    ) : Timeline()

    data class Mention(val following: Boolean?, val  visibility: String? = null) : Timeline()
    data class Show(val noteId: String) : Pageable
    data class SearchByTag(val tag: String, var reply: Boolean? = null, var renote: Boolean? = null, var withFiles: Boolean? = null, var poll: Boolean? = null) : Timeline()
    data class Featured(val offset: Int?) : Timeline()
    data class Notification(var following: Boolean? = null, var markAsRead: Boolean? = null) : Pageable
    data class UserTimeline(val userId: String, var includeReplies: Boolean = true, var includeMyRenotes: Boolean? = true, var withFiles: Boolean? = null) : Timeline()
    data class Search(var query: String, var host: String? = null, var userId: String? = null) : Timeline()
    data class Antenna(val antennaId: String) : Timeline()

    class Favorite : Timeline()
}

