package net.pantasystem.milktea.data.infrastructure.account.page.db

import androidx.room.ColumnInfo
import net.pantasystem.milktea.model.account.page.PageParams
import net.pantasystem.milktea.model.account.page.PageType

data class PageRecordParams(
    @ColumnInfo(name = "type")
    val type: PageType = PageType.HOME,

    @ColumnInfo(name = "withFiles")
    val withFiles: Boolean? = null,

    @ColumnInfo(name = "excludeNsfw")
    var excludeNsfw: Boolean? = null,

    @ColumnInfo(name = "includeLocalRenotes")
    var includeLocalRenotes: Boolean? = null,

    @ColumnInfo(name = "includeMyRenotes")
    var includeMyRenotes: Boolean? = null,

    @ColumnInfo(name = "includeRenotedMyRenotes")
    var includeRenotedMyRenotes: Boolean? = null,

    @ColumnInfo(name = "listId")
    val listId: String? = null,

    @ColumnInfo(name = "following")
    val following: Boolean? = null,

    @ColumnInfo(name = "visibility")
    val visibility: String? = null,

    @ColumnInfo(name = "noteId")
    val noteId: String? = null,

    @ColumnInfo(name = "tag")
    val tag: String? = null,

    @ColumnInfo(name = "reply")
    var reply: Boolean? = null,

    @ColumnInfo(name = "renote")
    var renote: Boolean? = null,

    @ColumnInfo(name = "poll")
    var poll: Boolean? = null,

    @ColumnInfo(name = "offset")
    val offset: Int? = null,

    @ColumnInfo(name = "markAsRead")
    var markAsRead: Boolean? = null,

    @ColumnInfo(name = "userId")
    val userId: String? = null,

    @ColumnInfo(name = "includeReplies")
    var includeReplies: Boolean? = null,

    @ColumnInfo(name = "query")
    var query: String? = null,

    @ColumnInfo(name = "host")
    var host: String? = null,

    @ColumnInfo(name = "antennaId")
    val antennaId: String? = null,

    @ColumnInfo(name = "channelId")
    val channelId: String? = null,

    @ColumnInfo(name = "clipId")
    val clipId: String? = null,
) {
    fun toParams(): PageParams {
        return PageParams(
            type = type,
            withFiles = withFiles,
            excludeNsfw = excludeNsfw,
            includeLocalRenotes = includeLocalRenotes,
            includeMyRenotes = includeMyRenotes,
            includeRenotedMyRenotes = includeRenotedMyRenotes,
            listId = listId,
            following = following,
            visibility = visibility,
            noteId = noteId,
            tag = tag,
            reply = reply,
            renote = renote,
            poll = poll,
            offset = offset,
            markAsRead = markAsRead,
            userId = userId,
            includeReplies = includeReplies,
            query = query,
            host = host,
            antennaId = antennaId,
            channelId = channelId,
            clipId = clipId
        )
    }

    companion object {
        fun from(model: PageParams): PageRecordParams {
            return with(model) {
                PageRecordParams(
                    type = type,
                    withFiles = withFiles,
                    excludeNsfw = excludeNsfw,
                    includeLocalRenotes = includeLocalRenotes,
                    includeMyRenotes = includeMyRenotes,
                    includeRenotedMyRenotes = includeRenotedMyRenotes,
                    listId = listId,
                    following = following,
                    visibility = visibility,
                    noteId = noteId,
                    tag = tag,
                    reply = reply,
                    renote = renote,
                    poll = poll,
                    offset = offset,
                    markAsRead = markAsRead,
                    userId = userId,
                    includeReplies = includeReplies,
                    query = query,
                    host = host,
                    antennaId = antennaId,
                    channelId = channelId,
                    clipId = clipId
                )
            }
        }
    }
}