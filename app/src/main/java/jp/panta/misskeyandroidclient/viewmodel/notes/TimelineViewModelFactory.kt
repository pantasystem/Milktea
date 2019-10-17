package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import java.lang.IllegalArgumentException

class TimelineViewModelFactory(private val requestSetting: NoteRequest.Setting?) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java)
            return TimelineViewModel(requestSetting!!) as T

        throw IllegalArgumentException("error")
    }
}