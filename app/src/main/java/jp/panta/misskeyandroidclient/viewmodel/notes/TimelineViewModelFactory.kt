package jp.panta.misskeyandroidclient.viewmodel.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class TimelineViewModelFactory(
    private val accountRelation: AccountRelation,
    private val requestSetting: NoteRequest.Setting,
    private val miApplication: MiApplication,
    private val settingStore: SettingStore
) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java){


            return TimelineViewModel(accountRelation, requestSetting, miApplication, settingStore, miApplication.getEncryption()) as T

        }


        throw IllegalArgumentException("error")
    }
}