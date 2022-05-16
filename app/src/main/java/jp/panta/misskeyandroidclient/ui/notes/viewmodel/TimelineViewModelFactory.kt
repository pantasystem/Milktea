package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.getPreferenceName
import net.pantasystem.milktea.data.infrastructure.KeyStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable

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
            return TimelineViewModel(account, accountId, pageable, miApplication, include) as T


        }


        throw IllegalArgumentException("error")
    }
}