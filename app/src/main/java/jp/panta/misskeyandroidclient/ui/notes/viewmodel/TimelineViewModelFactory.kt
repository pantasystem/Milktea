package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelFactory(
    private val account: Account?,
    private val accountId: Long? = account?.accountId,
    private val pageable: Pageable,
    private val miApplication: MiApplication,
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == TimelineViewModel::class.java){

            val settingStore = miApplication.getSettingStore()
            val include = NoteRequest.Include(
                includeLocalRenotes = settingStore.configState.value.isIncludeLocalRenotes,
                includeRenotedMyNotes = settingStore.configState.value.isIncludeRenotedMyNotes,
                includeMyRenotes = settingStore.configState.value.isIncludeMyRenotes
            )
            return TimelineViewModel(account, accountId, pageable, miApplication, include) as T

        }


        throw IllegalArgumentException("error")
    }
}