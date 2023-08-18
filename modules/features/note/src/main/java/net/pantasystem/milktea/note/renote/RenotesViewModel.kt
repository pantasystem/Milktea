package net.pantasystem.milktea.note.renote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.note.*
import net.pantasystem.milktea.model.note.repost.RenoteType
import net.pantasystem.milktea.model.note.repost.RenotesPagingService
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository

class RenotesViewModel @AssistedInject constructor(
    private val renotesPagingServiceFactory: RenotesPagingService.Factory,
    private val noteGetter: NoteRelationGetter,
    private val noteRepository: NoteRepository,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val userRepository: UserRepository,
    configRepository: LocalConfigRepository,
    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    @Assisted val noteId: Note.Id,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(noteId: Note.Id): RenotesViewModel
    }

    companion object;

    private val renotesPagingService by lazy {
        renotesPagingServiceFactory.create(noteId)
    }


    private val logger = loggerFactory.create("RenotesVM")

    val config = configRepository.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DefaultConfig.config,
    )

    val renotes = renotesPagingService.state.map { state ->
        state.suspendConvert { renotes ->
            renotes.mapNotNull {renote ->
                when(renote) {
                    is RenoteType.Renote -> if (renote.isQuote) {
                        null
                    } else {
                        noteGetter.get(renote.noteId).getOrNull()?.let {
                            RenoteItemType.Renote(it)
                        }
                    }
                    is RenoteType.Reblog -> RenoteItemType.Reblog(userRepository.find(renote.userId))
                }
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )

    val myId = accountStore.observeCurrentAccount.map {
        it?.let {
            User.Id(it.accountId, it.remoteId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val account = accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _errors = MutableStateFlow<Throwable?>(null)

    init {
        viewModelScope.launch {
            renotesPagingService.state.mapNotNull {
                (it.content as? StateContent.Exist)?.rawContent
            }.map { renotes ->
                renotes.mapNotNull { renote ->
                    (renote as? RenoteType.Renote)?.let {
                        noteCaptureAPIAdapter.capture(it.noteId)
                    }
                }
            }.map { flows ->
                combine(flows) {
                    it.toList()
                }
            }.collect()
        }
    }

    fun next() {
        viewModelScope.launch {
            runCancellableCatching {
                renotesPagingService.next()
            }.onFailure {
                logger.warning("next error", e = it)
                _errors.value = it
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runCancellableCatching {
                renotesPagingService.refresh()
            }.onFailure {
                logger.warning("refresh error", e = it)
                _errors.value = it
            }
        }
    }

    fun delete(item: RenoteItemType) {
        viewModelScope.launch {
            when(item) {
                is RenoteItemType.Reblog -> {
                    noteRepository.unrenote(noteId).onFailure {
                        _errors.value = it
                    }.onSuccess {
                        refresh()
                    }
                }
                is RenoteItemType.Renote -> {
                    noteRepository.delete(item.note.note.id).onFailure {
                        _errors.value = it
                    }.onSuccess {
                        refresh()
                    }
                }
            }

        }
    }

}

fun RenotesViewModel.Companion.provideViewModel(
    factory: RenotesViewModel.ViewModelAssistedFactory,
    noteId: Note.Id
) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(noteId) as T
    }
}

sealed interface RenoteItemType {

    val user: User
    data class Renote(val note: NoteRelation) : RenoteItemType {
        override val user: User
            get() = note.user
    }

    data class Reblog(override val user: User) : RenoteItemType
}