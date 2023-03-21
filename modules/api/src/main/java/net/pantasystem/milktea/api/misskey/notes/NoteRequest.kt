package net.pantasystem.milktea.api.misskey.notes


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.account.page.Pageable
import java.io.Serializable as JavaSerializable

@Serializable
data class NoteRequest(
    @SerialName("i")
    val i: String? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("withFiles")
    val withFiles: Boolean? = null,

    @SerialName("fileType")
    val fileType: String? = null,

    @SerialName("excludeNsfw")
    val excludeNsfw: Boolean? = null,

    @SerialName("limit")
    val limit: Int? = 20,

    @SerialName("sinceId")
    val sinceId: String? = null,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("sinceDate")
    val sinceDate: Long? = null,

    @SerialName("untilDate")
    val untilDate: Long? = null,

    @SerialName("query")
    val query: String? = null,

    @SerialName("tag")
    val tag: String? = null,

    @SerialName("includeLocalRenotes")
    val includeLocalRenotes: Boolean? = null,

    @SerialName("includeMyRenotes")
    val includeMyRenotes: Boolean? = null,

    @SerialName("includeRenotedMyNotes")
    val includeRenotedMyNotes: Boolean? = null,

    @SerialName("noteId")
    val noteId: String? = null,

    @SerialName("antennaId")
    val antennaId: String? = null,

    @SerialName("listId")
    val listId: String? = null,

    @SerialName("following")
    val following: Boolean? = null,

    @SerialName("visibility")
    val visibility: String? = null,

    @SerialName("reply")
    val reply: Boolean? = null,

    @SerialName("renote")
    val renote: Boolean? = null,

    @SerialName("poll")
    val poll: Boolean? = null,

    @SerialName("offset")
    val offset: Int? = null,

    @SerialName("includeReplies")
    val includeReplies: Boolean? = null,

    @SerialName("host")
    val host: String? = null,

    @SerialName("markAsRead")
    val markAsRead: Boolean? = null,

    @SerialName("channelId")
    val channelId: String? = null,

    @SerialName("clipId")
    val clipId: String? = null,
) : JavaSerializable {


    class Builder(
        val pageable: Pageable,
        var i: String?,
        var includes: Include? = null,
        var limit: Int = 20
    ) {

        fun build(conditions: Conditions?): NoteRequest {
            val params = pageable.toParams()
            return NoteRequest(
                i = i,
                userId = params.userId,
                withFiles = params.withFiles,
                excludeNsfw = params.excludeNsfw,
                limit = limit,
                sinceId = conditions?.sinceId,
                untilId = conditions?.untilId,
                untilDate = conditions?.untilDate,
                sinceDate = conditions?.sinceDate,
                query = params.query,
                tag = params.tag,
                includeRenotedMyNotes = includes?.includeRenotedMyNotes
                    ?: params.includeRenotedMyRenotes,
                includeMyRenotes = includes?.includeMyRenotes ?: params.includeMyRenotes,
                includeReplies = params.includeReplies,
                includeLocalRenotes = includes?.includeLocalRenotes ?: params.includeLocalRenotes,
                following = params.following,
                poll = params.poll,
                offset = params.offset,
                visibility = params.visibility,
                host = params.host,
                antennaId = params.antennaId,
                markAsRead = params.markAsRead,
                renote = params.renote,
                reply = params.reply,
                listId = params.listId,
                noteId = params.noteId,
                channelId = params.channelId,
                clipId = params.clipId
            )
        }

    }


    data class Conditions(
        @SerialName("sinceId") val sinceId: String? = null,
        @SerialName("untilId") val untilId: String? = null,
        @SerialName("sinceDate") val sinceDate: Long? = null,
        @SerialName("untilDate") val untilDate: Long? = null
    )

    data class Include(
        val includeLocalRenotes: Boolean? = null,
        val includeMyRenotes: Boolean? = null,
        val includeRenotedMyNotes: Boolean? = null
    )


}