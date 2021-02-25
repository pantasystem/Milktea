package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPIProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNoteRepository(
    val noteSubscriberProvider: NoteCaptureAPIProvider,
    val accountRepository: AccountRepository,
    val coroutineScope: CoroutineScope,
    loggerFactory: Logger.Factory
): NoteRepository{

    val logger = loggerFactory.create("InMemoryNoteRepository")

    private val notes = HashMap<Note.Id, Note>()

    private val mutex = Mutex()

    override var listener: NoteRepository.Listener = object : NoteRepository.Listener {
        override fun on(e: NoteRepository.Event) {
            logger.warning("リスナーが未設定です。 event:$e")
        }
    }


    override suspend fun get(noteId: Note.Id): Note? {
        mutex.withLock{
            return notes[noteId]
        }
    }

    /**
     * @param note 追加するノート
     * @return ノートが新たに追加されるとtrue、上書きされた場合はfalseが返されます。
     */
    override suspend fun add(note: Note): AddResult {
        mutex.withLock{
            val n = this.notes[note.id]
            if(n != null && n.instanceUpdatedAt != note.instanceUpdatedAt){
                return AddResult.CANCEL
            }
            this.notes[note.id] = note
            note.updated()

            return if(n == null){
                listener.on(NoteRepository.Event.Created(note.id))
                AddResult.CREATED
            } else {
                listener.on(NoteRepository.Event.Updated(note.id))
                AddResult.UPDATED
            }
        }
    }

    override suspend fun addAll(notes: List<Note>): List<AddResult> {
        return notes.map{
            this.add(it)
        }
    }

    /**
     * @param noteId 削除するNoteのid
     * @return 実際に削除されるとtrue、そもそも存在していなかった場合にはfalseが返されます
     */
    override suspend fun remove(noteId: Note.Id): Boolean {
        mutex.withLock{
            val n = this.notes[noteId]
            this.notes.remove(noteId)
            if(n == null){
                return false
            }
            listener.on(NoteRepository.Event.Deleted(noteId = noteId))
            return true
        }
    }

    override suspend fun removeByUserId(userId: User.Id): Int {
        return mutex.withLock {
            notes.values.filter {
                it.userId == userId
            }.count {
                this.remove(it.id)
            }
        }
    }

    /*@ExperimentalCoroutinesApi
    private fun subscribe(noteId: Note.Id) {
        coroutineScope.launch(Dispatchers.IO) {
            noteSubscriberProvider.get(noteId.accountId)?.subscribe(noteId.noteId)
                ?.collect { e->
                    when(e.body){
                        is NoteUpdated.Body.Deleted -> {
                            remove(noteId)
                        }
                        is NoteUpdated.Body.Reacted -> {
                            var note = get(noteId)
                            if(note != null){
                                val counts = ArrayList(note.reactionCounts)
                                var isFind = false
                                for(i in 0 until counts.size) {
                                    val count = counts[i]
                                    if(count.reaction == e.body.body.reaction) {
                                        counts[i] = count.copy(count = count.count + 1)
                                        isFind = true
                                        break
                                    }
                                }
                                if(!isFind){
                                    counts.add(ReactionCount(e.body.body.reaction, 1))
                                }
                                val myReaction = if(e.body.body.userId == accountRepository.get(noteId.accountId).remoteId) e.body.body.reaction else note.myReaction

                                note = note.copy(
                                    reactionCounts = counts,
                                    myReaction = myReaction
                                )
                                add(note)
                            }
                        }

                        is NoteUpdated.Body.Unreacted -> {
                            val note = get(noteId)
                            if(note != null){
                                val counts = note.reactionCounts
                                    .filterNot {
                                        it.reaction == e.body.body.reaction && it.count <= 1
                                    }
                                val myReaction = if(e.body.body.userId == accountRepository.get(noteId.accountId).remoteId) null else note.myReaction
                                add(note.copy(reactionCounts = counts, myReaction = myReaction))
                            }
                        }
                        is NoteUpdated.Body.PollVoted -> {
                            val note = get(noteId)
                            if(note != null){
                                val poll = note.poll?.let{
                                    val choices = ArrayList(it.choices)
                                        .mapIndexed{ i, c ->
                                            if(e.body.body.choice == i){
                                                val isVoted =  c.isVoted || accountRepository.get(noteId.accountId).remoteId == e.body.body.userId
                                                c.copy(votes = c.votes + 1, isVoted = isVoted)
                                            }else{
                                                c
                                            }
                                        }
                                    it.copy(
                                        choices = choices
                                    )
                                }
                                add(note.copy(poll = poll))
                            }
                        }
                    }
                }
        }
    }
*/
}