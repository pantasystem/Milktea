package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import java.io.Serializable
import java.util.*

class PostNoteTask(
    //connectionInstance: ConnectionInstance,
    connectionInformation: EncryptedConnectionInformation,
    encryption: Encryption
    //private val fileUploader: FileUploader
): Serializable{

    enum class Visibility(val visibility: String, val isLocalOnly: Boolean){
        PUBLIC("public", false),
        HOME("home", false),
        FOLLOWERS("followers", false),
        SPECIFIED("specified", false),
        PUBLIC_LOCAL_ONLY("public", true),
        HOME_LOCAL_ONLY("home", true),
        FOLLOWERS_LOCAL_ONLY("followers", true)
    }

    private val i: String = connectionInformation.getI(encryption)!!
    private var visibleUserIds: List<String>? = null
    private var visibility: CreateNote.Visibility? = null
    private var isLocal: Boolean? = null
    var files: List<FileNoteEditorData>? = null
    private var filesIds: List<String>? = null
    var text: String? = null
    var cw: String? = null
    var viaMobile: Boolean? = null
    var localOnly: Boolean? = null
    var noExtractMentions: Boolean? = null
    var noExtractHashtags: Boolean? = null
    var noExtractEmojis: Boolean? = null
    var replyId: String? = null
    var renoteId: String? = null
    var poll: CreatePoll? = null
    //　世界か美優か？いい加減にしろ美優に決まってんだろ！！！
    //  でもその迷いが発せしてしまう純心さと優しさが尊い！！
    fun setVisibility(visibility: Visibility?, visibilityUsers: List<String>? = null){
        visibility?: return
        val isLocalVisibility = visibility == Visibility.FOLLOWERS_LOCAL_ONLY
                || visibility == Visibility.HOME_LOCAL_ONLY
                || visibility == Visibility.PUBLIC_LOCAL_ONLY

        val v = when(visibility){
            Visibility.PUBLIC, Visibility.PUBLIC_LOCAL_ONLY -> CreateNote.Visibility.PUBLIC
            Visibility.HOME, Visibility.HOME_LOCAL_ONLY -> CreateNote.Visibility.HOME
            Visibility.FOLLOWERS, Visibility.FOLLOWERS_LOCAL_ONLY -> CreateNote.Visibility.FOLLOWERS
            Visibility.SPECIFIED -> CreateNote.Visibility.SPECIFIED
        }
        //require(canLocalVisibility == isLocal) { "" }
        this.visibility = v
        this.isLocal = isLocalVisibility
        this.visibleUserIds = visibilityUsers
    }
    
    fun execute(fileUploader: FileUploader): CreateNote?{
         val ok = if(files.isNullOrEmpty()){
             true
         }else{
             executeFileUpload(fileUploader)
        }
        return if(ok){
            CreateNote(
                i = i,
                visibility = visibility?.name?.toLowerCase(Locale.ENGLISH)?: "public",
                visibleUserIds = visibleUserIds,
                text = text,
                cw =cw,
                viaMobile = viaMobile,
                localOnly = localOnly,
                noExtractEmojis = noExtractEmojis,
                noExtractMentions = noExtractMentions,
                noExtractHashtags = noExtractHashtags,
                replyId = replyId,
                renoteId = renoteId,
                poll = poll,
                fileIds = filesIds
                )
        }else{
            null
        }

    }

    private fun executeFileUpload(fileUploader: FileUploader): Boolean{
        val tmpFiles = files
        filesIds = tmpFiles?.mapNotNull {
            if (it.isLocal && it.uploadFile != null) {
                fileUploader.upload(it.uploadFile)?.id
            } else {
                //skip
                it.remoteFileId
            }
        }

        //サイズが合わなければエラー
        return tmpFiles != null && tmpFiles.size == filesIds?.size
    }

}