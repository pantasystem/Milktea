package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.pantasystem.milktea.data.model.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import net.pantasystem.milktea.data.model.account.Account

import net.pantasystem.milktea.data.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.getPreferenceName
import java.lang.IllegalArgumentException
import net.pantasystem.milktea.data.model.account.page.Pageable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TimelineViewModelFactory(
    private val account: Account?,
    private val accountId: Long? = account?.accountId,
    private val pageable: Pageable,
    private val miApplication: MiApplication
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
            return TimelineViewModel(account, accountId, pageable, miApplication, miApplication.getSettingStore(), include) as T


        }


        throw IllegalArgumentException("error")
    }
}