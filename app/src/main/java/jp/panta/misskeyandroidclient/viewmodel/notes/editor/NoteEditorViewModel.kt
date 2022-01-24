package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.file.toFile
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class NoteEditorViewModel(
    private val miCore: MiCore,
    private val draftNoteDao: DraftNoteDao,
    replyId: Note.Id? = null,
    quoteToNoteId: Note.Id? = null,
    loggerFactory: Logger.Factory,
    dn: DraftNote? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val logger = loggerFactory.create("NoteEditorViewModel")

    private val _state = MutableStateFlow(
        dn?.toNoteEditingState()
            ?: NoteEditingState(
                renoteId = quoteToNoteId,
                replyId = replyId
            )
    )
    val state: StateFlow<NoteEditingState> = _state

    val text = _state.map {
        it.text
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null)
    val cw = _state.map {
        it.cw
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null)

    private val currentAccount = MutableLiveData<Account>().apply {
        miCore.getCurrentAccount().onEach {
            this.postValue(it)
        }.launchIn(viewModelScope + dispatcher)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val currentUser: StateFlow<UserViewData?> = miCore.getCurrentAccount().filterNotNull().map {
        val userId = User.Id(it.accountId, it.remoteId)
        UserViewData(
            userId,
            miCore,
            viewModelScope,
            dispatcher
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val draftNote = MutableLiveData<DraftNote>(dn)

    //val replyToNoteId = MutableLiveData<Note.Id>(replyId)
    val reply = _state.map {
        it.replyId?.let { noteId ->
            runCatching {
                miCore.getNoteRepository().find(noteId)
            }.getOrNull()
        }
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = null)

    val renoteId = _state.map {
        it.renoteId
    }.asLiveData()

    val hasCw = _state.map {
        it.hasCw
    }.asLiveData()


    val maxTextLength = miCore.getCurrentAccount().filterNotNull().map {
        miCore.getMetaRepository().get(it.instanceDomain)?.maxNoteTextLength ?: 1500
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    val textRemaining = combine(maxTextLength, text) { max, t ->
        max - (t?.codePointCount(0, t.length) ?: 0)
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)


    val files = _state.map {
        it.files
    }.asLiveData()

    val totalImageCount = _state.map {
        it.totalFilesCount
    }.asLiveData()


    val isPostAvailable = _state.map {
        it.checkValidate(textMaxLength = maxTextLength.value)
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


    @FlowPreview
    @ExperimentalCoroutinesApi
    val address = visibility.map {
        it as? Visibility.Specified
    }.map {
        it?.visibleUserIds?.map { uId ->
            setUpUserViewData(uId)
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setUpUserViewData(userId: User.Id): UserViewData {
        return UserViewData(userId, miCore, viewModelScope, dispatcher)
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

    init {
        miCore.getCurrentAccount().filterNotNull().onEach {
            _state.value = runCatching {
                _state.value.setAccount(it)
            }.getOrElse {
                NoteEditingState()
            }
        }.launchIn(viewModelScope + Dispatchers.IO)

        miCore.getCurrentAccount().filterNotNull().onEach {
            val v = miCore.getSettingStore().getNoteVisibility(it.accountId)
            _state.value = _state.value.copy(
                visibility = v
            )
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

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun post() {
        currentAccount.value?.let { account ->
            viewModelScope.launch(Dispatchers.IO) {

                val reservationPostingAt = _state.value.reservationPostingAt
                if (reservationPostingAt == null || reservationPostingAt <= Clock.System.now()) {
                    val createNote = _state.value.toCreateNote(account)
                    miCore.getTaskExecutor().dispatch(createNote.task(miCore.getNoteRepository()))
                } else {
                    runCatching {
                        val dfNote = toDraftNote()

                        val result = miCore.getDraftNoteDAO().fullInsert(dfNote)
                        dfNote.draftNoteId = result
                        miCore.getNoteReservationPostExecutor().register(dfNote)
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
                _state.value = state.value.copy(
                    files = _state.value.files.map {
                        if (appFile === appFile) {
                            appFile.copy(
                                isSensitive = !appFile.isSensitive
                            )
                        } else {
                            appFile
                        }
                    }
                )
            }
            is AppFile.Remote -> {
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        miCore.getDriveFileRepository().toggleNsfw(appFile.id)
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
        val files = files.value?.toMutableList()
            ?: mutableListOf()
        files.addAll(fpList.map {
            AppFile.Remote(it.id)
        })
        _state.value = _state.value.copy(
            files = files
        )
    }

    fun addFilePropertyFromIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                miCore.getFilePropertyDataSource().findIn(ids)
            }.onSuccess {
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
        _state.value = _state.value.copy(
            visibility = visibility
        )
        this.visibilitySelectedEvent.event = Unit
    }


    fun toggleReservationAt() {
        _state.value = _state.value.copy(
            reservationPostingAt = if (_state.value.reservationPostingAt == null) Clock.System.now() else null
        )
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
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


    fun addMentionUsers(users: List<User>, pos: Int): Int {
        val userNames = users.map {
            it.getDisplayUserName()
        }
        return addMentionUserNames(userNames, pos)
    }

    fun addMentionUserNames(userNames: List<String>, pos: Int): Int {
        val mentionBuilder = StringBuilder()
        userNames.forEachIndexed { index, userName ->
            if (index < userNames.size - 1) {
                mentionBuilder.appendLine(userName)
            } else {
                mentionBuilder.append(userName)
            }
        }
        val builder = StringBuilder(text.value ?: "")
        builder.insert(pos, mentionBuilder.toString())
        _state.value = _state.value.changeText(builder.toString())
        return pos + mentionBuilder.length
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

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun toDraftNote(): DraftNote {
        return DraftNote(
            accountId = currentAccount.value?.accountId!!,
            text = _state.value.text,
            cw = _state.value.cw,
            visibleUserIds = address.value.mapNotNull {
                it.userId?.id ?: it.user.value?.id?.id
            },
            draftPoll = poll.value?.toDraftPoll(),
            visibility = visibility.value.type(),
            localOnly = visibility.value.isLocalOnly(),
            renoteId = _state.value.renoteId?.noteId,
            replyId = _state.value.replyId?.noteId,
            files = files.value?.map {
                it.toFile()
            },
            reservationPostingAt = _state.value.reservationPostingAt?.toEpochMilliseconds()?.let{
                Date(it)
            }
        ).apply {
            this.draftNoteId = draftNote.value?.draftNoteId
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun saveDraft() {
        if (!canSaveDraft()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dfNote = toDraftNote()

                try {
                    isSaveNoteAsDraft.event = draftNoteDao.fullInsert(dfNote)
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

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun canSaveDraft(): Boolean {
        return !(_state.value.text.isNullOrBlank()
                && files.value.isNullOrEmpty()
                && poll.value?.choices.isNullOrEmpty()
                && address.value.isNullOrEmpty())
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
    fun clear() {
        _state.value = _state.value.clear()
    }


}