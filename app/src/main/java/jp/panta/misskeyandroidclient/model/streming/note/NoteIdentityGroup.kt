package jp.panta.misskeyandroidclient.model.streming.note

import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.util.*

class NoteIdentityGroup(
    val noteId: String
){

    private val mIdentityNotes = ArrayList<PlaneNoteViewData>()

    /**
     * 同じNoteIdのノートを追加します
     * @return 管理しているノートのカウントを返す
     */
    fun add(note: PlaneNoteViewData): Int{
        if(noteId != note.toShowNote.id){
            synchronized(mIdentityNotes){
                return mIdentityNotes.size
            }
        }
        synchronized(mIdentityNotes){
            mIdentityNotes.add(note)
            return mIdentityNotes.size
        }
    }

    /**
     * 同じ参照のノートを削除します
     * @return 実行後のノートのカウント数が返されます
     */
    fun remove(note: PlaneNoteViewData): Int{
        if(noteId != note.toShowNote.id){
            return mIdentityNotes.size
        }
        synchronized(mIdentityNotes){
            val iterator = mIdentityNotes.iterator()
            while(iterator.hasNext()){
                if(note === iterator.next()){
                    iterator.remove()
                }
            }
            return mIdentityNotes.size
        }


    }

    /**
     * @return 同じ参照のノートがを含んで入ればtrue を返します
     */
    fun isReferenced(note: PlaneNoteViewData): Boolean{
        synchronized(mIdentityNotes){
            val iterator = mIdentityNotes.iterator()
            while(iterator.hasNext()){
                if(iterator.next() === note){
                    return true
                }
            }
            return false
        }

    }
}