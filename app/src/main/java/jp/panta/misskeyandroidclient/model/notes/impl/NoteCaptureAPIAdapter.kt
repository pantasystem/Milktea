package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * model層とNoteCaptureAPIをいい感じに接続する
 */
class NoteCaptureAPIAdapter(
    private val accountRepository: AccountRepository,
    private val noteRepository: NoteRepository,
    private val noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
    private val coroutineScope: CoroutineScope,
    loggerFactory: Logger.Factory
) : NoteRepository.Listener {

    private val logger = loggerFactory.create("NoteCaptureAPIAdapter")

    private val accountWithNoteCaptureAPI = mutableMapOf<Long, NoteCaptureAPI>()
    private val lock = Mutex()

    init {
        noteRepository.listener = this
    }

    private val noteIdWithListeners = mutableMapOf<Note.Id, MutableSet<(NoteRepository.Event)->Unit>>()

    override fun on(e: NoteRepository.Event) {

        synchronized(noteIdWithListeners) {
            noteIdWithListeners[e.noteId]?.forEach { callback ->
                coroutineScope.launch {
                    callback.invoke(e)
                }
            }

        }

    }

    @ExperimentalCoroutinesApi
    suspend fun capture(id: Note.Id) : Flow<NoteRepository.Event> {
        val account = accountRepository.get(id.accountId)

        return channelFlow {

            val remoteNoteCaptureJob = launch {
                noteCaptureAPIWithAccountProvider.get(account)
                    .capture(id.noteId)
                    .collect {
                        launch {
                            handleRemoteEvent(account, it)
                        }
                    }
            }

            val repositoryEventListener: (NoteRepository.Event)->Unit = { ev ->
                offer(ev)
            }

            addRepositoryEventListener(id, repositoryEventListener)

            awaitClose {
                // NoteCaptureの購読を解除する
                remoteNoteCaptureJob.cancel()

                // リスナーを解除する
                removeRepositoryEventListener(id, repositoryEventListener)
            }


        }




    }

    private fun addRepositoryEventListener(noteId: Note.Id, listener: (NoteRepository.Event)-> Unit) {
        coroutineScope.launch {
            lock.withLock {
                var listeners = noteIdWithListeners[noteId]
                if(listeners == null) {
                    listeners = mutableSetOf(listener)
                }else{
                    listeners.add(listener)
                }
                noteIdWithListeners[noteId] = listeners
            }
        }
    }

    private fun removeRepositoryEventListener(noteId: Note.Id, listener: (NoteRepository.Event)-> Unit) {

        coroutineScope.launch {
            lock.withLock {
                val listeners: MutableSet<(NoteRepository.Event) -> Unit> =
                    noteIdWithListeners[noteId] ?: return@withLock

                if(!listeners.remove(listener)){
                    logger.warning("リスナーの削除に失敗しました。")
                }

            }
        }
    }

    /**
     * リポジトリを更新する
     */
    private suspend fun handleRemoteEvent(account: Account, e: NoteUpdated.Body) {
        val noteId = Note.Id(account.accountId, e.id)
        val note = noteRepository.get(noteId)
        if(note == null) {
            logger.warning("更新対称のノートが存在しませんでした:$noteId")
            return
        }
        when(e) {
            is NoteUpdated.Body.Deleted -> {
                noteRepository.remove(noteId)
            }
            is NoteUpdated.Body.Reacted-> {
                onReacted(note, account, e)
            }
            is NoteUpdated.Body.Unreacted -> {
                onUnReacted(note, account, e)
            }
            is NoteUpdated.Body.PollVoted -> {
                onPollVoted(note, account, e)
            }

        }

    }

    private suspend fun onUnReacted(note: Note, account: Account, e: NoteUpdated.Body.Unreacted) {
        val list = note.reactionCounts.toMutableList()
        val newList = list.asSequence().map {
            if(it.reaction == e.body.reaction) {
                it.copy(count = it.count - 1)
            }else{
                it
            }
        }.filter {
            it.count > 0
        }.toList()

        noteRepository.add(
            note.copy(
                reactionCounts = newList,
                myReaction = if(e.body.userId == account.remoteId) null else e.body.reaction

            )
        )
    }

    private suspend fun onReacted(note: Note, account: Account, e: NoteUpdated.Body.Reacted) {
        var hasItem = false
        var list = note.reactionCounts.map { count ->
            if(count.reaction == e.body.reaction) {
                hasItem = true
                count.copy(count = count.count + 1)
            }else{
                count
            }
        }
        if(hasItem) {
            val added = list.toMutableList()
            added.add(ReactionCount(reaction = e.body.reaction, count = 1))
            list = added
        }
        noteRepository.add(
            note.copy(
                reactionCounts = list,
                myReaction = if(e.body.userId == account.remoteId) e.body.reaction else note.myReaction
            )
        )
    }

    private suspend fun onPollVoted(note: Note, account: Account, e: NoteUpdated.Body.PollVoted) {
        val poll = note.poll
        requireNotNull(poll){
            "pollがNULLです"
        }
        val updatedChoices = poll.choices.mapIndexed { index, choice ->
            if(index == e.body.choice) {
                choice.copy(
                    votes = choice.votes + 1,
                    isVoted = if(e.body.userId == account.remoteId) true else choice.isVoted
                )
            }else{
                choice
            }
        }
        noteRepository.add(
            note.copy(
                poll = poll.copy(choices = updatedChoices)
            )
        )
    }
}