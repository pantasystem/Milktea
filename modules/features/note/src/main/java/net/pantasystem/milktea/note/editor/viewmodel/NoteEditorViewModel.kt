package net.pantasystem.milktea.note.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    private val getAllMentionUsersUseCase: GetAllMentionUsersUseCase,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val metaRepository: MetaRepository,
    private val driveFileRepository: DriveFileRepository,
    private val accountStore: AccountStore,
    private val draftNoteService: DraftNoteService,
    private val draftNoteRepository: DraftNoteRepository,
    private val noteReservationPostExecutor: NoteReservationPostExecutor,
    private val userViewDataFactory: UserViewData.Factory,
    private val settingStore: SettingStore,
    private val noteRepository: NoteRepository,
    private val channelRepository: ChannelRepository,
    private val noteEditorSwitchAccountExecutor: NoteEditorSwitchAccountExecutor,
    private val createNoteWorkerExecutor: CreateNoteWorkerExecutor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val logger = loggerFactory.create("NoteEditorViewModel")

    val text = savedStateHandle.getStateFlow<String?>(NoteEditorSavedStateKey.Text.name, null)

    val cw = savedStateHandle.getStateFlow<String?>(NoteEditorSavedStateKey.Cw.name, null)

    private val currentAccount = MutableStateFlow<Account?>(null)

    @FlowPreview
    @ExperimentalCoroutinesApi
    val currentUser: StateFlow<UserViewData?> =
        accountStore.state.map { it.currentAccount }.filterNotNull().map {
            val userId = User.Id(it.accountId, it.remoteId)
            userViewDataFactory.create(
                userId,
                viewModelScope,
                dispatcher
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


    val hasCw = savedStateHandle.getStateFlow(NoteEditorSavedStateKey.HasCW.name, false)


    @OptIn(ExperimentalCoroutinesApi::class)
    val maxTextLength =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            metaRepository.observe(account.instanceDomain).filterNotNull().map { meta ->
                meta.maxNoteTextLength ?: 1500
            }
        }.stateIn(
            viewModelScope + Dispatchers.IO,
            started = SharingStarted.Lazily,
            initialValue = 1500
        )


    val maxFileCount = accountStore.observeCurrentAccount.filterNotNull().mapNotNull {
        metaRepository.get(it.instanceDomain)?.getVersion()
    }.map {
        if (it >= Version("12.100.2")) {
            16
        } else {
            4
        }
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Eagerly, initialValue = 4)

    @OptIn(ExperimentalCoroutinesApi::class)
    val channels = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        suspend {
            channelRepository.findFollowedChannels(it.accountId).onFailure {
                logger.error("load channel error", it)
            }.getOrThrow()
        }.asLoadingStateFlow().onEach {
            logger.debug("Channel state:${it}")
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = ResultState.Loading(StateContent.NotExist())
    )

    val files = savedStateHandle.getStateFlow<List<AppFile>>(
        NoteEditorSavedStateKey.PickedFiles.name,
        emptyList()
    )

    val totalImageCount = files.map {
        it.size
    }.asLiveData()


    val visibility = savedStateHandle.getStateFlow<Visibility>(
        NoteEditorSavedStateKey.Visibility.name,
        Visibility.Public(false)
    )
    val isLocalOnly = visibility.map {
        it.isLocalOnly()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)


    val reservationPostingAt =
        savedStateHandle.getStateFlow<Date?>(NoteEditorSavedStateKey.ScheduleAt.name, null)


    val showVisibilitySelectionEvent = EventBus<Unit>()
    private val visibilitySelectedEvent = EventBus<Unit>()


    val address = visibility.map {
        it as? Visibility.Specified
    }.map {
        it?.visibleUserIds?.map { uId ->
            setUpUserViewData(uId)
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    private fun setUpUserViewData(userId: User.Id): UserViewData {
        return userViewDataFactory.create(userId, viewModelScope, dispatcher)
    }

    val isSpecified = visibility.map {
        it is Visibility.Specified
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val poll =
        savedStateHandle.getStateFlow<PollEditingState?>(NoteEditorSavedStateKey.Poll.name, null)

    private val noteEditorFormState = combine(text, cw, hasCw) { text, cw, hasCw ->
        NoteEditorFormState(
            text = text,
            cw = cw,
            hasCw = hasCw
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteEditorFormState())

    val textRemaining = combine(maxTextLength, noteEditorFormState.map { it.text }) { max, t ->
        max - (t?.codePointCount(0, t.length) ?: 0)
    }.catch {
        logger.error("observe meta error", it)
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    private val channelId =
        savedStateHandle.getStateFlow<Channel.Id?>(NoteEditorSavedStateKey.ChannelId.name, null)
    private val replyId =
        savedStateHandle.getStateFlow<Note.Id?>(NoteEditorSavedStateKey.ReplyId.name, null)
    private val renoteId =
        savedStateHandle.getStateFlow<Note.Id?>(NoteEditorSavedStateKey.RenoteId.name, null)

    private val draftNoteId =
        savedStateHandle.getStateFlow<Long?>(NoteEditorSavedStateKey.DraftNoteId.name, null)

    private val visibilityAndChannelId = combine(visibility, channelId) { v, c ->
        VisibilityAndChannelId(v, c)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VisibilityAndChannelId())

    private val noteEditorSendToState = combine(
        visibilityAndChannelId,
        replyId,
        renoteId,
        reservationPostingAt,
        draftNoteId,
    ) { vc, replyId, renoteId, scheduleDate, dfId ->
        NoteEditorSendToState(
            visibility = vc.visibility,
            channelId = vc.channelId,
            replyId = replyId,
            renoteId = renoteId,
            schedulePostAt = scheduleDate?.let {
                Instant.fromEpochMilliseconds(it.time)
            },
            draftNoteId = dfId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteEditorSendToState())

    val uiState = combine(
        noteEditorFormState,
        noteEditorSendToState,
        files,
        poll,
        currentAccount,
    ) { formState, sendToState, files, poll, account ->
        NoteEditorUiState(
            formState = formState,
            sendToState = sendToState,
            poll = poll,
            files = files,
            currentAccount = account,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteEditorUiState())

    val isPostAvailable = uiState.map {
        it.checkValidate(textMaxLength = maxTextLength.value, maxFileCount = maxFileCount.value)
    }.asLiveData()

    val isPost = EventBus<Boolean>()

    val showPollDatePicker = EventBus<Unit>()
    val showPollTimePicker = EventBus<Unit>()


    val isSaveNoteAsDraft = EventBus<Long?>()

    fun setRenoteTo(noteId: Note.Id?) {
        savedStateHandle.setRenoteId(noteId)
    }

    fun setReplyTo(noteId: Note.Id?) {
        savedStateHandle.setReplyId(noteId)
        if (noteId == null) {
            return
        }
        viewModelScope.launch {

            // NOTE: リプライ先のcwの状態をフォームに反映するようにする
            noteRepository.find(noteId).onSuccess { note ->
                savedStateHandle.setHasCw(note.cw != null)
                savedStateHandle.setCw(note.cw)
                savedStateHandle.setVisibility(note.visibility)
                savedStateHandle.setChannelId(note.channelId)
            }

            getAllMentionUsersUseCase(noteId).onSuccess { users ->
                val (text, _) = savedStateHandle.getText()
                    .addMentionUserNames(
                        users.map { it.displayUserName }, 0
                    )
                savedStateHandle.setText(text)
            }
        }
    }

    fun setDraftNoteId(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            draftNoteRepository.findOne(id).mapCatching {
                it.toNoteEditingState()
            }.onSuccess { note ->
                currentAccount.value = note.author
                savedStateHandle.setVisibility(note.visibility)
                savedStateHandle.setText(note.text)
                savedStateHandle.setCw(note.cw)
                savedStateHandle.setHasCw(note.cw != null)
                savedStateHandle.setFiles(note.files)
                savedStateHandle.setReplyId(note.replyId)
                savedStateHandle.setRenoteId(note.renoteId)
                savedStateHandle.setPoll(note.poll)
                savedStateHandle.setDraftNoteId(note.draftNoteId)
                savedStateHandle.setChannelId(note.channelId)
                savedStateHandle.setScheduleAt(
                    note.reservationPostingAt?.let {
                        Date(it.toEpochMilliseconds())
                    }
                )
            }
        }

    }

    init {
        accountStore.observeCurrentAccount.filterNotNull().map {
            it to noteEditorSwitchAccountExecutor(
                currentAccount.value,
                noteEditorSendToState.value,
                it
            )
        }.onEach { (account, result) ->
            currentAccount.value = account
            savedStateHandle.setReplyId(result.replyId)
            savedStateHandle.setRenoteId(result.renoteId)
            savedStateHandle.setVisibility(result.visibility)
            savedStateHandle.setChannelId(result.channelId)
        }.launchIn(viewModelScope + Dispatchers.IO)

        accountStore.observeCurrentAccount.filterNotNull().onEach {
            val v = settingStore.getNoteVisibility(it.accountId)
            if (channelId.value == null) {
                savedStateHandle.setVisibility(v)
            }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun changeText(text: String) {
        savedStateHandle[NoteEditorSavedStateKey.Text.name] = text
    }

    fun addPollChoice() {
        savedStateHandle.setPoll(savedStateHandle.getPoll().addPollChoice())
    }

    fun changePollChoice(id: UUID, text: String) {
        savedStateHandle.setPoll(
            savedStateHandle.getPoll().updatePollChoice(id, text)
        )
    }

    fun removePollChoice(id: UUID) {
        savedStateHandle.setPoll(
            savedStateHandle.getPoll().removePollChoice(id)
        )
    }


    fun togglePollMultiple() {
        savedStateHandle.setPoll(savedStateHandle.getPoll()?.toggleMultiple())
    }

    fun setPollExpiresAt(expiresAt: PollExpiresAt) {
        val state = savedStateHandle.getPoll()
        savedStateHandle.setPoll(
            state?.copy(
                expiresAt = expiresAt
            )
        )
    }


    fun post() {
        currentAccount.value?.let { account ->
            viewModelScope.launch(Dispatchers.IO) {
                val reservationPostingAt = uiState.value.sendToState.schedulePostAt
                draftNoteService.save(uiState.value.toCreateNote(account)).mapCatching { dfNote ->
                    if (reservationPostingAt == null || reservationPostingAt <= Clock.System.now()) {
                        createNoteWorkerExecutor.enqueue(dfNote.draftNoteId)
                    } else {
                        noteReservationPostExecutor.register(dfNote)
                    }
                }.onSuccess {
                    withContext(Dispatchers.Main) {
                        isPost.event = true
                    }
                }.onFailure {
                    logger.error("登録失敗", it)
                }
            }
        }

    }

    fun toggleNsfw(appFile: AppFile) {
        when (appFile) {
            is AppFile.Local -> {
                savedStateHandle.setFiles(files.value.toggleFileSensitiveStatus(appFile))
                savedStateHandle[NoteEditorSavedStateKey.PickedFiles.name] =
                    files.value.toggleFileSensitiveStatus(appFile)
            }
            is AppFile.Remote -> {
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        driveFileRepository.toggleNsfw(appFile.id)
                    }
                }
            }
        }

    }

    fun add(file: AppFile) {
        val files = files.value.toMutableList()
        files.add(
            file
        )
        savedStateHandle.setFiles(files)
    }


    private fun addAllFileProperty(fpList: List<FileProperty>) {
        val files = savedStateHandle.getFiles().toMutableList()
        files.addAll(fpList.map {
            AppFile.Remote(it.id)
        })
        savedStateHandle.setFiles(files)

    }

    fun addFilePropertyFromIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch(Dispatchers.IO) {
            filePropertyDataSource.findIn(ids).onSuccess {
                addAllFileProperty(it)
            }
        }
    }

    fun removeFileNoteEditorData(file: AppFile) {
        savedStateHandle.setFiles(
            savedStateHandle.getFiles().removeFile(file)
        )
    }


    fun fileTotal(): Int {
        return files.value.size
    }

    fun changeCwEnabled() {
        savedStateHandle.setHasCw(!savedStateHandle.getHasCw())
    }

    fun enablePoll() {
        val poll =
            if (savedStateHandle.getPoll() == null) PollEditingState(emptyList(), false) else null
        savedStateHandle.setPoll(poll)
    }

    fun disablePoll() {
        savedStateHandle.setPoll(null)
    }

    fun showVisibilitySelection() {
        showVisibilitySelectionEvent.event = Unit
    }

    fun setText(text: String) {
        savedStateHandle.setText(text)
    }

    fun setCw(text: String?) {
        savedStateHandle.setCw(text)
    }

    fun setVisibility(visibility: Visibility) {
        logger.debug("公開範囲がセットされた:$visibility")
        savedStateHandle.setChannelId(null)
        savedStateHandle.setVisibility(visibility)
        this.visibilitySelectedEvent.event = Unit
    }

    fun setChannelId(channelId: Channel.Id?) {
        val visibility = savedStateHandle.getVisibility()
        savedStateHandle.setChannelId(channelId)
        savedStateHandle.setVisibility(
            if (channelId == null) visibility else Visibility.Public(true),
        )
    }

    fun toggleReservationAt() {
        savedStateHandle.setScheduleAt(
            if (reservationPostingAt.value == null) Date(
                Clock.System.now().toEpochMilliseconds()
            ) else null
        )
    }


    fun setAddress(added: List<User.Id>, removed: List<User.Id>) {
        val list = ((visibility.value as? Visibility.Specified)?.visibleUserIds
            ?: emptyList()).toMutableList()
        list.addAll(
            added
        )

        list.removeAll {
            removed.any()
        }

        savedStateHandle.setVisibility(Visibility.Specified(list))
    }


    fun addMentionUserNames(userNames: List<String>, pos: Int): Int {
        val (text, nextPos) = savedStateHandle.getText()
            .addMentionUserNames(userNames, pos)
        savedStateHandle.setText(text)
        return nextPos
    }

    fun addEmoji(emoji: Emoji, pos: Int): Int {
        return addEmoji(":${emoji.name}:", pos)
    }

    fun addEmoji(emoji: String, pos: Int): Int {
        val builder = StringBuilder(savedStateHandle.getText() ?: "")
        builder.insert(pos, emoji)
        savedStateHandle.setText(builder.toString())
        logger.debug("position:${pos + emoji.length - 1}")
        return pos + emoji.length
    }

    fun setSchedulePostAt(instant: Instant?) {
        savedStateHandle.setScheduleAt(instant?.let {
            Date(it.toEpochMilliseconds())
        })
    }


    fun saveDraft() {
        if (!canSaveDraft()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                try {
                    val account = accountStore.currentAccount ?: throw UnauthorizedException()
                    val result =
                        draftNoteService.save(uiState.value.toCreateNote(account)).getOrThrow()
                    isSaveNoteAsDraft.event = result.draftNoteId
                } catch (e: Exception) {
                    logger.error("下書き書き込み中にエラー発生：失敗してしまった", e)
                }
            } catch (e: NullPointerException) {
                logger.error("下書き保存に失敗した", e)

            } catch (e: Throwable) {
                logger.error("下書き保存に失敗した", e)

            }

        }
    }

    fun canSaveDraft(): Boolean {
        return uiState.value.shouldDiscardingConfirmation()
    }


    fun clear() {
        savedStateHandle.setVisibility(Visibility.Public(false))
        savedStateHandle.setHasCw(false)
        savedStateHandle.setCw(null)
        savedStateHandle.setText(null)
        savedStateHandle.setFiles(emptyList())
        savedStateHandle.setPoll(null)
        savedStateHandle.setScheduleAt(null)
    }


}

data class NoteEditorFormState(
    val text: String? = null,
    val cw: String? = null,
    val hasCw: Boolean = false,
)

data class VisibilityAndChannelId(
    val visibility: Visibility = Visibility.Public(false),
    val channelId: Channel.Id? = null,
)

data class NoteEditorSendToState(
    val visibility: Visibility = Visibility.Public(false),
    val channelId: Channel.Id? = null,
    val renoteId: Note.Id? = null,
    val replyId: Note.Id? = null,
    val schedulePostAt: Instant? = null,
    val draftNoteId: Long? = null,
)

data class NoteEditorUiState(
    val formState: NoteEditorFormState = NoteEditorFormState(),
    val sendToState: NoteEditorSendToState = NoteEditorSendToState(),
    val poll: PollEditingState? = null,
    val files: List<AppFile> = emptyList(),
    val currentAccount: Account? = null,
) {
    val totalFilesCount: Int
        get() = this.files.size

    fun checkValidate(textMaxLength: Int = 3000, maxFileCount: Int = 4): Boolean {
        if (this.files.size > maxFileCount) {
            return false
        }

        if ((this.formState.text?.codePointCount(0, this.formState.text.length)
                ?: 0) > textMaxLength
        ) {
            return false
        }

        if (sendToState.channelId != null && sendToState.visibility != Visibility.Public(true)) {
            return false
        }

        if (this.sendToState.renoteId != null) {
            return true
        }
        if (this.poll != null && this.poll.checkValidate()) {
            return true
        }
        return !(
                this.formState.text.isNullOrBlank()
                        && this.files.isEmpty()
                )
    }

    fun shouldDiscardingConfirmation(): Boolean {
        val address = (sendToState.visibility as? Visibility.Specified)?.visibleUserIds
            ?: emptyList()
        return !formState.text.isNullOrBlank()
                || files.isNotEmpty()
                || !poll?.choices.isNullOrEmpty()
                || address.isNotEmpty()
    }
}


fun NoteEditorUiState.toCreateNote(account: Account): CreateNote {
    return CreateNote(
        author = account,
        visibility = sendToState.visibility,
        text = formState.text,
        cw = if (formState.hasCw) formState.text else null,
        viaMobile = false,
        files = files,
        replyId = sendToState.replyId,
        renoteId = sendToState.renoteId,
        poll = poll?.toCreatePoll(),
        draftNoteId = sendToState.draftNoteId,
        channelId = sendToState.channelId,
        scheduleWillPostAt = sendToState.schedulePostAt,
    )
}

enum class NoteEditorSavedStateKey() {
    Text, Cw, PickedFiles, Visibility, ChannelId, ReplyId, RenoteId, ScheduleAt, DraftNoteId, HasCW, Poll
}


fun SavedStateHandle.setText(text: String?) {
    this[NoteEditorSavedStateKey.Text.name] = text
}

fun SavedStateHandle.getText(): String? {
    return this[NoteEditorSavedStateKey.Text.name]
}

fun SavedStateHandle.setCw(text: String?) {
    this[NoteEditorSavedStateKey.Cw.name] = text
}

fun SavedStateHandle.setFiles(files: List<AppFile>) {
    this[NoteEditorSavedStateKey.PickedFiles.name] = files
}

fun SavedStateHandle.getFiles(): List<AppFile> {
    return this[NoteEditorSavedStateKey.PickedFiles.name] ?: emptyList()
}

fun SavedStateHandle.setChannelId(channelId: Channel.Id?) {
    this[NoteEditorSavedStateKey.ChannelId.name] = channelId
}


fun SavedStateHandle.setReplyId(noteId: Note.Id?) {
    this[NoteEditorSavedStateKey.ReplyId.name] = noteId
}

fun SavedStateHandle.setRenoteId(noteId: Note.Id?) {
    this[NoteEditorSavedStateKey.RenoteId.name] = noteId
}


fun SavedStateHandle.setScheduleAt(date: Date?) {
    this[NoteEditorSavedStateKey.ScheduleAt.name] = date
}

fun SavedStateHandle.setHasCw(hasCw: Boolean) {
    this[NoteEditorSavedStateKey.HasCW.name] = hasCw
}

fun SavedStateHandle.getHasCw(): Boolean {
    return this[NoteEditorSavedStateKey.HasCW.name] ?: false
}

fun SavedStateHandle.getPoll(): PollEditingState? {
    return this[NoteEditorSavedStateKey.Poll.name]
}

fun SavedStateHandle.setPoll(value: PollEditingState?) {
    this[NoteEditorSavedStateKey.Poll.name] = value
}

fun SavedStateHandle.setVisibility(visibility: Visibility) {
    this[NoteEditorSavedStateKey.Visibility.name] = visibility
}

fun SavedStateHandle.getVisibility(): Visibility {
    return this[NoteEditorSavedStateKey.Visibility.name] ?: Visibility.Public(false)
}

fun SavedStateHandle.setDraftNoteId(id: Long?) {
    this[NoteEditorSavedStateKey.DraftNoteId.name] = id
}