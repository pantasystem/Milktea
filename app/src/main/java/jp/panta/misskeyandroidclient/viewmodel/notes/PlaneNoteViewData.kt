package jp.panta.misskeyandroidclient.viewmodel.notes

import android.arch.lifecycle.MediatorLiveData
import android.databinding.ObservableField
import jp.panta.misskeyandroidclient.model.notes.Note

class PlaneNoteViewData (private val note: Note){

    val id = note.id

    val toShowNote: Note
        get() {
            return if(note.reNoteId != null && note.text == null && note.files.isNullOrEmpty()){
                note.reNote?: note
            }else{
                note
            }
        }

    val statusMessage: String?
        get(){
            if(note.reply != null){
                //reply
                return "${toShowNote.user.name}が返信しました"
            }else if(note.reNoteId == null && (note.text != null || note.files != null)){
                //Note
                return null
            }else if(note.reNoteId != null && note.text == null && note.files.isNullOrEmpty()){
                //reNote
                return "${note.user.name}がリノートしました"

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

    val text = toShowNote.text

    val emojis = toShowNote.emojis

    val files = toShowNote.files

    var replyCount: String? = if(toShowNote.replyCount > 0) toShowNote.replyCount.toString() else null

    val reNoteCount: String?
        get() = if(toShowNote.reNoteCount > 0) toShowNote.reNoteCount.toString() else null

    //val reactionCount: String? = if(toShowNote.reactionCounts?.isNullOrEmpty() == false) toShowNote.reactionCounts?.size.toString() else null
    val reactionCounts = toShowNote.reactionCounts

    val reactionCount = ObservableField<Int>().apply{
        var sum = 0
        reactionCounts?.forEach{
            sum += it.value
        }
        this.set(sum)
    }



    //reNote先
    val subNote: Note? = toShowNote.reNote

    val subNoteUserName = subNote?.user?.userName
    val subNoteName = subNote?.user?.name
    val subNoteAvatarUrl = subNote?.user?.avatarUrl
    val subNoteText = subNote?.text
    val subNoteEmojis = subNote?.emojis


    fun addReaction(reaction: String){
        var count = reactionCount.get()
        if(count == null) count = 1 else count++
        reactionCount.set(count)
    }

    fun takeReaction(reaction: String){

    }



}