package jp.panta.misskeyandroidclient.api.misskey.notes

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import kotlinx.serialization.Serializable
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
    val markAsRead: Boolean? = null
): JavaSerializable{


    class Builder(
        val pageable: Pageable,
        var i: String?,
        var includes: Include? = null,
        var limit: Int = 20
    ){

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
                includeRenotedMyNotes = includes?.includeRenotedMyNotes?: params.includeRenotedMyRenotes,
                includeMyRenotes = includes?.includeMyRenotes?: params.includeMyRenotes,
                includeReplies = params.includeReplies,
                includeLocalRenotes = includes?.includeLocalRenotes?: params.includeLocalRenotes,
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
                noteId = params.noteId
            )
        }

    }
    /*class Builder(
        val pageableTimeline: Page.Timeline,
        var include: Include? = null
    ){


        fun build(i: String, conditions: Conditions?): NoteRequest{
            return when(pageableTimeline){
                is Page.HomeTimeline -> {
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeLocalRenotes = pageableTimeline.includeLocalRenotes?: include?.includeLocalRenotes,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        includeRenotedMyNotes = pageableTimeline.includeRenotedMyRenotes?: include?.includeRenotedMyNotes,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.HybridTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeLocalRenotes = pageableTimeline.includeLocalRenotes?: include?.includeLocalRenotes,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        includeRenotedMyNotes = pageableTimeline.includeRenotedMyRenotes?: include?.includeRenotedMyNotes,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.GlobalTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.LocalTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.UserListTimeline ->{
                    NoteRequest( i = i,
                        listId = pageableTimeline.listId,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeLocalRenotes = pageableTimeline.includeLocalRenotes?: include?.includeLocalRenotes,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        includeRenotedMyNotes = pageableTimeline.includeRenotedMyRenotes?: include?.includeRenotedMyNotes,
                        withFiles = pageableTimeline.withFiles
                    )
                }

                is Page.Mention ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        following = pageableTimeline.following,
                        visibility = pageableTimeline.visibility
                    )
                }
                is Page.SearchByTag ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        withFiles = pageableTimeline.withFiles,
                        tag = pageableTimeline.tag

                    )
                }
                is Page.UserTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        withFiles = pageableTimeline.withFiles,
                        includeReplies = pageableTimeline.includeReplies,
                        userId = pageableTimeline.userId
                    )
                }
                is Page.Search ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        userId = pageableTimeline.userId,
                        query = pageableTimeline.query,
                        host = pageableTimeline.host
                    )
                }
                is Page.Favorite ->{
                    NoteRequest(
                        i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate
                    )
                }
                is Page.Featured ->{
                    NoteRequest(
                        i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate
                    )
                }

                is Page.Antenna ->{
                    NoteRequest(
                        i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        antennaId = pageableTimeline.antennaId
                    )
                }
                else -> throw IllegalArgumentException("type: ${pageableTimeline.javaClass}")

            }
        }
    }*/



    data class Conditions(
        @SerializedName("sinceId") val sinceId: String? = null,
        @SerializedName("untilId") val untilId: String? = null,
        @SerializedName("sinceDate") val sinceDate: Long? = null,
        @SerializedName("untilDate") val untilDate: Long? = null
    )

    data class Include(
        val includeLocalRenotes: Boolean? = null,
        val includeMyRenotes: Boolean? = null,
        val includeRenotedMyNotes: Boolean? = null
    )



}