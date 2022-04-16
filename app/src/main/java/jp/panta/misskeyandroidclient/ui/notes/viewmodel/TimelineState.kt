package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import net.pantasystem.milktea.data.model.notes.NoteRelation

sealed class TimelineState{
    abstract val notes: List<PlaneNoteViewData>
    data class Init(override val notes: List<PlaneNoteViewData>) : TimelineState()
    data class LoadNew(override val notes: List<PlaneNoteViewData>) : TimelineState()
    data class LoadOld(override val notes: List<PlaneNoteViewData>) : TimelineState()
    data class ReceivedNew(override val notes: List<PlaneNoteViewData>) : TimelineState()
    data class Deleted(override val notes: List<PlaneNoteViewData>) : TimelineState()


    /**
     * sinceIdを複数取得します。
     * @param size 取得数をここに指定します。ノート数がこれにみたいない場合このサイズの用件を満たさない場合があります。
     * @return sinceIdの候補、先頭ほど最新のノートIdです。
     */
    fun getSinceIds(size: Int): List<String>{
        var index = 0
        val list = ArrayList<String>()
        while(index < notes.size && list.size < size){
            if(notes[index].note is NoteRelation.Normal){
                list.add(notes[index].getRequestId())
            }
            index ++
        }

        return list
    }
    /**
     * untilIdを複数取得します。
     * @param size 取得数をここに指定します。ノート数がこれにみたいない場合このサイズの用件を満たさない場合があります。
     * @return untilIdの候補、先頭ほど古いノートIdです。
     */
    fun getUntilIds(size: Int): List<String>{
        var counter = 0
        val list = ArrayList<String>()
        while(notes.size - counter - 1>= 0 && list.size < size){
            if(notes[notes.size - 1 - counter].note is NoteRelation.Normal){
                list.add(notes[notes.size - 1 - counter].getRequestId())
            }
            counter ++
        }
        return list
    }


}