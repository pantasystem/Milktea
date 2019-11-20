package jp.panta.misskeyandroidclient.viewmodel.notes

import android.preference.PreferenceManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class TimelineViewModelFactory(
    private val connectionInstance: ConnectionInstance,
    private val requestSetting: NoteRequest.Setting,
    private val miApplication: MiApplication,
    private val settingStore: SettingStore
    //private val noteCapture: NoteCapture,
    //private val timelineCapture: TimelineCapture?
) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java){

            val misskeyAPI = miApplication.misskeyAPIService ?: MisskeyAPIServiceBuilder.build(connectionInstance.instanceBaseUrl)

            return TimelineViewModel(connectionInstance, requestSetting, misskeyAPI, settingStore) as T

        }


        throw IllegalArgumentException("error")
    }
}