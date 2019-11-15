package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.view.notes.editor.FileNoteEditorData
import java.io.Serializable
import java.util.*

class CreateCreateNoteTask(
    private val connectionInstance: ConnectionInstance,
    private val fileUploader: FileUploader
): Serializable{
    private val i: String = connectionInstance.getI()!!
    private var visibleUsers: List<String>? = null
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
    fun setVisibility(visibility: CreateNote.Visibility, isLocal: Boolean, visibilityUsers: List<String>? = null){
        val canLocalVisibility = (visibility == CreateNote.Visibility.PUBLIC
                || visibility == CreateNote.Visibility.FOLLOWERS
                || visibility == CreateNote.Visibility.HOME)

        //require(canLocalVisibility == isLocal) { "" }
        require(isLocal == canLocalVisibility) { "localはpublic,followers,homeのみで使用できます" }
        this.visibility = visibility
        this.isLocal = isLocal
        this.visibleUsers = visibilityUsers
    }
    
    fun execute(): CreateNote?{
         val ok = if(files.isNullOrEmpty()){
            executeFileUpload(fileUploader)
        }else{
           true
        }
        return if(ok){
            CreateNote(
                i = i,
                visibility = visibility?.name?.toLowerCase(Locale.ENGLISH)?: "public",
                visibleUserIds = visibleUsers,
                text = text,
                cw =cw,
                viaMobile = viaMobile,
                localOnly = localOnly,
                noExtractEmojis = noExtractEmojis,
                noExtractMentions = noExtractMentions,
                noExtractHashtags = noExtractHashtags,
                replyId = replyId,
                renoteId = renoteId,
                poll = poll
                )
        }else{
            null
        }

    }

    private fun executeFileUpload(fileUploader: FileUploader): Boolean{
        val tmpFiles = files
        val exUploadAndUploaded = tmpFiles?.mapNotNull {
            if (it.isLocal && it.uploadFile != null) {
                fileUploader.upload(it.uploadFile)
            } else {
                //skip
                it.fileProperty
            }
        }
        filesIds = exUploadAndUploaded?.map{
            it.id
        }
        //サイズが合わなければエラー
        return tmpFiles != null && tmpFiles.size == exUploadAndUploaded?.size
    }

}