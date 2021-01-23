package jp.panta.misskeyandroidclient.api.notes

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

data class NoteDTO(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date,
    val createdAt: Date,
    val text: String?,
    val cw: String?,
    val userId: String?,

    val replyId: String?,

    @SerializedName("renoteId")
    val reNoteId: String?,

    val viaMobile: Boolean?,
    val visibility: String?,
    val localOnly: Boolean?,

    @SerializedName("visibleUserIds")
    val visibleUserIds: List<String>?,

    val url: String?,
    val uri: String?,
    @SerializedName("renoteCount") val reNoteCount: Int,
    @SerializedName("reactions") val reactionCounts: LinkedHashMap<String, Int>?,
    @SerializedName("emojis") val emojis: List<Emoji>?,
    @SerializedName("repliesCount") val replyCount: Int,
    @SerializedName("user") val user: UserDTO,
    @SerializedName("files") val files: List<FileProperty>?,
    //@JsonProperty("fileIds") val mediaIds: List<String?>?,    //v10, v11の互換性が取れない
    val poll: Poll?,
    @SerializedName("renote") val reNote: NoteDTO?,
    val reply: NoteDTO?,
    @SerializedName("myReaction") val myReaction: String?,

    @SerializedName("_featuredId_") val tmpFeaturedId: String?,

    val app: App
): Serializable

fun NoteDTO.toNote(): Note{
    return Note(
        id = this.id,
        createdAt = this.createdAt,
        text = this.text,
        cw = this.cw,
        userId = this.userId,
        replyId = this.replyId,
        renoteId = this.reNoteId,
        viaMobile = this.viaMobile,
        visibility = this.visibility,
        localOnly = this.localOnly,
        emojis = this.emojis,
        app = this.app,
        files = this.files,
        poll = this.poll,
        reactionCounts = this.reactionCounts?.map{
            ReactionCount(reaction = it.key, it.value)
        }?: emptyList(),
        renoteCount = this.reNoteCount,
        repliesCount = this.replyCount,
        uri = this.uri,
        url = this.url,
        visibleUserIds = this.visibleUserIds,
        myReaction = this.myReaction
    )
}