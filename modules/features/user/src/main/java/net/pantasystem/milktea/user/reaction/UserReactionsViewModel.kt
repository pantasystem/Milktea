package net.pantasystem.milktea.user.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStoreProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

@HiltViewModel
class UserReactionsViewModel @Inject constructor(
    private val urlPreviewStoreProvider: UrlPreviewStoreProvider,
    noteRelationGetter: NoteRelationGetter,
    noteTranslationStore: NoteTranslationStore,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    accountRepository: AccountRepository,
    private val userDataSource: UserDataSource,
) : ViewModel() {
    private val _userId = MutableStateFlow<User.Id?>(null)

    private val currentAccountWatcher = CurrentAccountWatcher(
        accountRepository = accountRepository,
        currentAccountId = null
    )

    private val cache = PlaneNoteViewDataCache(
        currentAccountWatcher::getAccount,
        noteCaptureAPIAdapter,
        noteTranslationStore,
        { account -> urlPreviewStoreProvider.getUrlPreviewStore(account) },
        viewModelScope,
        noteRelationGetter,
    )



    fun setUserId(userId: User.Id) {
        _userId.value = userId
        currentAccountWatcher.currentAccountId = userId.accountId
    }

}