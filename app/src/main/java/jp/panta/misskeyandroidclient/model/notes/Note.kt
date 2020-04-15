package jp.panta.misskeyandroidclient.model.notes

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.model.users.User
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap

data class Note(
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
    @SerializedName("user") val user: User,
    @SerializedName("files") val files: List<FileProperty>?,
    //@JsonProperty("fileIds") val mediaIds: List<String?>?,    //v10, v11の互換性が取れない
    val poll: Poll?,
    @SerializedName("renote") val reNote: Note?,
    val reply: Note?,
    @SerializedName("myReaction") val myReaction: String?
): Serializable