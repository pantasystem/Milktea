package jp.panta.misskeyandroidclient.viewmodel.notes


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.url.UrlPreview
import jp.panta.misskeyandroidclient.viewmodel.notes.media.MediaViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.poll.PollViewData
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask

open class PlaneNoteViewData (
    val note: Note,
    val account: Account,
    var determineTextLength: DetermineTextLength
) : NoteViewData{


    val id = note.id

    override fun getRequestId(): String {
        return id
    }

    val toShowNote: Note
        get() {
            return if(note.reNoteId != null && note.text == null && note.files.isNullOrEmpty()){
                note.reNote?: note
            }else{
                note
            }
        }

    val isMyNote = account.remoteId == toShowNote.user.id

    val isRenotedByMe = (note.reNoteId != null && note.text == null && note.files.isNullOrEmpty()) && note.user.id == account.remoteId

    val statusMessage: String?
        get(){
            if(note.reply != null){
                //reply
                return "${note.user.getDisplayUserName()}が返信しました"
            }else if(note.reNoteId == null && (note.text != null || note.files != null)){
                //Note
                return null
            }else if(note.reNoteId != null && note.text == null && note.files.isNullOrEmpty()){
                //reNote
                return "${note.user.getDisplayUserName()}がリノートしました"

            }else if(note.reNoteId != null && (note.text != null || note.files != null)){
                //quote
                //"${note.user.name}が引用リノートしました"
                return null
            }else{
                return null
            }
        }

    val userId: String
        get() = toShowNote.user.id

    val name: String
        get() = toShowNote.user.name?: toShowNote.user.userName

    val userName: String
        get() = if(toShowNote.user.host == null){
            "@" + toShowNote.user.userName
        }else{
            "@" + toShowNote.user.userName + "@" + toShowNote.user.host
        }

    val avatarUrl = toShowNote.user.avatarUrl

    val cw = toShowNote.cw
    val cwNode = MFMParser.parse(toShowNote.cw, toShowNote.emojis)

    //true　折り畳み
    val text = toShowNote.text.apply{
        determineTextLength.setText(this)
    }

    val contentFolding = MutableLiveData<Boolean>(cw != null || determineTextLength.isLong())
    val contentFoldingStatusMessage: LiveData<String> = Transformations.map(contentFolding){
        if(it) "もっと見る: ${text?.length}文字" else "隠す"
    }


    val textNode = MFMParser.parse(toShowNote.text, toShowNote.emojis)
    val urls = textNode?.getUrls()

    val emojis = toShowNote.emojis

    val emojiMap = HashMap<String, Emoji>(toShowNote.emojis?.map{
        it.name to it
    }?.toMap()?: mapOf())

    val files = toShowNote.files?: emptyList()
    private val previewableFiles = toShowNote.files?.filter{
        it.type?.startsWith("image") == true || it.type?.startsWith("video") == true
    }?: emptyList()
    val media = MediaViewData(previewableFiles)


    val urlPreviewList = MutableLiveData<List<UrlPreview>>()

    val previews = MediatorLiveData<List<Preview>>().apply{
        addSource(urlPreviewList){
            val otherFiles: List<Preview> = toShowNote.files?.filterNot{ fp ->
                fp.type?.startsWith("image") == true || fp.type?.startsWith("video") == true
            }?.map{ fp ->
                Preview.FileWrapper(fp)
            }?: emptyList()
            val list = ArrayList(otherFiles)
            val urlPreviews = it?.map{ url ->
                Preview.UrlWrapper(url)
            }?: emptyList()
            list.addAll(urlPreviews)
            postValue(list)

        }
    }

    //var replyCount: String? = if(toShowNote.replyCount > 0) toShowNote.replyCount.toString() else null
    val replyCount = MutableLiveData<Int>(toShowNote.replyCount)

    val reNoteCount: String?
        get() = if(toShowNote.reNoteCount > 0) toShowNote.reNoteCount.toString() else null
    val renoteCount = MutableLiveData<Int>(toShowNote.reNoteCount)

    val reactionCounts = MutableLiveData<LinkedHashMap<String, Int>>(toShowNote.reactionCounts)

    val reactionCount = Transformations.map(reactionCounts){
        var sum = 0
        it?.forEach{ map ->
            sum += map.value
        }
        return@map sum
    }

    val myReaction = MutableLiveData<String>(toShowNote.myReaction)

    val poll = if(toShowNote.poll == null) null else PollViewData(toShowNote.poll!!, toShowNote.id)

    //reNote先
    val subNote: Note? = toShowNote.reNote

    val subNoteUserName = subNote?.user?.userName
    val subNoteName = subNote?.user?.name
    val subNoteAvatarUrl = subNote?.user?.avatarUrl
    val subNoteText = subNote?.text
    val subNoteTextNode = MFMParser.parse(subNote?.text, subNote?.emojis)
    val subNoteEmojis = subNote?.emojis

    val subCw = subNote?.cw
    val subCwNode = MFMParser.parse(subNote?.cw, subNote?.emojis)
    //true　折り畳み
    val subContentFolding = MutableLiveData<Boolean>( subCw != null )
    val subContentFoldingStatusMessage = Transformations.map(subContentFolding){
        if(it) "もっと見る: ${subNoteText?.length}" else "閉じる"
    }
    val subNoteFiles = subNote?.files?: emptyList()
    val subNoteMedia =
        MediaViewData(subNoteFiles)

    fun addReaction(reaction: String, emoji: Emoji?, isMyReaction: Boolean = false){
        val reactions = reactionCounts.value?: LinkedHashMap()
        val existingReactionCount = reactions[reaction]
        if(existingReactionCount == null){
            reactions[reaction] = 1
        }else{
            reactions[reaction] = existingReactionCount + 1
        }

        if(emoji != null){
            emojiMap[emoji.name] = emoji
        }

        if(isMyReaction){
            myReaction.postValue(reaction)
            Log.d("PlaneNoteViewData", "リアクションをしました:${reactions[reaction]}, $reaction")
        }
        reactionCounts.postValue(LinkedHashMap(reactions))

    }

    fun takeReaction(reaction: String, isMyReaction: Boolean = false){
        val reactions = reactionCounts.value
            ?: return

        val count = reactions[reaction]

        if(count == null || count < 1){
            return
        }else{
            reactions[reaction] = count - 1
        }

        reactionCounts.postValue(LinkedHashMap(reactions))
        if(isMyReaction){
            myReaction.postValue(null)
            //Log.d("PlaneNoteViewData", "リアクションを解除しました")
        }

    }
    fun changeContentFolding(){
        val isFolding = contentFolding.value?: return
        contentFolding.value = !isFolding
    }

    fun changeSubContentFolding(){
        val isFolding = subContentFolding.value?: return
        subContentFolding.value = !isFolding
    }

    fun setUrlPreviews(list: List<UrlPreview>){
        urlPreviewList.postValue(list)
    }

    val urlPreviewLoadTaskCallback = object : UrlPreviewLoadTask.Callback{
        override fun accept(list: List<UrlPreview>) {
            urlPreviewList.postValue(list)
        }
    }

    fun update(note: Note){
        emojiMap.clear()
        emojiMap.putAll(note.emojis?.map{
            it.name to it
        }?: emptyList())
        renoteCount.postValue(note.reNoteCount)

        myReaction.postValue(note.myReaction)
        reactionCounts.postValue(note.reactionCounts)
    }

}