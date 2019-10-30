package jp.panta.misskeyandroidclient.viewmodel.notification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(
    connectionInstance: ConnectionInstance,
    misskeyAPI: MisskeyAPI,
    noteCapture: NoteCapture
) : ViewModel(){

    var isLoadingFlag = false
    //loadNewはない

    val notificationsLiveData = MutableLiveData<NotificationViewData>()

    fun loadInit(){
        viewModelScope.launch(Dispatchers.IO){

        }
    }
    fun loadOld(){
        viewModelScope.launch(Dispatchers.IO){

        }
    }


}