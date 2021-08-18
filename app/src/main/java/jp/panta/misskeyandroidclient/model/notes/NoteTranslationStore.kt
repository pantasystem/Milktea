package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.notes.translation.Translate
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

data class NoteTranslationsState(
    val noteIdWithTranslation: Map<Note.Id, Translation>,
    val loadings: Set<Note.Id>,
    val loadFails: Map<Note.Id, Throwable>
) {
    fun isLoading(noteId: Note.Id) : Boolean{
        return loadings.contains(noteId)
    }

    fun translation(noteId: Note.Id) : Translation? {
        return noteIdWithTranslation[noteId]
    }

    fun loading(noteId: Note.Id) : NoteTranslationsState {
        return this.copy(
            loadings = this.loadings.toMutableSet().also {
                it.add(noteId)
            }
        )
    }

    fun complete(noteId: Note.Id, translate: Translation?, throwable: Throwable?) : NoteTranslationsState{
        return this.copy(
            loadings = this.loadings.toMutableSet().also {
                it.remove(noteId)
            },
            noteIdWithTranslation = this.noteIdWithTranslation.toMutableMap().also {
                if(translate != null){
                    it[noteId] = translate
                }
            },
            loadFails = this.loadFails.toMutableMap().also {
                if(throwable == null) {
                    it.remove(noteId)
                }else{
                    it[noteId] = throwable
                }
            }
        )
    }


    fun state(noteId: Note.Id): State<Translation> {
        val translation = translation(noteId)
        val isLoading = isLoading(noteId)
        val error = loadFails[noteId]
        val content = if (translation == null) {
            StateContent.NotExist()
        } else {
            StateContent.Exist(translation)
        }
        return when {
            isLoading -> {
                State.Loading(content)
            }
            error != null -> {
                State.Error(content, error)
            }
            else -> {
                State.Fixed(content)
            }
        }

    }
}
class NoteTranslationStore(
    val noteRepository: NoteRepository,
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption
){

    private val _state = MutableStateFlow(NoteTranslationsState(emptyMap(), emptySet(), emptyMap()))

    private val _mutex = Mutex()

    fun state(id: Note.Id) : Flow<State<Translation>> {
        return _state.map { states ->
            states.state(id)
        }
    }

    suspend fun translate(noteId: Note.Id) {
        if(_state.value.isLoading(noteId)) {
            return
        }
        _mutex.withLock {
            _state.value = _state.value.loading(noteId)
        }
        runCatching {
            val account = accountRepository.get(noteId.accountId)
            val api = misskeyAPIProvider.get(account.instanceDomain)
            val res = api.translate(
                Translate(
                    i = account.getI(encryption),
                    targetLang = Locale.getDefault().toString(),
                    noteId = noteId.noteId,
                )
            )
            res.throwIfHasError().body()!!
        }.onFailure {
            _mutex.withLock {
                _state.value = _state.value.complete(noteId, null, it)
            }
        }.onSuccess {
            _mutex.withLock {
                _state.value = _state.value.complete(noteId, Translation(
                    sourceLang = it.sourceLang,
                    text = it.text
                ), null)
            }
        }
    }

}