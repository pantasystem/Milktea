package net.pantasystem.milktea.model.notes


import net.pantasystem.milktea.model.ITask
import net.pantasystem.milktea.data.model.notes.poll.CreatePoll


/**
 * @param noExtractEmojis 本文からカスタム絵文字を展開しないか否か
 * @param noExtractMentions 本文からメンションを展開しないか否か
 * @param noExtractHashtags 本文からハッシュタグを展開しないか否か
 */
data class CreateNote(
    val author: net.pantasystem.milktea.model.account.Account,
    val visibility: Visibility,
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var files: List<net.pantasystem.milktea.model.file.AppFile>? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val poll: CreatePoll? = null,
    val draftNoteId: Long? = null,
    val channelId: net.pantasystem.milktea.model.channel.Channel.Id? = null,
)

class CreateNoteTask(
    private val noteRepository: NoteRepository,
    val createNote: CreateNote
) : ITask<Note> {
    override suspend fun execute(): Note {
        return noteRepository.create(createNote)
    }
}

fun CreateNote.task(noteRepository: NoteRepository) : CreateNoteTask {
    return CreateNoteTask(noteRepository, this)
}

fun NoteEditingState.toCreateNote(account: net.pantasystem.milktea.model.account.Account): CreateNote {
    return CreateNote(
        author = account,
        visibility = visibility,
        text = text,
        cw = cw,
        viaMobile = false,
        files = files,
        replyId = replyId,
        renoteId = renoteId,
        poll = poll?.toCreatePoll(),
        draftNoteId = draftNoteId,
        channelId = channelId
    )
}