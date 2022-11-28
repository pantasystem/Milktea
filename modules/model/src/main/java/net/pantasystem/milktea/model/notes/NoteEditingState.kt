package net.pantasystem.milktea.model.notes


import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.from
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.poll.CreatePoll
import net.pantasystem.milktea.model.user.User
import java.util.*

data class AddMentionResult(
    val cursorPos: Int,
    val state: NoteEditingState
)

/**
 * @param textCursorPos 次のカーソルの位置を明示的に示したい時に使用します。平常時はNullを指定します。
 */
data class NoteEditingState(
    val author: Account? = null,
    val visibility: Visibility = Visibility.Public(false),
    val text: String? = null,
    val textCursorPos: Int? = null,
    val cw: String? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val files: List<AppFile> = emptyList(),
    val poll: PollEditingState? = null,
    val viaMobile: Boolean = true,
    val draftNoteId: Long? = null,
    val reservationPostingAt: Instant? = null,
    val channelId: Channel.Id? = null,
) {

    val hasCw: Boolean
        get() = cw != null

    val totalFilesCount: Int
        get() = this.files.size

    val isSpecified: Boolean
        get() = this.visibility is Visibility.Specified

    val isLocalOnly: Boolean
        get() = this.visibility.isLocalOnly()


    fun setDraftNote(draftNote: DraftNote?): NoteEditingState {
        return draftNote?.toNoteEditingState() ?: this
    }

    fun changeRenoteId(renoteId: Note.Id?): NoteEditingState {
        return copy(
            renoteId = renoteId
        )
    }

    fun changeReplyTo(replyId: Note.Id?): NoteEditingState {
        return copy(
            replyId = replyId
        )
    }

    fun checkValidate(textMaxLength: Int = 3000, maxFileCount: Int = 4): Boolean {
        if (this.files.size > maxFileCount) {
            return false
        }

        if ((this.text?.codePointCount(0, this.text.length) ?: 0) > textMaxLength) {
            return false
        }

        if (channelId != null && visibility != Visibility.Public(true)) {
            return false
        }

        if (this.renoteId != null) {
            return true
        }

        if (this.poll != null && this.poll.checkValidate()) {
            return true
        }

        return !(
                this.text.isNullOrBlank()
                        && this.files.isEmpty()
                )
    }

    fun changeText(text: String): NoteEditingState {
        return this.copy(
            text = text,
            textCursorPos = null,
        )
    }

    fun addMentionUserNames(userNames: List<String>, pos: Int): AddMentionResult {
        val mentionBuilder = StringBuilder()
        userNames.forEachIndexed { index, userName ->
            if (index < userNames.size - 1) {
                // NOTE: 次の文字がつながらないようにする
                mentionBuilder.appendLine("$userName ")
            } else {
                // NOTE: 次の文字がつながらないようにする
                mentionBuilder.append("$userName ")
            }
        }
        val builder = StringBuilder(text ?: "")
        builder.insert(pos, mentionBuilder.toString())
        val nextPos = pos + mentionBuilder.length
        return AddMentionResult(nextPos, copy(text = builder.toString(), textCursorPos = nextPos))
    }

    fun changeCw(text: String?): NoteEditingState {
        return this.copy(
            cw = text
        )
    }

    fun addFile(file: AppFile): NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                add(file)
            }
        )
    }

    fun removeFile(file: AppFile): NoteEditingState {
        return this.copy(
            files = this.files.toMutableList().apply {
                remove(file)
            }
        )
    }

    fun changePollExpiresAt(expiresAt: PollExpiresAt): NoteEditingState {
        return this.copy(
            poll = this.poll?.copy(expiresAt = expiresAt)
        )
    }

    fun setAccount(account: Account?): NoteEditingState {
        if (author == null) {
            return this.copy(
                author = account
            )
        }
        if (account == null) {
            throw IllegalArgumentException("現在の状態に未指定のAccountを指定することはできません")
        }

        if (replyId != null) {
            if (replyId.accountId != account.accountId && author.instanceDomain != account.instanceDomain) {
                throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(replyId)。")
            }
        }

        if (renoteId != null) {
            if (renoteId.accountId != account.accountId && author.instanceDomain != account.instanceDomain) {
                throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(renoteId)。")
            }
        }


        if (visibility is Visibility.Specified
            && (visibility.visibleUserIds.isNotEmpty()
                    || author.instanceDomain == account.instanceDomain)
        ) {
            if (!visibility.visibleUserIds.all { it.accountId == account.accountId }) {
                throw IllegalArgumentException("異なるインスタンスドメインのアカウントを切り替えることはできません(visibility)。")
            }
        }

        return this.copy(
            author = account,
            files = files,
            replyId = replyId?.copy(accountId = account.accountId),
            renoteId = renoteId?.copy(accountId = account.accountId),
            visibility = if (visibility is Visibility.Specified) {
                visibility.copy(visibleUserIds = visibility.visibleUserIds.map {
                    it.copy(accountId = account.accountId)
                })
            } else {
                visibility
            }
        )

    }

    fun removePollChoice(id: UUID): NoteEditingState {
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

    fun addPollChoice(): NoteEditingState {
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

    fun updatePollChoice(id: UUID, text: String): NoteEditingState {
        return this.copy(
            poll = this.poll?.let {
                it.copy(
                    choices = it.choices.map { choice ->
                        if (choice.id == id) {
                            choice.copy(
                                text = text
                            )
                        } else {
                            choice
                        }
                    }
                )
            }
        )
    }

    fun toggleCw(): NoteEditingState {
        return this.copy(
            cw = if (this.hasCw) null else ""
        )
    }

    fun togglePoll(): NoteEditingState {
        return this.copy(
            poll = if (poll == null) PollEditingState(emptyList(), false) else null
        )
    }

    fun clear(): NoteEditingState {
        return NoteEditingState(author = this.author)
    }

    fun toggleFileSensitiveStatus(appFile: AppFile.Local): NoteEditingState {
        return copy(
            files = files.map {
                if (it === appFile || it is AppFile.Local && it.isAttributeSame(appFile)) {
                    appFile.copy(isSensitive = !appFile.isSensitive)
                } else {
                    it
                }
            }
        )
    }

    fun setChannelId(channelId: Channel.Id?): NoteEditingState {
        return copy(
            visibility = if (channelId == null) visibility else Visibility.Public(true),
            channelId = channelId
        )
    }

    fun setVisibility(visibility: Visibility): NoteEditingState {
        if (channelId == null) {
            return copy(
                visibility = visibility
            )
        }

        if (visibility != Visibility.Public(true)) {
            return copy(
                visibility = Visibility.Public(true)
            )
        }

        return this
    }

    fun shouldDiscardingConfirmation(): Boolean {
        val address = (visibility as? Visibility.Specified)?.visibleUserIds
            ?: emptyList()
        return !text.isNullOrBlank()
                || files.isNotEmpty()
                || !poll?.choices.isNullOrEmpty()
                || address.isNotEmpty()
    }
}

sealed interface PollExpiresAt {
    object Infinity : PollExpiresAt
    data class DateAndTime(val expiresAt: Instant) : PollExpiresAt

    fun asDate(): Date? {
        return this.expiresAt()?.toEpochMilliseconds()?.let {
            Date(it)
        }
    }
}

fun PollExpiresAt.expiresAt(): Instant? {
    return when (this) {
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

    fun checkValidate(): Boolean {
        return choices.all {
            it.text.isNotBlank()
        } && this.choices.size >= 2
    }

    fun toggleMultiple(): PollEditingState {
        return this.copy(
            multiple = !this.multiple
        )
    }
}

data class PollChoiceState(
    val text: String,
    val id: UUID = UUID.randomUUID()
)

fun DraftNote.toNoteEditingState(): NoteEditingState {
    return NoteEditingState(
        text = this.text,
        cw = this.cw,
        draftNoteId = this.draftNoteId,
        visibility = Visibility(
            type = this.visibility,
            isLocalOnly = this.localOnly ?: false,
            visibleUserIds = this.visibleUserIds?.map {
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
                } ?: PollExpiresAt.Infinity,
                multiple = it.multiple
            )
        },
        replyId = this.replyId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        renoteId = this.renoteId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        files = draftFiles?.map {
            AppFile.from(it)
        } ?: emptyList(),
        reservationPostingAt = reservationPostingAt?.let {
            Instant.fromEpochMilliseconds(it.time)
        },
        channelId = channelId,
        textCursorPos = null,
    )
}

fun PollEditingState.toCreatePoll(): CreatePoll {
    return CreatePoll(
        choices = this.choices.map {
            it.text
        },
        multiple = multiple,
        expiresAt = expiresAt.expiresAt()?.toEpochMilliseconds()
    )
}



fun NoteEditingState.toCreateNote(account: Account): CreateNote {
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
        channelId = channelId,
        scheduleWillPostAt = reservationPostingAt
    )
}