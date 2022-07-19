package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.notes.poll.CreatePoll

@Serializable
data class CreateNote(
    val i: String,
    val visibility: String = "public",
    val visibleUserIds: List<String>? = null,
    val text: String? = null,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var fileIds: List<String>? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val poll: CreatePoll? = null,
    val channelId: String? = null,


    ){

    @Serializable
    data class Response(val createdNote: NoteDTO)


}