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

    /**
     * sinceIdを複数取得します。
     * @param size 取得数をここに指定します。ノート数がこれにみたいない場合このサイズの用件を満たさない場合があります。
     * @return sinceIdの候補、先頭ほど最新のノートIdです。
     */
    fun getSinceIds(size: Int): List<String>{
        var index = 0
        val list = ArrayList<String>()
        while(index < notes.size && list.size < size){
            if(notes[index].note.tmpFeaturedId.isNullOrBlank()){
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
        while(notes.size - counter >= 0 && list.size < size){
            if(notes[notes.size - 1 - counter].note.tmpFeaturedId.isNullOrBlank()){
                list.add(notes[notes.size - 1 - counter].getRequestId())
            }
            counter ++
        }
        return list
    }


}