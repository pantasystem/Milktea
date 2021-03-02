package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * NoteCaptureをCoroutineScopeで管理する
 */
class ScopedNoteCapture(
    val coroutineScope: CoroutineScope,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    val noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
    val accountRepository: AccountRepository,
    val noteRepository: NoteRepository,
    loggerFactory: Logger.Factory
) {
    private val logger = loggerFactory.create("ScopedNoteCapture")

    fun capture(noteId: Note.Id) {
        // NOTE: coroutineScopeの破棄とともにCaptureも解除される
        coroutineScope.launch(dispatcher) {
            val account = accountRepository.get(noteId.accountId)
            noteCaptureAPIWithAccountProvider.get(account).capture(noteId.noteId).onEach { e ->
                launch(dispatcher) {
                    handle(account, e)
                }
            }.launchIn(this)
        }
    }

    private suspend fun handle(account: Account, e: NoteUpdated.Body) {
        val noteId = Note.Id(account.accountId, e.id)
        try{
            val note = noteRepository.get(noteId)
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
        if(!hasItem) {
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