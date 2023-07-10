package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.notes.poll.CreatePoll

@Serializable
data class CreateNote(
    @SerialName("i")
    val i: String,

    @SerialName("visibility")
    val visibility: String = "public",

    @SerialName("visibleUserIds")
    val visibleUserIds: List<String>? = null,

    @SerialName("text")
    val text: String? = null,

    @SerialName("cw")
    val cw: String? = null,

    @SerialName("viaMobile")
    val viaMobile: Boolean? = null,

    @SerialName("localOnly")
    val localOnly: Boolean? = null,

    @SerialName("noExtractMentions")
    val noExtractMentions: Boolean? = null,

    @SerialName("noExtractHashtags")
    val noExtractHashtags: Boolean? = null,

    @SerialName("noExtractEmojis")
    val noExtractEmojis: Boolean? = null,

    @SerialName("fileIds")
    var fileIds: List<String>? = null,

    @SerialName("replyId")
    val replyId: String? = null,

    @SerialName("renoteId")
    val renoteId: String? = null,

    @SerialName("poll")
    val poll: CreatePoll? = null,

    @SerialName("channelId")
    val channelId: String? = null,

    @SerialName("reactionAcceptance")
    val reactionAcceptance: ReactionAcceptanceType? = null,
) {

    @Serializable
    data class Response(val createdNote: NoteDTO)


}