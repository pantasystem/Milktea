package jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import java.util.*

class PollChoice(val choice: Poll.Choice? = null) {
    val id = UUID.randomUUID().toString()
    var text = MutableLiveData<String>(choice?.text)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PollChoice

        if (id != other.id) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }


}