package net.pantasystem.milktea.note.editor.viewmodel

//import net.pantasystem.milktea.model.instance.InstanceInfoRepository
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.text.UrlPatternChecker
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModelUiStateHelper
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.drive.*
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.CopyFileToAppDirUseCase
import net.pantasystem.milktea.model.file.UpdateAppFileSensitiveUseCase
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.instance.InstanceInfo
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.note.*
import net.pantasystem.milktea.model.note.draft.DraftNoteRepository
import net.pantasystem.milktea.model.note.draft.DraftNoteService
import net.pantasystem.milktea.model.note.reservation.NoteReservationPostExecutor
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    private val accountStore: AccountStore,
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
    localConfigRepository: LocalConfigRepository,
    private val featureEnables: FeatureEnables,
    private val noteRelationGetter: NoteRelationGetter,
//    private val instanceInfoRepository: InstanceInfoRepository,
    private val updateSensitiveUseCase: UpdateAppFileSensitiveUseCase,
    private val apResolverRepository: ApResolverRepository,
    userRepository: UserRepository,
    private val copyFileToAppDirUseCase: CopyFileToAppDirUseCase,
    buildNoteEditorUiState: NoteEditorUiStateBuilder,
    buildNoteEditorSendToStateBuilder: NoteEditorSendToStateBuilder,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    private val logger = loggerFactory.create("NoteEditorViewModel")

    private val _currentAccount = MutableStateFlow<Account?>(null)
    val currentAccount = _currentAccount.asStateFlow()

    val text = savedStateHandle.getStateFlow<String?>(NoteEditorSavedStateKey.Text.name, null)

    val textCursorPos = MutableSharedFlow<TextWithCursorPos>(extraBufferCapacity = 10)

    val cw = savedStateHandle.getStateFlow<String?>(NoteEditorSavedStateKey.Cw.name, null)
    val hasCw = savedStateHandle.getStateFlow(NoteEditorSavedStateKey.HasCW.name, false)

    val files = savedStateHandle.getStateFlow<List<AppFile>>(
        NoteEditorSavedStateKey.PickedFiles.name,
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val instanceInfoType = _currentAccount.filterNotNull().flatMapLatest {
        instanceInfoService.observe(it.normalizedInstanceUri)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isSensitiveMedia =
        savedStateHandle.getStateFlow<Boolean?>(NoteEditorSavedStateKey.IsSensitive.name, null)

    private val filePreviewSources = NoteEditorFilePreviewSourcesMapper(
        filePropertyDataSource,
        driveFileRepository,
        logger,
        viewModelScope
    ).create(files)

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
        _currentAccount.filterNotNull().flatMapLatest { account ->
            instanceInfoService.observe(account.normalizedInstanceUri).filterNotNull()
                .map { meta ->
                    meta.maxNoteTextLength
                }
        }.stateIn(
            viewModelScope + Dispatchers.IO,
            started = SharingStarted.Lazily,
            initialValue = 1500
        )


    val enableFeatures = _currentAccount.filterNotNull().map {
        featureEnables.enableFeatures(it.normalizedInstanceUri)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val maxFileCount = _currentAccount.filterNotNull().mapNotNull {
        instanceInfoService.find(it.normalizedInstanceUri).getOrNull()?.maxFileCount
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Eagerly, initialValue = 4)

    private val _visibility = savedStateHandle.getStateFlow<Visibility?>(
        NoteEditorSavedStateKey.Visibility.name,
        null
    )

    val visibility = NoteEditorVisibilityCombiner(
        viewModelScope,
        localConfigRepository,
    ).create(
        _visibility,
        _currentAccount,
        channelId,
    )

    private val reactionAcceptanceType = savedStateHandle.getStateFlow<String?>(
        NoteEditorSavedStateKey.ReactionAcceptance.name,
        null
    ).map { strType ->
        ReactionAcceptanceType.values().find {
            it.name == strType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val reservationPostingAt =
        savedStateHandle.getStateFlow<Date?>(NoteEditorSavedStateKey.ScheduleAt.name, null)

    val poll = savedStateHandle.getStateFlow<PollEditingState?>(
        NoteEditorSavedStateKey.Poll.name,
        null
    )

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


    @OptIn(ExperimentalCoroutinesApi::class)
    val channels = _currentAccount.filterNotNull().flatMapLatest {
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
        initialValue = ResultState.initialState()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val user = currentAccount.filterNotNull().flatMapLatest { account ->
        userRepository.observe(User.Id(account.accountId, account.remoteId)).map {
            it.castAndPartiallyFill()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private val draftNoteId =
        savedStateHandle.getStateFlow<Long?>(NoteEditorSavedStateKey.DraftNoteId.name, null)

    private val _isPosting = MutableStateFlow(false)
    val isPosting = _isPosting.asStateFlow()

    private val noteEditorSendToState = buildNoteEditorSendToStateBuilder(
        visibilityFlow = _visibility,
        currentAccountFlow = _currentAccount,
        channelIdFlow = channelId,
        replyIdFlow = replyId,
        renoteIdFlow = renoteId,
        reservationPostingAtFlow = reservationPostingAt,
        draftNoteIdFlow = draftNoteId,
        reactionAcceptanceType = reactionAcceptanceType,
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NoteEditorSendToState()
    )

    val uiState = buildNoteEditorUiState(
        textFlow = text,
        cwFlow = cw,
        hasCwFlow = hasCw,
        isSensitiveMediaFlow = isSensitiveMedia,
        filePreviewSourcesFlow = filePreviewSources,
        pollFlow = poll,
        currentAccountFlow = _currentAccount,
        noteEditorSendToStateFlow = noteEditorSendToState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteEditorUiState())

    val textRemaining = combine(maxTextLength, uiState) { max, uiState ->
        val t = uiState.formState.text
        max - (t?.codePointCount(0, t.length) ?: 0)
    }.catch {
        logger.error("observe meta error", it)
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    val isPostAvailable = uiState.map {
        it.checkValidate(textMaxLength = maxTextLength.value, maxFileCount = maxFileCount.value)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    private val cache = planeNoteViewDataCacheFactory.create({
        requireNotNull(_currentAccount.value)
    }, viewModelScope)

    val replyTo = replyId.map { id ->
        noteRelationGetter.get(id ?: throw UnauthorizedException()).getOrThrow()?.let {
            cache.get(it)
        }
    }.catch {
        emit(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val accountUiState = AccountViewModelUiStateHelper(
        _currentAccount,
        accountStore,
        userRepository,
        instanceInfoService,
        viewModelScope,
    ).uiState

    private val _fileSizeInvalidEvent =
        MutableSharedFlow<FileSizeInvalidEvent>(extraBufferCapacity = 10)
    val fileSizeInvalidEvent = _fileSizeInvalidEvent.asSharedFlow()

    private val _isPost = MutableSharedFlow<Boolean>(extraBufferCapacity = 10)
    val isPost = _isPost.asSharedFlow()

    private val _showPollDatePicker = MutableSharedFlow<Unit>(extraBufferCapacity = 10)
    val showPollDatePicker = _showPollDatePicker.asSharedFlow()

    private val _showPollTimePicker = MutableSharedFlow<Unit>(extraBufferCapacity = 10)
    val showPollTimePicker = _showPollTimePicker.asSharedFlow()

    private val _isSaveNoteAsDraft = MutableSharedFlow<Long?>(extraBufferCapacity = 10)
    val isSaveNoteAsDraft = _isSaveNoteAsDraft.asSharedFlow()

    var focusType: NoteEditorFocusEditTextType = NoteEditorFocusEditTextType.Text


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
                _currentAccount.value = note.currentAccount
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
        _currentAccount.value?.let { account ->
            viewModelScope.launch {
                if (_isPosting.value) {
                    return@launch
                }
                _isPosting.value = true
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
                    _isPost.tryEmit(true)
                }.onFailure {
                    logger.error("登録失敗", it)
                }
                _isPosting.value = false
            }
        }

    }

    fun toggleNsfw(appFile: AppFile) {
        if (!enableFeatures.value.contains(FeatureType.Drive)) {
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
        if (_currentAccount.value?.instanceType == Account.InstanceType.MASTODON) {
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
                    driveFileRepository.update(
                        UpdateFileProperty(
                            fileId = appFile.id,
                            name = ValueType.Some(name)
                        )
                    ).onFailure {
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
                    driveFileRepository.update(
                        UpdateFileProperty(
                            fileId = appFile.id,
                            comment = ValueType.Some(comment),
                        )
                    ).onFailure {
                        logger.error("update file comment failed", it)
                    }
                }
            }
        }
    }

    fun addFile(uri: Uri) = viewModelScope.launch {
        copyFileToAppDirUseCase(uri).onSuccess { file ->
            val files = files.value.toMutableList()
            files.add(
                file
            )
            savedStateHandle.setFiles(files)
        }.onFailure {
            logger.error("ファイルのコピーに失敗しました", it)
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

    fun removeFileNoteEditorData(file: AppFile) = viewModelScope.launch {
        savedStateHandle.setFiles(
            savedStateHandle.getFiles().removeFile(file)
        )
    }


    fun fileTotal(): Int {
        return files.value.size
    }


    fun enablePoll() {
        val poll =
            if (savedStateHandle.getPoll() == null) PollEditingState.EMPTY_POLL_EDITING_STATE else null
        savedStateHandle.setPoll(poll)
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
        when (focusType) {
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
            when (val account = _currentAccount.value) {
                null -> Result.failure(UnauthorizedException())
                else -> Result.success(account)
            }.mapCancellableCatching { account ->
                draftNoteService.save(uiState.value.toCreateNote(account)).getOrThrow()
            }.onSuccess { result ->
                _isSaveNoteAsDraft.tryEmit(result.draftNoteId)
            }.onFailure { e ->
                logger.error("下書き保存に失敗した", e)
            }
        }
    }

    fun onPastePostUrl(text: String, start: Int, beforeText: String, count: Int) =
        viewModelScope.launch {
            val urlText = text.substring(start, start + count)
            val ca = _currentAccount.value ?: return@launch
            val canQuote = canQuote()

            if (!canQuote) {
                return@launch
            }

            if (UrlPatternChecker.isMatch(urlText)) {
                apResolverRepository.resolve(ca.accountId, urlText).onSuccess {
                    when (it) {
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

    suspend fun canQuote(): Boolean {
        val ca = _currentAccount.value ?: return false
        return instanceInfoService.find(ca.normalizedInstanceUri).map {
            it.canQuote
        }.getOrElse { false }
    }

    fun setAccountId(accountId: Long?) {
        viewModelScope.launch {
            (accountId?.let {
                accountRepository.get(accountId)
            } ?: accountRepository.getCurrentAccount()).onSuccess {
                setAccount(it)
            }.onFailure {
                logger.error("アカウントの取得に失敗した", it)
            }
        }
    }

    fun setAccountIdAndSwitchCurrentAccount(accountId: Long?) {
        viewModelScope.launch {
            (accountId?.let {
                accountRepository.get(accountId)
            } ?: accountRepository.getCurrentAccount()).onSuccess {
                setAccount(it)
                accountStore.setCurrent(it)
            }.onFailure {
                logger.error("アカウントの取得に失敗した", it)
            }
        }
    }

    fun onReactionAcceptanceSelected(type: ReactionAcceptanceType?) {
        savedStateHandle.setReactionAcceptanceType(type)
    }

    fun onExpireAtChangeDateButtonClicked() {
        _showPollDatePicker.tryEmit(Unit)
    }
    fun onExpireAtChangeTimeButtonClicked(){
        _showPollTimePicker.tryEmit(Unit)
    }
    private fun setUpUserViewData(userId: User.Id): UserViewData {
        return userViewDataFactory.create(userId, viewModelScope, dispatcher)
    }

    private suspend fun setAccount(account: Account) = runCancellableCatching<Unit> {
        val result = noteEditorSwitchAccountExecutor(
            _currentAccount.value,
            uiState.value.sendToState,
            account,
        )

        if (account.accountId != _currentAccount.value?.accountId && _currentAccount.value != null) {
            savedStateHandle.setReplyId(result.replyId)
            savedStateHandle.setRenoteId(result.renoteId)
            savedStateHandle.setChannelId(result.channelId)
        }
        if (_currentAccount.value != null) {
            savedStateHandle.setVisibility(null)
        }
        logger.debug {
            "currentAccount:${account.userName}@${account.getHost()}"
        }
        _currentAccount.value = account
    }

}

data class TextWithCursorPos(val text: String?, val cursorPos: Int)
data class FileSizeInvalidEvent(
    val file: AppFile.Local,
    val instanceInfo: InstanceInfo,
    val account: Account,
)

enum class NoteEditorFocusEditTextType {
    Cw, Text
}