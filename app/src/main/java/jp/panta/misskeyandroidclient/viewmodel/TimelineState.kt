package jp.panta.misskeyandroidclient.viewmodel

import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

data class TimelineState(
    val notes: List<PlaneNoteViewData>,
    val state: State
){
    enum class State{
        INIT,
        LOAD_NEW,
        LOAD_OLD,
        RECEIVED_NEW
    }

    fun getSinceId(): String?{
        return notes.firstOrNull()?.id
    }

    fun getUntilId(): String?{
        return notes.lastOrNull()?.id
    }
}