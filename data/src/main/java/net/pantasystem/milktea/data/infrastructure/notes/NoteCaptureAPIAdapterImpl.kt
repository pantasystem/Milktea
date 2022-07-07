package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.streaming.NoteUpdated
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource

/**
 * model層とNoteCaptureAPIをいい感じに接続する
 */
class NoteCaptureAPIAdapterImpl(
    private val accountRepository: AccountRepository,
    private val noteDataSource: NoteDataSource,
    private val noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
    loggerFactory: Logger.Factory,
    cs: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NoteDataSource.Listener, NoteCaptureAPIAdapter {

    private val logger = loggerFactory.create("NoteCaptureAPIAdapter")

    private val coroutineScope = CoroutineScope(cs.coroutineContext + dispatcher)


    init {
        noteDataSource.addEventListener(this)
    }

    private val noteIdWithListeners =
        mutableMapOf<Note.Id, MutableSet<(NoteDataSource.Event) -> Unit>>()

    private val noteIdWithJob = mutableMapOf<Note.Id, Job>()

    private val noteUpdatedDispatcher = MutableSharedFlow<Pair<Account, NoteUpdated.Body>>()

    init {
        coroutineScope.launch(dispatcher) {
            noteUpdatedDispatcher.collect {
                handleRemoteEvent(it.first, it.second)
            }
        }
    }

    override fun on(e: NoteDataSource.Event) {

        synchronized(noteIdWithListeners) {
            noteIdWithListeners[e.noteId]?.forEach { callback ->
                coroutineScope.launch {
                    callback.invoke(e)
                }
            }

        }

    }

    override fun capture(id: Note.Id): Flow<NoteDataSource.Event> = channelFlow {
        val account = accountRepository.get(id.accountId).getOrThrow()

        val repositoryEventListener: (NoteDataSource.Event) -> Unit = { ev ->
            trySend(ev)
        }

        synchronized(noteIdWithJob) {
            if (addRepositoryEventListener(id, repositoryEventListener)) {
                logger.debug("未登録だったのでRemoteに対して購読を開始する")
                val job = noteCaptureAPIWithAccountProvider.get(account)
                    .capture(id.noteId)
                    .catch { e ->
                        logger.error("ノート更新イベント受信中にエラー発生", e = e)
                    }
                    .onEach {
                        noteUpdatedDispatcher.emit(account to it)
                    }.launchIn(coroutineScope)
                noteIdWithJob[id] = job
            }
        }

        awaitClose {
            // NoteCaptureの購読を解除する
            synchronized(noteIdWithJob) {
                // リスナーを解除する
                if (removeRepositoryEventListener(id, repositoryEventListener)) {

                    // すべてのリスナーが解除されていればRemoteへの購読も解除する
                    noteIdWithJob.remove(id)?.cancel() ?: run {
                        logger.warning("購読解除しようとしたところすでに解除されていた")
                    }
                }
            }
        }
    }.shareIn(coroutineScope, replay = 1, started = SharingStarted.WhileSubscribed())


    /**
     * @return Note.Idが初めてListenerに登録されるとtrueが返されます。
     */
    private fun addRepositoryEventListener(
        noteId: Note.Id,
        listener: (NoteDataSource.Event) -> Unit
    ): Boolean {
        synchronized(noteIdWithListeners) {
            val listeners = noteIdWithListeners[noteId]
            return if (listeners.isNullOrEmpty()) {
                noteIdWithListeners[noteId] = mutableSetOf(listener)
                true
            } else {
                listeners.add(listener)
                noteIdWithListeners[noteId] = listeners
                false
            }
        }

    }

    /**
     * @return Note.Idに関連するListenerすべてが解除されるとfalseが返されます。
     */
    private fun removeRepositoryEventListener(
        noteId: Note.Id,
        listener: (NoteDataSource.Event) -> Unit
    ): Boolean {

        synchronized(noteIdWithListeners) {
            val listeners: MutableSet<(NoteDataSource.Event) -> Unit> =
                noteIdWithListeners[noteId] ?: return false

            if (!listeners.remove(listener)) {
                logger.warning("リスナーの削除に失敗しました。")
                return false
            }

            if (listeners.isEmpty()) {
                return true
            }
            return false
        }

    }

    /**
     * リポジトリを更新する
     */
    private suspend fun handleRemoteEvent(account: Account, e: NoteUpdated.Body) {
        val noteId = Note.Id(account.accountId, e.id)
        try {
            val note = noteDataSource.get(noteId)
            when (e) {
                is NoteUpdated.Body.Deleted -> {
                    noteDataSource.remove(noteId)
                }
                is NoteUpdated.Body.Reacted -> {
                    noteDataSource.add(note.onReacted(account, e))
                }
                is NoteUpdated.Body.Unreacted -> {
                    noteDataSource.add(note.onUnReacted(account, e))
                }
                is NoteUpdated.Body.PollVoted -> {
                    noteDataSource.add(note.onPollVoted(account, e))
                }

            }
        } catch (e: Exception) {
            logger.warning("更新対称のノートが存在しませんでした:$noteId", e = e)
        }


    }


}