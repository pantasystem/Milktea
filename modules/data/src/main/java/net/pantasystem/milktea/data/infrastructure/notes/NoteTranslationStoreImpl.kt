package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.translation.Translate
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.app_store.notes.NoteTranslationsState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.Translation
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteTranslationStoreImpl @Inject constructor(
    val noteRepository: NoteRepository,
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
) : NoteTranslationStore {

    private val _state = MutableStateFlow(NoteTranslationsState(emptyMap(), emptySet(), emptyMap()))

    private val _mutex = Mutex()

    override fun state(id: Note.Id): Flow<ResultState<Translation>> {
        return _state.map { states ->
            states.state(id)
        }
    }

    override suspend fun translate(noteId: Note.Id) {
        if (_state.value.isLoading(noteId)) {
            return
        }
        _mutex.withLock {
            _state.value = _state.value.loading(noteId)
        }

        runCancellableCatching {
            withContext(Dispatchers.IO) {
                val account = accountRepository.get(noteId.accountId).getOrThrow()
                val api = misskeyAPIProvider.get(account.normalizedInstanceUri)
                val req = Translate(
                    i = account.token,
                    targetLang = Locale.getDefault().language,
                    noteId = noteId.noteId,
                )

                val res = api.translate(
                    req
                )
                res.throwIfHasError().body()!!
            }
        }.onFailure {
            _mutex.withLock {
                _state.value = _state.value.complete(noteId, null, it)
            }
        }.onSuccess {
            _mutex.withLock {
                _state.value = _state.value.complete(
                    noteId, Translation(
                        sourceLang = it.sourceLang,
                        text = it.text
                    ), null
                )
            }
        }
    }

}