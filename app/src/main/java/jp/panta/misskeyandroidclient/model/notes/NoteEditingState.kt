package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant
import java.util.*

data class NoteEditingState(
    val author: Account? = null,
    val visibility: Visibility = Visibility.Public(false),
    val text: String? = null,
    val cw: String? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val files: List<AppFile> = emptyList(),
    val poll: PollEditingState? = null,
    val viaMobile: Boolean = true,
    val draftNoteId: Long? = null,
    val reservationPostingAt: Instant? = null,
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
        if(this.files.size > 4) {
            return false
        }

        if((this.text?.codePointCount(0, this.text.length) ?: 0) > textMaxLength) {
            return false
        }

        if(this.renoteId != null) {
            return true
        }

        if(this.poll != null && this.poll.checkValidate()) {
            return true
        }

        return !(
                this.text.isNullOrBlank()
                        && this.files.isNullOrEmpty()
                )
    }

    fun changeText(text: String) : NoteEditingState {
        return this.copy(
            text = text
        )
    }

    fun changeCw(text: String?) : NoteEditingState {
        return this.copy(
            cw = text
        )
    }

    fun addFile(file: AppFile) : NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                add(file)
            }
        )
    }

    fun removeFile(file: AppFile) : NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                remove(file)
            }
        )
    }

    fun changePollExpiresAt(expiresAt: PollExpiresAt) : NoteEditingState{
        return this.copy(
            poll = this.poll?.copy(expiresAt = expiresAt)
        )
    }

    fun setAccount(account: Account?) : NoteEditingState{
        if(author == null) {
            return this.copy(
                author = account
            )
        }
        if(account == null) {
            throw IllegalArgumentException("現在の状態に未指定のAccountを指定することはできません")
        }
        if(files.any { it is AppFile.Remote }) {
            throw IllegalArgumentException("リモートファイル指定時にアカウントを変更することはできません(files)。")
        }
        if(!(replyId == null || author.instanceDomain == account.instanceDomain)) {
            throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(replyId)。")
        }

        if(!(renoteId == null || author.instanceDomain == account.instanceDomain)) {
            throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(renoteId)。")
        }

        if(visibility is Visibility.Specified
            && (visibility.visibleUserIds.isNotEmpty()
                    || author.instanceDomain == account.instanceDomain)
        ) {
            throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(visibility)。")
        }

        return this.copy(
            author = account,
            files = files,
            replyId = replyId?.copy(accountId = account.accountId),
            renoteId = renoteId?.copy(accountId = account.accountId),
            visibility = if(visibility is Visibility.Specified) {
                visibility.copy(visibleUserIds = visibility.visibleUserIds.map {
                    it.copy(accountId = account.accountId)
                })
            }else{
                visibility
            }
        )

    }

    fun removePollChoice(id: UUID) : NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.filterNot { choice ->
                        choice.id == id
                    }
                )
            }
        )
    }

    fun addPollChoice() : NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.toMutableList().also { list ->
                        list.add(
                            PollChoiceState("")
                        )
                    }
                )
            }
        )
    }

    fun updatePollChoice(id: UUID, text: String) : NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.map { choice ->
                        if(choice.id == id) {
                            choice.copy(
                                text = text
                            )
                        }else{
                            choice
                        }
                    }
                )
            }
        )
    }

    fun toggleCw() : NoteEditingState {
        return this.copy(
            cw = if(this.hasCw) null else ""
        )
    }

    fun togglePoll() : NoteEditingState {
        return this.copy(
            poll = if(poll == null) PollEditingState(emptyList(), false) else null
        )
    }

    fun clear() : NoteEditingState {
        return NoteEditingState(author = this.author)
    }

}

sealed interface PollExpiresAt {
    object Infinity : PollExpiresAt
    data class DateAndTime(val expiresAt: Instant) : PollExpiresAt

    fun asDate(): Date? {
        return this.expiresAt()?.toEpochMilliseconds()?.let{
            Date(it)
        }
    }
}

fun PollExpiresAt.expiresAt() : Instant? {
    return when(this) {
        is PollExpiresAt.Infinity -> null
        is PollExpiresAt.DateAndTime -> this.expiresAt
    }
}

data class PollEditingState(
    val choices: List<PollChoiceState>,
    val multiple: Boolean,
    val expiresAt: PollExpiresAt = PollExpiresAt.Infinity
) {

    val isExpiresAtDateTime: Boolean
        get() = expiresAt is PollExpiresAt.DateAndTime

    fun checkValidate() : Boolean {
        return choices.all {
            it.text.isNotBlank()
        } && this.choices.size >= 2
    }

    fun toggleMultiple() : PollEditingState{
        return this.copy(
            multiple = !this.multiple
        )
    }
}

data class PollChoiceState(
    val text: String,
    val id: UUID = UUID.randomUUID()
)

fun DraftNote.toNoteEditingState() : NoteEditingState{
    return NoteEditingState(
        text = this.text,
        cw = this.cw,
        draftNoteId = this.draftNoteId,
        visibility = Visibility(type = this.visibility, isLocalOnly = this.localOnly ?: false, visibleUserIds = this.visibleUserIds?.map {
            User.Id(accountId = accountId, id = it)
        }),
        viaMobile = this.viaMobile ?: true,
        poll = this.draftPoll?.let {
            PollEditingState(
                choices = it.choices.map { choice ->
                    PollChoiceState(choice)
                },
                expiresAt = it.expiresAt?.let { ex ->
                    PollExpiresAt.DateAndTime(
                        Instant.fromEpochMilliseconds(ex)
                    )
                }?: PollExpiresAt.Infinity,
                multiple = it.multiple
            )
        },
        replyId = this.replyId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        renoteId = this.renoteId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        files = this.files?.map {
            if(it.isRemoteFile) {
                AppFile.Remote(
                    it.remoteFileId!!
                )
            }else{
                AppFile.Local(
                    name = it.name,
                    isSensitive = it.isSensitive ?: false,
                    path = it.path ?: "",
                    thumbnailUrl = it.thumbnailUrl,
                    type = it.type ?: "",
                    folderId = null
                )
            }
        } ?: emptyList(),
        reservationPostingAt = reservationPostingAt?.let {
            Instant.fromEpochMilliseconds(it.time)
        }
    )
}

fun PollEditingState.toCreatePoll() : CreatePoll {
    return CreatePoll(
        choices = this.choices.map {
            it.text
        },
        multiple = multiple,
        expiresAt = expiresAt.expiresAt()?.toEpochMilliseconds()
    )
}

fun PollEditingState.toDraftPoll() : DraftPoll {
    return DraftPoll(
        choices = this.choices.map {
            it.text
        },
        multiple = multiple,
        expiresAt = expiresAt.expiresAt()?.toEpochMilliseconds()
    )
}