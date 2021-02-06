package jp.panta.misskeyandroidclient.api.notes

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

data class NoteDTO(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date,
    val createdAt: Date,
    val text: String?,
    val cw: String?,
    val userId: String,

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

fun NoteDTO.toNote(account: Account): Note{
    return Note(
        id = Note.Id(account.accountId, this.id),
        createdAt = this.createdAt,
        text = this.text,
        cw = this.cw,
        userId = User.Id(account.accountId, this.userId),
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
        visibleUserIds = this.visibleUserIds?.map{
            User.Id(account.accountId, it)
        }?: emptyList(),
        myReaction = this.myReaction
    )
}

fun NoteDTO.toNoteAndUser(account: Account): Pair<Note, User> {
    val note = this.toNote(account)
    val user = this.user.toUser(false)
    return note to user
}

fun NoteDTO.toEntities(account: Account): Triple<Note, List<Note>, List<User>>{
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val note = this.toNote(account)
    notes.add(note)
    users.add(this.user.toUser(false))
    if(this.reply != null){
        val nAndU = this.reply.toNoteAndUser(account)
        notes.add(nAndU.first)
        users.add(nAndU.second)
    }

    if(this.reNote != null){
        val nAndU = this.reNote.toNoteAndUser(account)
        notes.add(nAndU.first)
        users.add(nAndU.second)
    }

    return Triple(note, notes, users)
}