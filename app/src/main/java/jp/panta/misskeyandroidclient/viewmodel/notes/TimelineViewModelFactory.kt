package jp.panta.misskeyandroidclient.viewmodel.notes

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.notes.LiveNotePagingStore
import java.lang.IllegalArgumentException

class TimelineViewModelFactory(private val type: TimelineViewModel.Type) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java)
            return TimelineViewModel(type) as T

        throw IllegalArgumentException("error")
    }
}