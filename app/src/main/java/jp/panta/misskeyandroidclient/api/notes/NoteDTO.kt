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
import jp.panta.misskeyandroidclient.model.notes.Visibility
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.SerialName
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

@kotlinx.serialization.Serializable
data class NoteDTO(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date,
    @kotlinx.serialization.Serializable(with = DateSerializer::class) val createdAt: Date,
    val text: String? = null,
    val cw: String? = null,
    val userId: String,

    val replyId: String? = null,

    val renoteId: String? = null,

    val viaMobile: Boolean? = null,
    val visibility: String? = null,
    val localOnly: Boolean? = null,

    @SerializedName("visibleUserIds")
    val visibleUserIds: List<String>? = null,

    val url: String? = null,
    val uri: String? = null,

    val renoteCount: Int,

    @SerializedName("reactions")
    @SerialName("reactions")
    val reactionCounts: LinkedHashMap<String, Int>? = null,

    @SerializedName("emojis") val emojis: List<Emoji>? = null,

    @SerializedName("repliesCount")
    @SerialName("repliesCount")
    val replyCount: Int,
    val user: UserDTO,
    val files: List<FileProperty>? = null,
    //@JsonProperty("fileIds") val mediaIds: List<String?>? = null,    //v10, v11の互換性が取れない
    val poll: Poll? = null,
    @SerializedName("renote")
    @SerialName("renote")
    val reNote: NoteDTO? = null,
    val reply: NoteDTO? = null,

    @SerializedName("myReaction")
    val myReaction: String? = null,

    @SerializedName("_featuredId_")
    @SerialName("_featuredId_")
    val tmpFeaturedId: String? = null,
    @SerialName("_prId_")
    @SerializedName("_prId_")
    val promotionId: String? = null,

    val app: App? = null
): Serializable

fun NoteDTO.toNote(account: Account): Note{
    val visibility = Visibility(this.visibility?: "public", isLocalOnly = localOnly?: false, visibleUserIds = visibleUserIds?.map { id ->
        User.Id(account.accountId, id)
    }?: emptyList())
    return Note(
        id = Note.Id(account.accountId, this.id),
        createdAt = this.createdAt,
        text = this.text,
        cw = this.cw,
        userId = User.Id(account.accountId, this.userId),
        replyId = this.replyId?.let{ Note.Id(account.accountId, this.replyId) },
        renoteId = this.renoteId?.let{ Note.Id(account.accountId, this.renoteId) },
        viaMobile = this.viaMobile,
        visibility = visibility,
        localOnly = this.localOnly,
        emojis = this.emojis,
        app = this.app,
        files = this.files,
        poll = this.poll,
        reactionCounts = this.reactionCounts?.map{
            ReactionCount(reaction = it.key, it.value)
        }?: emptyList(),
        renoteCount = this.renoteCount,
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
    val user = this.user.toUser(account,false)
    return note to user
}

fun NoteDTO.toEntities(account: Account): Triple<Note, List<Note>, List<User>>{
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val note = this.toNote(account)
    notes.add(note)
    users.add(this.user.toUser(account, false))
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