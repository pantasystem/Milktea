package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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
    //val reactionCounts = toShowNote.reactionCounts
    val reactionCounts = MutableLiveData<LinkedHashMap<String, Int>>(toShowNote.reactionCounts)

    val reactionCount = Transformations.map(reactionCounts){
        var sum = 0
        it.forEach{ map ->
            sum += map.value
        }
        return@map sum
    }

    val myReaction = MutableLiveData<String>(toShowNote.myReaction)

    //reNote先
    val subNote: Note? = toShowNote.reNote

    val subNoteUserName = subNote?.user?.userName
    val subNoteName = subNote?.user?.name
    val subNoteAvatarUrl = subNote?.user?.avatarUrl
    val subNoteText = subNote?.text
    val subNoteEmojis = subNote?.emojis


    fun addReaction(reaction: String, isMyReaction: Boolean = false){
        val reactions = reactionCounts.value?: LinkedHashMap()
        val existingReactionCount = reactions[reaction]
        if(existingReactionCount == null){
            reactions[reaction] = 1
        }else{
            reactions[reaction] = existingReactionCount + 1
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

    init{
        Log.d("PlaneNoteViewData", "reactions: ${toShowNote.reactionCounts}, myReaction: ${this.myReaction.value}")
    }

}