package jp.panta.misskeyandroidclient.viewmodel.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication

import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.util.getPreferenceName
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.page.Page

@Suppress("UNCHECKED_CAST")
class TimelineViewModelFactory(
    private val page: Page,
    private val miApplication: MiApplication,
    private val settingStore: SettingStore
) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java){

            val sharedPreferences = miApplication.getSharedPreferences(miApplication.getPreferenceName(), Context.MODE_PRIVATE)
            val includeMyRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_MY_RENOTES.name, true)
            val includeRenotedMyNotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.name, true)
            val includeLocalRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.name, true)
            val include = NoteRequest.Include(
                includeLocalRenotes = includeLocalRenotes,
                includeRenotedMyNotes = includeRenotedMyNotes,
                includeMyRenotes = includeMyRenotes
            )

            return TimelineViewModel(page, include, miApplication, settingStore, miApplication.getEncryption()) as T

        }


        throw IllegalArgumentException("error")
    }
}