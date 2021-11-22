package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File
import kotlinx.datetime.Instant
import java.util.*

data class NoteEditingState(
    val author: Account? = null,
    val visibility: Visibility = Visibility.Public(false),
    val text: String? = null,
    val cw: String? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val files: List<File> = emptyList(),
    val poll: PollEditingState? = null,
    val viaMobile: Boolean = true,
    val draftNoteId: Long? = null,
) {

    val hasCw: Boolean
        get() = cw != null

    val totalFilesCount: Int
        get() = this.files.size

    val isSpecified: Boolean
        get() = this.visibility is Visibility.Specified

    val isLocalOnly: Boolean
        get() = this.visibility.isLocalOnly()


    fun checkValidate(textMaxLength: Int = 3000) : Boolean {
        return !(this.files.isEmpty()
                && this.files.size > 4
                && this.renoteId == null
                && this.text.isNullOrBlank()
                && (this.text?.codePointCount(0, this.text.length) ?: 0) > textMaxLength
                && this.poll?.checkValidate() == true)
    }

    fun changeText(text: String) : NoteEditingState {
        return this.copy(
            text = text
        )
    }

    fun addFile(file: File) : NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                add(file)
            }
        )
    }

    fun removeFile(file: File) : NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                remove(file)
            }
        )
    }

    fun toggleCw() : NoteEditingState {
        return this.copy(
            cw = if(this.hasCw) null else ""
        )
    }

}

data class PollEditingState(
    val choices: List<PollChoiceState>,
    val multiple: Boolean,
    val expiresAt: Instant?
) {
    fun checkValidate() : Boolean {
        return choices.all {
            it.text.isNotBlank()
        }
    }
}

data class PollChoiceState(
    val text: String,
    val id: String = UUID.randomUUID().toString()
)