package net.pantasystem.milktea.api.misskey.notes


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.account.page.Pageable
import java.io.Serializable as JavaSerializable

@Serializable
data class NoteRequest(
    val i: String? = null,
    val userId: String? = null,
    val withFiles: Boolean? = null,
    val fileType: String? = null,
    val excludeNsfw: Boolean? = null,
    val limit: Int? = 20,
    val sinceId: String? = null,
    val untilId: String? = null,
    val sinceDate: Long? = null,
    val untilDate: Long? = null,
    val query: String? = null,
    val tag: String? = null,
    val includeLocalRenotes: Boolean? = null,
    val includeMyRenotes: Boolean? = null,
    val includeRenotedMyNotes: Boolean? = null,
    val noteId: String? = null,
    val antennaId: String? = null,
    val listId: String? = null,
    val following: Boolean? = null,
    val visibility: String? = null,
    val reply: Boolean? = null,
    val renote: Boolean? = null,
    val poll: Boolean? = null,
    val offset: Int? = null,
    val includeReplies: Boolean? = null,
    val host: String? = null,
    val markAsRead: Boolean? = null,
    val channelId: String? = null,
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