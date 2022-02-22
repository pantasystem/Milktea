package jp.panta.misskeyandroidclient.api.notes

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.Visibility
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import java.io.Serializable
import kotlin.collections.LinkedHashMap

@kotlinx.serialization.Serializable
data class NoteDTO(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
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
    val files: List<FilePropertyDTO>? = null,
    //@JsonProperty("fileIds") val mediaIds: List<String?>? = null,    //v10, v11の互換性が取れない
    val fileIds: List<String>? = null,
    val poll: PollDTO? = null,
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
        fileIds = this.fileIds?.map { FileProperty.Id(account.accountId, it) },
        poll = this.poll?.toPoll(),
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

data class NoteRelationEntities(
    val note: Note,
    val notes: List<Note>,
    val users: List<User>,
    val files: List<FileProperty>
)

fun NoteDTO.toEntities(account: Account): NoteRelationEntities{
    val dtoList = mutableListOf<NoteDTO>()
    dtoList.add(this)


    if(this.reply != null){

        dtoList.add(this.reply)
    }
    if(this.reNote != null){
        dtoList.add(reNote)
    }

    val note = this.toNote(account)
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()

    pickEntities(account, notes, users, files)
    return NoteRelationEntities(
        note = note,
        notes = notes,
        users = users,
        files = files
    )
}

private fun NoteDTO.pickEntities(account: Account, notes: MutableList<Note>, users: MutableList<User>, files: MutableList<FileProperty>) {
    val (note, user) = this.toNoteAndUser(account)
    notes.add(note)
    users.add(user)
    files.addAll(
        this.files?.map {
            it.toFileProperty(account)
        }?: emptyList()
    )
    if(this.reply != null) {
        this.reply.pickEntities(account, notes, users, files)
    }

    if(this.reNote != null) {
        this.reNote.pickEntities(account, notes, users, files)
    }
}