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
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.text.UrlPatternChecker
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.UpdateFileProperty
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.UpdateAppFileSensitiveUseCase
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.InstanceInfo
import net.pantasystem.milktea.model.instance.InstanceInfoRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.RememberVisibility
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    accountStore: AccountStore,
    private val getAllMentionUsersUseCase: GetAllMentionUsersUseCase,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val instanceInfoService: InstanceInfoService,
    private val driveFileRepository: DriveFileRepository,
    private val draftNoteService: DraftNoteService,
    private val draftNoteRepository: DraftNoteRepository,
    private val noteReservationPostExecutor: NoteReservationPostExecutor,
    private val userViewDataFactory: UserViewData.Factory,
    private val noteRepository: NoteRepository,
    private val channelRepository: ChannelRepository,
    private val noteEditorSwitchAccountExecutor: NoteEditorSwitchAccountExecutor,
    private val createNoteWorkerExecutor: CreateNoteWorkerExecutor,
    private val accountRepository: AccountRepository,
    private val localConfigRepository: LocalConfigRepository,
    private val featureEnables: FeatureEnables,
    private val noteRelationGetter: NoteRelationGetter,
    private val instanceInfoRepository: InstanceInfoRepository,
    private val updateSensitiveUseCase: UpdateAppFileSensitiveUseCase,
    private val apResolverRepository: ApResolverRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val logger = loggerFactory.create("NoteEditorViewModel")

    private val currentAccount = MutableStateFlow<Account?>(null)

    val text = savedStateHandle.getStateFlow<String?>(NoteEditorSavedStateKey.Text.name, null)

    val textCursorPos = MutableSharedFlow<TextWithCursorPos>(extraBufferCapacity = 10)

    val cw = savedStateHandle.getStateFlow<String?>(NoteEditorSavedStateKey.Cw.name, null)
    val hasCw = savedStateHandle.getStateFlow(NoteEditorSavedStateKey.HasCW.name, false)

    val files = savedStateHandle.getStateFlow<List<AppFile>>(
        NoteEditorSavedStateKey.PickedFiles.name,
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val instanceInfoType = currentAccount.filterNotNull().flatMapLatest {
        instanceInfoService.observe(it.normalizedInstanceUri)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isSensitiveMedia =
        savedStateHandle.getStateFlow<Boolean?>(NoteEditorSavedStateKey.IsSensitive.name, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val driveFiles = files.flatMapLatest { files ->
        val fileIds = files.mapNotNull {
            it as? AppFile.Remote
        }.map {
            it.id
        }
        filePropertyDataSource.observeIn(fileIds)
    }.catch {
        logger.error("drive fileの取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val filePreviewSources = combine(files, driveFiles) { files, driveFiles ->
        files.mapNotNull { appFile ->
            when (appFile) {
                is AppFile.Local -> {
                    FilePreviewSource.Local(appFile)
                }
                is AppFile.Remote -> {
                    runCancellableCatching {
                        driveFiles.firstOrNull {
                            it.id == appFile.id
                        } ?: driveFileRepository.find(appFile.id)
                    }.getOrNull()?.let {
                        FilePreviewSource.Remote(appFile, it)
                    }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalImageCount = files.map {
        it.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val channelId =
        savedStateHandle.getStateFlow<Channel.Id?>(NoteEditorSavedStateKey.ChannelId.name, null)
    private val replyId =
        savedStateHandle.getStateFlow<Note.Id?>(NoteEditorSavedStateKey.ReplyId.name, null)
    private val renoteId =
        savedStateHandle.getStateFlow<Note.Id?>(NoteEditorSavedStateKey.RenoteId.name, null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val maxTextLength =
        currentAccount.filterNotNull().flatMapLatest { account ->
            instanceInfoService.observe(account.normalizedInstanceUri).filterNotNull()
                .map { meta ->
                    meta.maxNoteTextLength
                }
        }.stateIn(
            viewModelScope + Dispatchers.IO,
            started = SharingStarted.Lazily,
            initialValue = 1500
        )


    val enableFeatures = currentAccount.filterNotNull().map {
        featureEnables.enableFeatures(it.normalizedInstanceUri)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val maxFileCount = currentAccount.filterNotNull().mapNotNull {
        instanceInfoService.find(it.normalizedInstanceUri).getOrNull()?.maxFileCount
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Eagerly, initialValue = 4)

    @OptIn(ExperimentalCoroutinesApi::class)
    val instanceInfo = currentAccount.filterNotNull().flatMapLatest {
        instanceInfoRepository.observeByHost(it.getHost())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    private val _visibility = savedStateHandle.getStateFlow<Visibility?>(
        NoteEditorSavedStateKey.Visibility.name,
        null
    )
    val visibility = combine(_visibility, currentAccount.filterNotNull().map {
        localConfigRepository.getRememberVisibility(it.accountId).getOrElse {
            RememberVisibility.None
        }
    }, channelId) { formVisibilityState, settingVisibilityState, channelId ->
        when {
            formVisibilityState != null -> formVisibilityState
            settingVisibilityState is RememberVisibility.None -> Visibility.Public(false)
            settingVisibilityState is RememberVisibility.Remember -> settingVisibilityState.visibility
            channelId != null -> Visibility.Public(true)
            else -> Visibility.Public(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Visibility.Public(false))

    val isLocalOnly = visibility.map {
        it.isLocalOnly()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val reservationPostingAt =
        savedStateHandle.getStateFlow<Date?>(NoteEditorSavedStateKey.ScheduleAt.name, null)

    val poll = savedStateHandle.getStateFlow<PollEditingState?>(
        NoteEditorSavedStateKey.Poll.name,
        null
    )

    private val noteEditorFormState =
        combine(text, cw, hasCw, isSensitiveMedia) { text, cw, hasCw, sensitive ->
            NoteEditorFormState(
                text = text,
                cw = cw,
                hasCw = hasCw,
                isSensitive = sensitive ?: false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteEditorFormState())

    val address = visibility.map {
        it as? Visibility.Specified
    }.map {
        it?.visibleUserIds?.map { uId ->
            setUpUserViewData(uId)
        } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isSpecified = visibility.map {
        it is Visibility.Specified
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val textRemaining = combine(maxTextLength, noteEditorFormState.map { it.text }) { max, t ->
        max - (t?.codePointCount(0, t.length) ?: 0)
    }.catch {
        logger.error("observe meta error", it)
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    @OptIn(ExperimentalCoroutinesApi::class)
    val channels = currentAccount.filterNotNull().flatMapLatest {
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

    @FlowPreview
    @ExperimentalCoroutinesApi
    val currentUser: StateFlow<UserViewData?> =
        currentAccount.filterNotNull().map {
            val userId = User.Id(it.accountId, it.remoteId)
            userViewDataFactory.create(
                userId,
                viewModelScope,
                dispatcher
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


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
        filePreviewSources,
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

    private val cache = planeNoteViewDataCacheFactory.create({
        requireNotNull(currentAccount.value)
    }, viewModelScope)

    val replyTo = replyId.map { id ->
        noteRelationGetter.get(id ?: throw UnauthorizedException()).getOrThrow()?.let {
            cache.get(it)
        }
    }.catch {
        emit(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _fileSizeInvalidEvent =
        MutableSharedFlow<FileSizeInvalidEvent>(extraBufferCapacity = 10)
    val fileSizeInvalidEvent = _fileSizeInvalidEvent.asSharedFlow()

    val isPost = EventBus<Boolean>()

    val showPollDatePicker = EventBus<Unit>()
    val showPollTimePicker = EventBus<Unit>()


    val isSaveNoteAsDraft = EventBus<Long?>()

    var focusType: NoteEditorFocusEditTextType = NoteEditorFocusEditTextType.Text

    init {
        accountStore.observeCurrentAccount.filterNotNull().map {
            it to noteEditorSwitchAccountExecutor(
                currentAccount.value,
                noteEditorSendToState.value,
                it
            )
        }.onEach { (account, result) ->
            if (account.accountId != currentAccount.value?.accountId && currentAccount.value != null) {
                savedStateHandle.setReplyId(result.replyId)
                savedStateHandle.setRenoteId(result.renoteId)
                savedStateHandle.setChannelId(result.channelId)
            }
            if (currentAccount.value != null) {
                savedStateHandle.setVisibility(null)
            }
            currentAccount.value = account
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun setRenoteTo(noteId: Note.Id?) {
        savedStateHandle.setRenoteId(noteId)
        if (noteId == null) {
            return
        }
        viewModelScope.launch {
            noteRepository.find(noteId).onSuccess { note ->
                savedStateHandle.setVisibility(note.visibility)
                savedStateHandle.setChannelId(note.channelId)
            }
        }
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
                val (text, pos) = savedStateHandle.getText()
                    .addMentionUserNames(
                        users.map { it.displayUserName }, 0
                    )
                savedStateHandle.setText(text)
                textCursorPos.tryEmit(TextWithCursorPos(text, pos))
            }
        }
    }

    fun setDraftNoteId(id: Long) {
        viewModelScope.launch {
            draftNoteRepository.findOne(id).mapCancellableCatching {
                val account = accountRepository.get(it.accountId).getOrThrow()
                it.toNoteEditingState().copy(
                    currentAccount = account
                )
            }.onSuccess { note ->
                currentAccount.value = note.currentAccount
                savedStateHandle.applyBy(note)
            }
        }

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
            viewModelScope.launch {
                val reservationPostingAt =
                    savedStateHandle.getNoteEditingUiState(
                        account,
                        visibility.value,
                        driveFileRepository
                    ).sendToState.schedulePostAt
                draftNoteService.save(
                    savedStateHandle.getNoteEditingUiState(
                        account,
                        visibility.value,
                        driveFileRepository
                    )
                        .toCreateNote(account)
                ).mapCancellableCatching { dfNote ->
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
        if (currentAccount.value?.instanceType == Account.InstanceType.MASTODON) {
            return
        }
        when (appFile) {
            is AppFile.Local -> {
                savedStateHandle.setFiles(files.value.toggleFileSensitiveStatus(appFile))
                savedStateHandle[NoteEditorSavedStateKey.PickedFiles.name] =
                    files.value.toggleFileSensitiveStatus(appFile)
            }
            is AppFile.Remote -> {
                viewModelScope.launch {
                    runCancellableCatching {
                        driveFileRepository.toggleNsfw(appFile.id)
                    }
                }
            }
        }

    }

    fun toggleSensitive() {
        val sensitive = savedStateHandle.getSensitive()
        if (currentAccount.value?.instanceType == Account.InstanceType.MASTODON) {
            viewModelScope.launch {
                savedStateHandle.setFiles(
                    savedStateHandle.getFiles().mapNotNull { appFile ->
                        updateSensitiveUseCase(appFile, !sensitive).onFailure {
                            logger.error("ファイルのセンシティブの状態の更新に失敗しました", it)
                        }.getOrNull()
                    }
                )
            }
        }
        savedStateHandle.setSensitive(!sensitive)
    }

    fun updateFileName(appFile: AppFile, name: String) {
        when (appFile) {
            is AppFile.Local -> {
                savedStateHandle.setFiles(files.value.updateFileName(appFile, name))
            }
            is AppFile.Remote -> {
                viewModelScope.launch {
                    runCancellableCatching {
                        val file = driveFileRepository.find(appFile.id)
                        driveFileRepository.update(
                            UpdateFileProperty(
                                fileId = file.id,
                                comment = file.comment,
                                folderId = file.folderId,
                                isSensitive = file.isSensitive,
                                name = name
                            )
                        ).getOrThrow()
                    }.onFailure {
                        logger.error("update file name failed", it)
                    }

                }
            }
        }
    }

    fun updateFileComment(appFile: AppFile, comment: String) {
        when (appFile) {
            is AppFile.Local -> {
                savedStateHandle.setFiles(files.value.updateFileComment(appFile, comment))
            }
            is AppFile.Remote -> {
                viewModelScope.launch {
                    runCancellableCatching {
                        val file = driveFileRepository.find(appFile.id)
                        driveFileRepository.update(
                            UpdateFileProperty(
                                fileId = file.id,
                                comment = comment,
                                folderId = file.folderId,
                                isSensitive = file.isSensitive,
                                name = file.name
                            )
                        ).getOrThrow()
                    }.onFailure {
                        logger.error("update file comment failed", it)
                    }
                }
            }
        }
    }

    fun add(file: AppFile) = viewModelScope.launch {
        val files = files.value.toMutableList()
        files.add(
            file
        )
        savedStateHandle.setFiles(files)
        val account = currentAccount.value ?: return@launch
        val localFile = when (file) {
            is AppFile.Local -> file
            is AppFile.Remote -> return@launch
        }
        val instanceInfo =
            instanceInfoRepository.findByHost(account.getHost()).getOrNull() ?: return@launch
        val maxFileSize = instanceInfo.clientMaxBodyByteSize ?: return@launch

        if (maxFileSize < (localFile.fileSize ?: 0)) {
            _fileSizeInvalidEvent.tryEmit(FileSizeInvalidEvent(file, instanceInfo, account))
        }
    }


    private fun addAllFileProperty(fpList: List<FileProperty>) {
        val files = savedStateHandle.getFiles().toMutableList()
        files.addAll(fpList.map {
            AppFile.Remote(it.id)
        })
        savedStateHandle.setFiles(files)

    }

    fun addFilePropertyFromIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch {
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


    fun enablePoll() {
        val poll =
            if (savedStateHandle.getPoll() == null) PollEditingState(listOf(
                PollChoiceState("", UUID.randomUUID()),
                PollChoiceState("", UUID.randomUUID()),
                PollChoiceState("", UUID.randomUUID())
            ), false) else null
        savedStateHandle.setPoll(poll)
    }

    fun disablePoll() {
        savedStateHandle.setPoll(null)
    }

    fun setText(text: String) {
        savedStateHandle.setText(text)
    }

    fun changeCwEnabled() {
        savedStateHandle.setHasCw(!savedStateHandle.getHasCw())
    }

    fun setCw(text: String?) {
        savedStateHandle.setCw(text)
        focusType = NoteEditorFocusEditTextType.Text
    }

    fun setVisibility(visibility: Visibility) {
        logger.debug("公開範囲がセットされた:$visibility")
        savedStateHandle.setChannelId(null)
        savedStateHandle.setVisibility(visibility)
    }

    fun setChannelId(channelId: Channel.Id?) {
        savedStateHandle.setChannelId(channelId)
        if (channelId == null) {
            return
        } else {
            savedStateHandle.setVisibility(Visibility.Public(true))
        }
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
        when(focusType) {
            NoteEditorFocusEditTextType.Cw -> {
                val builder = StringBuilder(savedStateHandle.getCw() ?: "")
                logger.debug("pos:$pos")
                builder.insert(pos, emoji)
                savedStateHandle.setCw(builder.toString())
                logger.debug("position:${pos + emoji.length - 1}")
                return pos + emoji.length
            }
            NoteEditorFocusEditTextType.Text -> {
                val builder = StringBuilder(savedStateHandle.getText() ?: "")
                builder.insert(pos, emoji)
                savedStateHandle.setText(builder.toString())
                logger.debug("position:${pos + emoji.length - 1}")
                return pos + emoji.length
            }
        }

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
        viewModelScope.launch {
            when (val account = currentAccount.value) {
                null -> Result.failure(UnauthorizedException())
                else -> Result.success(account)
            }.mapCancellableCatching { account ->
                draftNoteService.save(uiState.value.toCreateNote(account)).getOrThrow()
            }.onSuccess { result ->
                isSaveNoteAsDraft.event = result.draftNoteId
            }.onFailure { e ->
                logger.error("下書き保存に失敗した", e)
            }
        }
    }

    fun onPastePostUrl(text: String, start: Int, beforeText: String, count: Int) = viewModelScope.launch {
        val urlText = text.substring(start, start + count)
        val ca = currentAccount.value ?: return@launch
        if (UrlPatternChecker.isMatch(urlText)) {
            apResolverRepository.resolve(ca.accountId, urlText).onSuccess {
                when(it) {
                    is ApResolver.TypeNote -> {
                        setRenoteTo(it.note.id)
                        setText(beforeText)
                    }
                    is ApResolver.TypeUser -> return@launch
                }
            }
        }
    }

    fun canSaveDraft(): Boolean {
        return uiState.value.shouldDiscardingConfirmation()
    }


    fun clear() {
        savedStateHandle.applyBy(NoteEditorUiState())
    }

    private fun setUpUserViewData(userId: User.Id): UserViewData {
        return userViewDataFactory.create(userId, viewModelScope, dispatcher)
    }


}

data class TextWithCursorPos(val text: String?, val cursorPos: Int)
data class FileSizeInvalidEvent(
    val file: AppFile.Local,
    val instanceInfo: InstanceInfo,
    val account: Account
)

enum class NoteEditorFocusEditTextType {
    Cw, Text
}