package jp.panta.misskeyandroidclient.viewmodel.notes

data class TimelineState(
    val notes: List<PlaneNoteViewData>,
    val state: State
){
    enum class State{
        INIT,
        LOAD_NEW,
        LOAD_OLD,
        RECEIVED_NEW,
        REMOVED
    }

    fun getSinceId(): String?{
        return notes.firstOrNull()?.getRequestId()
    }

    fun getUntilId(): String?{
        return notes.lastOrNull()?.getRequestId()
    }
}