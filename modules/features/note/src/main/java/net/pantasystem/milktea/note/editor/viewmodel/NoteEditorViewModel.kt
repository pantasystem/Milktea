package net.pantasystem.milktea.note.editor.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.NoteEditingState
import net.pantasystem.milktea.app_store.notes.toCreateNote
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.CreateNoteTaskExecutor
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.channel.Channel
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
import java.io.IOException
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
    private val createNoteTaskExecutor: CreateNoteTaskExecutor,
    private val createNoteUseCase: CreateNoteUseCase,
    private val draftNoteService: DraftNoteService,
    private val draftNoteRepository: DraftNoteRepository,
    private val noteReservationPostExecutor: NoteReservationPostExecutor,
    private val userViewDataFactory: UserViewData.Factory,
    private val settingStore: SettingStore,
    private val noteRepository: NoteRepository
) : ViewModel() {


    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val logger = loggerFactory.create("NoteEditorViewModel")

    private val _state = MutableStateFlow(NoteEditingState())
    val state: StateFlow<NoteEditingState> = _state

    val text = _state.map {
        it.text
    }.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)
    val cw = _state.map {
        it.cw
    }.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)

    private val currentAccount = MutableLiveData<Account>().apply {
        accountStore.observeCurrentAccount.onEach {
            this.postValue(it)
        }.launchIn(viewModelScope + dispatcher)
    }

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


    val hasCw = _state.map {
        it.hasCw
    }.asLiveData()


    val maxTextLength = accountStore.observeCurrentAccount.filterNotNull().map {
        metaRepository.get(it.instanceDomain)?.maxNoteTextLength ?: 1500
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    val textRemaining = combine(maxTextLength, state.map { it.text }) { max, t ->
        max - (t?.codePointCount(0, t.length) ?: 0)
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    val maxFileCount = accountStore.observeCurrentAccount.filterNotNull().mapNotNull {
        metaRepository.get(it.instanceDomain)?.getVersion()
    }.map {
        if (it >= Version("12.100.2")) {
            16
        } else {
            4
        }
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Eagerly, initialValue = 4)


    val files = _state.map {
        it.files
    }.asLiveData()

    val totalImageCount = _state.map {
        it.totalFilesCount
    }.asLiveData()


    val isPostAvailable = _state.map {
        it.checkValidate(textMaxLength = maxTextLength.value, maxFileCount = maxFileCount.value)
    }.asLiveData()

    val visibility = _state.map {
        it.visibility
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = Visibility.Public(false))


    val isLocalOnly = _state.map {
        it.visibility.isLocalOnly()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)


    val isLocalOnlyEnabled = _state.map {
        it.visibility is CanLocalOnly
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val reservationPostingAt = _state.map {
        it.reservationPostingAt
    }.map { instant ->
        instant?.toEpochMilliseconds()?.let {
            Date(it)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


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

    val isSpecified = _state.map {
        it.isSpecified
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val poll = _state.map {
        it.poll
    }.distinctUntilChanged()
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)

    //val noteTask = MutableLiveData<PostNoteTask>()
    val isPost = EventBus<Boolean>()

    val showPollDatePicker = EventBus<Unit>()
    val showPollTimePicker = EventBus<Unit>()


    val isSaveNoteAsDraft = EventBus<Long?>()

    fun setRenoteTo(noteId: Note.Id?) {
        _state.value = _state.value.changeRenoteId(noteId)
    }

    fun setReplyTo(noteId: Note.Id?) {
        _state.value = _state.value.changeReplyTo(noteId)
        if (noteId == null) {
            return
        }
        viewModelScope.launch {

            // NOTE: リプライ先のcwの状態をフォームに反映するようにする
            noteRepository.find(noteId).onSuccess { note ->
                _state.update { state ->
                    state.changeCw(note.cw)
                }
            }

            getAllMentionUsersUseCase(noteId).onSuccess { users ->
                _state.update { state ->
                    state.addMentionUserNames(users.map { it.displayUserName }, 0).state
                }
            }
        }
    }

    fun setDraftNoteId(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            draftNoteRepository.findOne(id).onSuccess { note ->
                _state.value = _state.value.setDraftNote(note)
            }
        }

    }

    init {
        accountStore.observeCurrentAccount.filterNotNull().onEach {
            _state.value = runCatching {
                _state.value.setAccount(it)
            }.getOrElse {
                NoteEditingState()
            }
        }.launchIn(viewModelScope + Dispatchers.IO)

        accountStore.observeCurrentAccount.filterNotNull().onEach {
            val v = settingStore.getNoteVisibility(it.accountId)
            _state.value = _state.value.setVisibility(v)

        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun changeText(text: String) {
        _state.value = _state.value.changeText(text)
    }

    fun addPollChoice() {
        _state.value = _state.value.addPollChoice()
    }

    fun changePollChoice(id: UUID, text: String) {
        _state.value = _state.value.updatePollChoice(id, text)
    }

    fun removePollChoice(id: UUID) {
        _state.value = _state.value.removePollChoice(id)
    }

    fun updateState(state: NoteEditingState) {
        _state.value = state
    }

    fun togglePollMultiple() {
        _state.value = state.value.copy(
            poll = state.value.poll?.toggleMultiple()
        )
    }


    fun post() {
        currentAccount.value?.let { account ->
            viewModelScope.launch(Dispatchers.IO) {

                val reservationPostingAt = _state.value.reservationPostingAt
                if (reservationPostingAt == null || reservationPostingAt <= Clock.System.now()) {
                    val createNote = _state.value.toCreateNote(account)
                    createNoteTaskExecutor.dispatch(createNote.task(createNoteUseCase))
                } else {
                    draftNoteService.save(_state.value.toCreateNote(account))
                        .mapCatching { dfNote ->
                            noteReservationPostExecutor.register(dfNote)
                        }.onFailure {
                        logger.error("登録失敗", it)
                    }
                }
                withContext(Dispatchers.Main) {
                    isPost.event = true
                }
            }

        }

    }

    fun toggleNsfw(appFile: AppFile) {
        when (appFile) {
            is AppFile.Local -> {
                _state.value = state.value.toggleFileSensitiveStatus(appFile)
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
        val files = files.value?.toMutableList()
            ?: mutableListOf()
        files.add(
            file
        )
        _state.value = _state.value.addFile(file)
    }


    private fun addAllFileProperty(fpList: List<FileProperty>) {
        val files = state.value.files.toMutableList()
        files.addAll(fpList.map {
            AppFile.Remote(it.id)
        })
        _state.value = _state.value.copy(
            files = files
        )
    }

    fun addFilePropertyFromIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch(Dispatchers.IO) {
            filePropertyDataSource.findIn(ids).onSuccess {
                addAllFileProperty(it)
            }
        }
    }

    fun removeFileNoteEditorData(file: AppFile) {
        _state.value = _state.value.removeFile(file)
    }


    fun fileTotal(): Int {
        return files.value?.size ?: 0
    }


    fun changeCwEnabled() {
        _state.value = _state.value.toggleCw()
        logger.debug("cw:${cw.value}")
    }

    fun enablePoll() {
        _state.value = _state.value.togglePoll()

    }

    fun disablePoll() {
        _state.value = _state.value.togglePoll()
    }

    fun showVisibilitySelection() {
        showVisibilitySelectionEvent.event = Unit
    }

    fun setText(text: String) {
        _state.value = _state.value.changeText(text)
    }

    fun setCw(text: String?) {
        _state.value = _state.value.changeCw(text)
    }

    fun setVisibility(visibility: Visibility) {
        logger.debug("公開範囲がセットされた:$visibility")
        _state.value = _state.value.setVisibility(visibility)
        this.visibilitySelectedEvent.event = Unit
    }

    fun setChannelId(channelId: Channel.Id?) {
        _state.value = _state.value.setChannelId(channelId)
    }

    fun toggleReservationAt() {
        _state.value = _state.value.copy(
            reservationPostingAt = if (_state.value.reservationPostingAt == null) Clock.System.now() else null
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

        _state.value = _state.value.copy(
            visibility = Visibility.Specified(list)
        )
    }


    fun addMentionUserNames(userNames: List<String>, pos: Int): Int {
        val result = _state.value.addMentionUserNames(userNames, pos)
        _state.value = result.state
        return result.cursorPos
    }

    fun addEmoji(emoji: Emoji, pos: Int): Int {
        return addEmoji(":${emoji.name}:", pos)
    }

    fun addEmoji(emoji: String, pos: Int): Int {
        val builder = StringBuilder(_state.value.text ?: "")
        builder.insert(pos, emoji)
        _state.value = _state.value.changeText(builder.toString())
        logger.debug("position:${pos + emoji.length - 1}")
        return pos + emoji.length
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
                        draftNoteService.save(_state.value.toCreateNote(account)).getOrThrow()
                    isSaveNoteAsDraft.event = result.draftNoteId
                } catch (e: Exception) {
                    logger.error("下書き書き込み中にエラー発生：失敗してしまった", e)
                }
            } catch (e: IOException) {

            } catch (e: NullPointerException) {
                logger.error("下書き保存に失敗した", e)

            } catch (e: Throwable) {
                logger.error("下書き保存に失敗した", e)

            }

        }
    }

    fun canSaveDraft(): Boolean {
        return _state.value.shouldDiscardingConfirmation()
    }


    fun clear() {
        _state.value = _state.value.clear()
    }


}