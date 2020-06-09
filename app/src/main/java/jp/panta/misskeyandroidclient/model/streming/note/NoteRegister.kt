package jp.panta.misskeyandroidclient.model.streming.note

import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.util.*
import kotlin.collections.HashMap

/**
 * キャプチャーするノートをローカルで管理するためのクラス
 * このクラスでadd, removeなどの処理をしてもremoteに影響されることは基本的にはない
 */
class NoteRegister {
    val registerId = UUID.randomUUID().toString()

    private val mNotes = HashMap<String, NoteIdentityGroup>()

    fun add(note: PlaneNoteViewData){
        synchronized(mNotes){
            val exNote = mNotes[note.toShowNote.id]
                ?: NoteIdentityGroup(note.toShowNote.id)
            if(!exNote.isReferenced(note)){
                exNote.add(note)
            }
            mNotes[exNote.noteId] = exNote
        }
    }

    fun remove(note: PlaneNoteViewData){
        val noteIdentityGroup = synchronized(mNotes){
            mNotes[note.toShowNote.id]
        }?: return

        synchronized(noteIdentityGroup){
            noteIdentityGroup.remove(note)

            // noteIdentityGroupの仲間で空になればNoteIdentityを削除します
            if(noteIdentityGroup.isEmpty()){
                synchronized(mNotes){
                    mNotes.remove(noteIdentityGroup.noteId)
                }
            }
        }

    }

    fun contains(note: PlaneNoteViewData): Boolean{
        return contains(note.toShowNote.id)
    }

    fun contains(noteId: String): Boolean{
        synchronized(mNotes){
            return mNotes.contains(noteId)
        }
    }

    fun registeredNoteIds(): List<String>{
        return mNotes.keys.toList()
    }

    fun getNoteIdentityGroups(): List<NoteIdentityGroup>{
        return mNotes.values.toList()
    }

    fun clear(){
        mNotes.clear()
    }

}