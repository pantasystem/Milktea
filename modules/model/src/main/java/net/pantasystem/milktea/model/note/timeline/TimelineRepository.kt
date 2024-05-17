package net.pantasystem.milktea.model.note.timeline

import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note


/**
 * 投稿に関するタイムラインを取得するためのリポジトリ
 * このリポジトリはキャッシュを持つことができる
 * キャッシュを持つことができるかどうかは[TimelineType.canCache]で判断する
 * キャッシュを持つことができる場合は[findLaterTimeline]で取得したデータをキャッシュに保存する
 * ページネーションの状態を無視して呼び出してしまうと、キャッシュの順序が狂ってしまうので注意
 */
interface TimelineRepository {
    suspend fun findPreviousTimeline(
        type: TimelineType,
        untilId: String? = null,
        untilDate: Long? = null,
        limit: Int = 10,
    ): Result<TimelineResponse>

    /**
     * @Param sinceDate はサーバーによっては使えない場合がある
     */
    suspend fun findLaterTimeline(
        type: TimelineType,
        sinceId: String? = null,
        sinceDate: Long? = null,
        limit: Int = 10,
    ): Result<TimelineResponse>

}

data class TimelineResponse(
    val timelineItems: List<Note.Id>,
    val sinceId: String?,
    val untilId: String?,
)

data class TimelineType(
    val accountId: Long,
    val pageable: Pageable,
    val pageId: Long?,
) {

    fun isAllowPageable(): Boolean {
        return when (pageable) {
            is Pageable.Antenna -> true
            Pageable.CalckeyRecommendedTimeline -> true
            is Pageable.ChannelTimeline -> true
            is Pageable.ClipNotes -> true
            Pageable.Favorite -> true
            is Pageable.Featured -> true
            is Pageable.Gallery -> false
            is Pageable.GlobalTimeline -> true
            is Pageable.HomeTimeline -> true
            is Pageable.HybridTimeline -> true
            is Pageable.LocalTimeline -> true
            Pageable.Mastodon.BookmarkTimeline -> true
            is Pageable.Mastodon.HashTagTimeline -> true
            is Pageable.Mastodon.HomeTimeline -> true
            is Pageable.Mastodon.ListTimeline -> true
            is Pageable.Mastodon.LocalTimeline -> true
            Pageable.Mastodon.Mention -> false
            is Pageable.Mastodon.PublicTimeline -> true
            is Pageable.Mastodon.SearchTimeline -> true
            Pageable.Mastodon.TrendTimeline -> true
            is Pageable.Mastodon.UserTimeline -> true
            is Pageable.Mention -> true
            is Pageable.Notification -> false
            is Pageable.Search -> true
            is Pageable.SearchByTag -> true
            is Pageable.Show -> false
            is Pageable.UserListTimeline -> true
            is Pageable.UserTimeline -> true
        }
    }

    fun canCache(): Boolean {
        if (!isAllowPageable()) {
            return false
        }
        if (pageId == null) {
            return false
        }
        return when (pageable) {
            is Pageable.Antenna -> false
            Pageable.CalckeyRecommendedTimeline -> true
            is Pageable.ChannelTimeline -> true
            is Pageable.ClipNotes -> false
            Pageable.Favorite -> false
            is Pageable.Featured -> false
            is Pageable.Gallery -> false
            is Pageable.GlobalTimeline -> true
            is Pageable.HomeTimeline -> true
            is Pageable.HybridTimeline -> true
            is Pageable.LocalTimeline -> true
            Pageable.Mastodon.BookmarkTimeline -> false
            is Pageable.Mastodon.HashTagTimeline -> false
            is Pageable.Mastodon.HomeTimeline -> true
            is Pageable.Mastodon.ListTimeline -> true
            is Pageable.Mastodon.LocalTimeline -> true
            Pageable.Mastodon.Mention -> false
            is Pageable.Mastodon.PublicTimeline -> true
            is Pageable.Mastodon.SearchTimeline -> false
            Pageable.Mastodon.TrendTimeline -> false
            is Pageable.Mastodon.UserTimeline -> false
            is Pageable.Mention -> false
            is Pageable.Notification -> false
            is Pageable.Search -> false
            is Pageable.SearchByTag -> false
            is Pageable.Show -> false
            is Pageable.UserListTimeline -> false
            is Pageable.UserTimeline -> false
        }
    }
}