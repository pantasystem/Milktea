package net.pantasystem.milktea.note.viewmodel


import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android_ui.getTextType
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.file.AboutMediaType
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.url.UrlPreview
import net.pantasystem.milktea.model.url.UrlPreviewLoadTask
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.media.viewmodel.MediaViewData
import net.pantasystem.milktea.note.reaction.ReactionViewData

open class PlaneNoteViewData(
    val note: NoteRelation,
    val account: Account,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val noteTranslationStore: NoteTranslationStore,
    private val instanceEmojis: List<Emoji>,
    noteDataSource: NoteDataSource,
    configRepository: LocalConfigRepository,
    emojiRepository: CustomEmojiRepository,
    coroutineScope: CoroutineScope,
) : NoteViewData {

    val id = note.note.id

    var filterResult: FilterResult = FilterResult.NotExecuted

    val toShowNote: NoteRelation
        get() {
            return if (note.note.isRenote() && !note.note.hasContent()) {
                note.renote ?: note
            } else {
                note
            }
        }

    val currentNote: LiveData<Note> = noteDataSource.observeOne(toShowNote.note.id).map {
        it ?: toShowNote.note
    }.onStart {
        emit(toShowNote.note)
    }.asLiveData(coroutineScope.coroutineContext)

    val isRenotedByMe = !note.note.hasContent() && note.user.id.id == account.remoteId


    val userId: User.Id
        get() = toShowNote.user.id

    val name: String
        get() = toShowNote.user.displayName

    val userName: String = toShowNote.user.displayUserName

    val avatarUrl = toShowNote.user.avatarUrl

    val cw = toShowNote.note.cw
    val cwNode = MFMParser.parse(
        toShowNote.note.cw, (toShowNote.note.emojis ?: emptyList()) + instanceEmojis,
        userHost = toShowNote.user
            .host,
        accountHost = account.getHost()
    )

    //true　折り畳み
    val text = toShowNote.note.text

    val contentFolding = MutableLiveData(cw != null)
    val contentFoldingStatusMessage: LiveData<StringSource> = Transformations.map(contentFolding) {
        CwTextGenerator(toShowNote, it)
    }


    val textNode = getTextType(account, toShowNote, instanceEmojis)

    val translateState: LiveData<ResultState<Translation?>?> =
        this.noteTranslationStore.state(toShowNote.note.id).asLiveData()

    var emojis = toShowNote.note.emojis ?: emptyList()

    val emojiMap = HashMap<String, Emoji>(toShowNote.note.emojis?.associate {
        it.name to it
    } ?: mapOf())

    private val previewableFiles = toShowNote.files?.map {
        FilePreviewSource.Remote(AppFile.Remote(it.id), it)
    }?.filter {
        it.aboutMediaType == AboutMediaType.IMAGE || it.aboutMediaType == AboutMediaType.VIDEO
    } ?: emptyList()
    val media = MediaViewData(previewableFiles, configRepository.get().getOrNull())

    val isOnlyVisibleRenoteStatusMessage = MutableLiveData<Boolean>(false)


    val urlPreviewList = MutableLiveData<List<UrlPreview>>()

    val previews = MediatorLiveData<List<Preview>>().apply {
        val otherFiles = toShowNote.files?.map { file ->
            FilePreviewSource.Remote(AppFile.Remote(file.id), file)
        }?.filterNot { fp ->
            fp.aboutMediaType == AboutMediaType.IMAGE || fp.aboutMediaType == AboutMediaType.VIDEO
        }?.map { file ->
            Preview.FileWrapper(file)
        }

        postValue(otherFiles)
        this.addSource(urlPreviewList) {
            val urlPreviews = it?.map { url ->
                Preview.UrlWrapper(url)
            } ?: emptyList()
            postValue((otherFiles ?: emptyList()) + urlPreviews)

        }
    }

    //var replyCount: String? = if(toShowNote.replyCount > 0) toShowNote.replyCount.toString() else null
    val replyCount = MutableLiveData(toShowNote.note.repliesCount)


    val renoteCount: LiveData<Int> = Transformations.map(currentNote) {
        it?.renoteCount ?: 0
    }

    val favoriteCount = Transformations.map(currentNote) {
        (it.type as? Note.Type.Mastodon?)?.favoriteCount
    }

    val canRenote = Transformations.map(currentNote) {
        it.canRenote(User.Id(accountId = account.accountId, id = account.remoteId))
    }

    val reactionCountsExpanded = MutableLiveData(toShowNote.note.reactionCounts.size <= Note.SHORT_REACTION_COUNT_MAX_SIZE)

    val reactionCountsViewData: LiveData<List<ReactionViewData>> = currentNote.switchMap { n ->
        reactionCountsExpanded.map {
            val reactions = if (it == true) {
                n.reactionCounts
            } else {
                n.getShortReactionCounts(note.note.isRenoteOnly())
            }
            ReactionViewData.from(reactions, n, emojiRepository.getAndConvertToMap(account.getHost()))
        }
    }

    val reactionCount = currentNote.map { note ->
        note.reactionCounts.sumOf {
            it.count
        }
    }

    val myReaction: LiveData<String?> = Transformations.map(currentNote) {
        it.myReaction
    }

    val poll = MutableLiveData<Poll?>(toShowNote.note.poll)

    //reNote先
    val subNote: NoteRelation? = toShowNote.renote

    val subNoteAvatarUrl = subNote?.user?.avatarUrl
    val subNoteTextNode = subNote?.let {
        getTextType(account, it, instanceEmojis)
    }

    val subCw = subNote?.note?.cw
    val subCwNode = MFMParser.parse(
        subNote?.note?.cw,
        (subNote?.note?.emojis ?: emptyList()) + instanceEmojis,
        accountHost = account.getHost(),
        userHost = subNote?.user?.host
    )

    //true　折り畳み
    val subContentFolding = MutableLiveData(subCw != null)

    val subContentFoldingStatusMessage = Transformations.map(subContentFolding) { isFolding ->
        CwTextGenerator(subNote, isFolding)
    }
    val subNoteFiles = subNote?.files ?: emptyList()
    val subNoteMedia = MediaViewData(subNote?.files?.map {
        FilePreviewSource.Remote(AppFile.Remote(it.id), it)
    } ?: emptyList(), configRepository.get().getOrNull())

    val channelInfo: LiveData<Note.Type.Misskey.SimpleChannelInfo?> = currentNote.map {
        (it.type as? Note.Type.Misskey)?.channel
    }

    val isVisibleNoteDivider = configRepository.observe().map {
        it.isEnableNoteDivider
    }.distinctUntilChanged().asLiveData(coroutineScope.coroutineContext)

    val isVisibleInstanceTicker = configRepository.observe().map {
        it.isEnableInstanceTicker
    }.distinctUntilChanged().asLiveData(coroutineScope.coroutineContext)

    val isUserNameDefault = configRepository.observe().map {
        it.isUserNameDefault
    }.distinctUntilChanged().asLiveData(coroutineScope.coroutineContext)

    val isVisibleSubNoteMediaPreview = subContentFolding.map { folding ->
        !(folding || subNoteFiles.isEmpty() || subNoteMedia.isOver4Files)
    }

    fun changeContentFolding() {
        val isFolding = contentFolding.value ?: return
        contentFolding.value = !isFolding
    }

    fun changeSubContentFolding() {
        val isFolding = subContentFolding.value ?: return
        subContentFolding.value = !isFolding
    }


    val urlPreviewLoadTaskCallback = object : UrlPreviewLoadTask.Callback {
        override fun accept(list: List<UrlPreview>) {
            urlPreviewList.postValue(list)
        }
    }

    fun update(note: Note) {
        require(toShowNote.note.id == note.id) {
            "更新として渡されたNote.Idと現在のIdが一致しません。"
        }
        emojiMap.putAll(note.emojis?.map {
            it.name to it
        } ?: emptyList())
        emojis = emojiMap.values.toList() + instanceEmojis
        note.poll?.let {
            poll.postValue(it)
        }
    }

    fun capture(job: (Flow<NoteDataSource.Event>) -> Job) {
        val flow = noteCaptureAPIAdapter.capture(toShowNote.note.id).onEach {
            if (it is NoteDataSource.Event.Updated) {
                update(it.note)
            }
        }.catch { e ->
            Log.d("PlaneNoteViewData", "error", e)
        }
        this.job = job(flow)
    }

    var job: Job? = null

    // NOTE: (Panta) cwの時点で大半が隠されるので折りたたむ必要はない
    // NOTE: (Panta) cwを折りたたんでしまうとcw展開後に自動的に折りたたまれてしまって二度手間になる可能性がある。
    val expanded = MutableLiveData<Boolean>(cw != null)


    init {
        require(toShowNote.note.id != subNote?.note?.id)
    }

    fun expand() {
        Log.d("PlaneNoteViewData", "expand")
        expanded.value = true
    }

    fun expandReactions() {
        reactionCountsExpanded.value = true
    }

    enum class FilterResult {
        NotExecuted,
        ShouldFilterNote,
        Pass,
    }
}

