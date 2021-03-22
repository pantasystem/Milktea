package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * model層とNoteCaptureAPIをいい感じに接続する
 */
class NoteCaptureAPIAdapter(
    private val accountRepository: AccountRepository,
    private val noteDataSource: NoteDataSource,
    private val noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
    loggerFactory: Logger.Factory,
    cs: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NoteDataSource.Listener {

    private val logger = loggerFactory.create("NoteCaptureAPIAdapter")

    private val coroutineScope = CoroutineScope(cs.coroutineContext + dispatcher)


    init {
        noteDataSource.addEventListener(this)
    }

    private val noteIdWithListeners = mutableMapOf<Note.Id, MutableSet<(NoteDataSource.Event)->Unit>>()

    private val noteIdWithJob = mutableMapOf<Note.Id, Job>()

    private val noteUpdatedDispatcher = MutableSharedFlow<Pair<Account, NoteUpdated.Body>>()

    init{
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

    @ExperimentalCoroutinesApi
    fun capture(id: Note.Id) : Flow<NoteDataSource.Event> = channelFlow {
        val account = accountRepository.get(id.accountId)

        val repositoryEventListener: (NoteDataSource.Event)->Unit = { ev ->
            offer(ev)
        }

        synchronized(noteIdWithJob) {
            if(addRepositoryEventListener(id, repositoryEventListener)){
                logger.debug("未登録だったのでRemoteに対して購読を開始する")
                val job = noteCaptureAPIWithAccountProvider.get(account)
                    .capture(id.noteId)
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
                if(removeRepositoryEventListener(id, repositoryEventListener)){

                    // すべてのリスナーが解除されていればRemoteへの購読も解除する
                    noteIdWithJob.remove(id)?.cancel()?: run{
                        logger.warning("購読解除しようとしたところすでに解除されていた")
                    }
                }
            }
        }
    }.shareIn(coroutineScope, replay = 1, started = SharingStarted.Lazily)


    /**
     * @return Note.Idが初めてListenerに登録されるとtrueが返されます。
     */
    private fun addRepositoryEventListener(noteId: Note.Id, listener: (NoteDataSource.Event)-> Unit): Boolean {
        synchronized(noteIdWithListeners) {
            val listeners = noteIdWithListeners[noteId]
            return if(listeners.isNullOrEmpty()) {
                noteIdWithListeners[noteId] = mutableSetOf(listener)
                true
            }else{
                listeners.add(listener)
                noteIdWithListeners[noteId] = listeners
                false
            }
        }

    }

    /**
     * @return Note.Idに関連するListenerすべてが解除されるとfalseが返されます。
     */
    private fun removeRepositoryEventListener(noteId: Note.Id, listener: (NoteDataSource.Event)-> Unit): Boolean {

        synchronized(noteIdWithListeners) {
            val listeners: MutableSet<(NoteDataSource.Event) -> Unit> =
                noteIdWithListeners[noteId] ?: return false

            if(!listeners.remove(listener)){
                logger.warning("リスナーの削除に失敗しました。")
                return false
            }

            if(listeners.isEmpty()) {
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
        try{
            val note = noteDataSource.get(noteId)
            when(e) {
                is NoteUpdated.Body.Deleted -> {
                    noteDataSource.remove(noteId)
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
        }catch(e: Exception){
            logger.warning("更新対称のノートが存在しませんでした:$noteId", e = e)
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

        noteDataSource.add(
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
        if(!hasItem) {
            val added = list.toMutableList()
            added.add(ReactionCount(reaction = e.body.reaction, count = 1))
            list = added
        }
        val emojis = e.body.emoji?.let {
            note.emojis?.let {
                it.toMutableList().also { eList ->
                    eList.add(e.body.emoji)
                }
            }
        }?: note.emojis

        noteDataSource.add(
            note.copy(
                reactionCounts = list,
                myReaction = if(e.body.userId == account.remoteId) e.body.reaction else note.myReaction,
                emojis = emojis
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
        noteDataSource.add(
            note.copy(
                poll = poll.copy(choices = updatedChoices)
            )
        )
    }
}