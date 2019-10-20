package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import java.lang.IllegalArgumentException

class TimelineViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val requestSetting: NoteRequest.Setting?,
    private val noteCapture: NoteCapture,
    private val timelineCapture: TimelineCapture?
) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java)
            return TimelineViewModel(connectionInstance, requestSetting!!, noteCapture, timelineCapture) as T

        throw IllegalArgumentException("error")
    }
}