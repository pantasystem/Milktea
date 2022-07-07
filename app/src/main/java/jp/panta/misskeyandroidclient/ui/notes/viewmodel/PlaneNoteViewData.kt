package jp.panta.misskeyandroidclient.ui.notes.viewmodel


import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.MediaViewData
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.data.infrastructure.url.UrlPreview
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.file.File
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.user.User

open class PlaneNoteViewData(
    val note: NoteRelation,
    val account: Account,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val noteTranslationStore: NoteTranslationStore
) : NoteViewData {


    val id = note.note.id

    override fun getRequestId(): String {
        return id.noteId
    }

    val toShowNote: NoteRelation
        get() {
            return if (note.note.isRenote() && !note.note.hasContent()) {
                note.renote ?: note
            } else {
                note
            }
        }

    val isMyNote = account.remoteId == toShowNote.user.id.id

    val isRenotedByMe = !note.note.hasContent() && note.user.id.id == account.remoteId

    val statusMessage: String?
        get() {
            if (note.reply != null) {
                //reply
                return "${note.user.displayUserName}が返信しました"
            } else if (note.note.renoteId == null && (note.note.text != null || note.files != null)) {
                //Note
                return null
            } else if (note.note.renoteId != null && note.note.text == null && note.files.isNullOrEmpty()) {
                //reNote
                return "${note.user.displayUserName}がリノートしました"

            } else if (note.note.renoteId != null && (note.note.text != null || note.files != null)) {
                //quote
                //"${note.user.name}が引用リノートしました"
                return null
            } else {
                return null
            }
        }

    val userId: User.Id
        get() = toShowNote.user.id

    val name: String
        get() = toShowNote.user.displayName

    val userName: String = toShowNote.user.displayUserName

    val avatarUrl = toShowNote.user.avatarUrl

    val cw = toShowNote.note.cw
    val cwNode = MFMParser.parse(toShowNote.note.cw, toShowNote.note.emojis)

    //true　折り畳み
    val text = toShowNote.note.text

    val contentFolding = MutableLiveData(cw != null)
    val contentFoldingStatusMessage: LiveData<String> = Transformations.map(contentFolding) {
        if (it) "もっと見る: ${text?.length}文字" else "隠す"
    }


    val textNode = MFMParser.parse(toShowNote.note.text, toShowNote.note.emojis)
    val urls = textNode?.getUrls()


    val translateState: LiveData<ResultState<Translation?>?> =
        this.noteTranslationStore.state(toShowNote.note.id).asLiveData()

    var emojis = toShowNote.note.emojis ?: emptyList()

    val emojiMap = HashMap<String, Emoji>(toShowNote.note.emojis?.associate {
        it.name to it
    } ?: mapOf())

    val files = toShowNote.files?.map { fileProperty ->
        fileProperty.toFile()
    }
    private val previewableFiles = files?.filter {
        it.aboutMediaType == File.AboutMediaType.IMAGE || it.aboutMediaType == File.AboutMediaType.VIDEO
    } ?: emptyList()
    val media = MediaViewData(previewableFiles)


    val urlPreviewList = MutableLiveData<List<UrlPreview>>()

    val previews = MediatorLiveData<List<Preview>>().apply {
        val otherFiles = getNotMediaFiles().map { file ->
            Preview.FileWrapper(file)
        }

        postValue(otherFiles)

        this.addSource(urlPreviewList) {

            val list: ArrayList<Preview> = ArrayList(otherFiles)
            val urlPreviews = it?.map { url ->
                Preview.UrlWrapper(url)
            } ?: emptyList()
            list.addAll(urlPreviews)
            postValue(list)

        }
    }

    //var replyCount: String? = if(toShowNote.replyCount > 0) toShowNote.replyCount.toString() else null
    val replyCount = MutableLiveData(toShowNote.note.repliesCount)

    val reNoteCount: String?
        get() = if (toShowNote.note.renoteCount > 0) toShowNote.note.renoteCount.toString() else null
    val renoteCount = MutableLiveData(toShowNote.note.renoteCount)

    val canRenote =
        toShowNote.note.canRenote(User.Id(accountId = account.accountId, id = account.remoteId))

    val reactionCounts = MutableLiveData(toShowNote.note.reactionCounts)

    val reactionCount = Transformations.map(reactionCounts) {
        var sum = 0
        it?.forEach { count ->
            sum += count.count
        }
        return@map sum
    }

    val myReaction = MutableLiveData<String?>(toShowNote.note.myReaction)

    val poll = MutableLiveData<Poll?>(toShowNote.note.poll)

    //reNote先
    val subNote: NoteRelation? = toShowNote.renote

    val subNoteAvatarUrl = subNote?.user?.avatarUrl
    private val subNoteText = subNote?.note?.text
    val subNoteTextNode = MFMParser.parse(subNote?.note?.text, subNote?.note?.emojis)

    val subCw = subNote?.note?.cw
    val subCwNode = MFMParser.parse(subNote?.note?.cw, subNote?.note?.emojis)

    //true　折り畳み
    val subContentFolding = MutableLiveData(subCw != null)
    val subContentFoldingStatusMessage = Transformations.map(subContentFolding) {
        if (it) "もっと見る: ${subNoteText?.length}" else "閉じる"
    }
    val subNoteFiles = subNote?.files?.map {
        it.toFile()
    } ?: emptyList()
    val subNoteMedia = MediaViewData(subNoteFiles)


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
        emojis = emojiMap.values.toList()
        renoteCount.postValue(note.renoteCount)

        myReaction.postValue(note.myReaction)
        reactionCounts.postValue(note.reactionCounts)
        note.poll?.let {
            poll.postValue(it)
        }
    }

    private fun getNotMediaFiles(): List<File> {
        return files?.filterNot { fp ->
            fp.aboutMediaType == File.AboutMediaType.IMAGE || fp.aboutMediaType == File.AboutMediaType.VIDEO
        } ?: emptyList()
    }

    val eventFlow = noteCaptureAPIAdapter.capture(toShowNote.note.id).onEach {
        if (it is NoteDataSource.Event.Updated) {
            update(it.note)
        }
    }.catch { e ->
        Log.d("PlaneNoteViewData", "error", e)
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

}