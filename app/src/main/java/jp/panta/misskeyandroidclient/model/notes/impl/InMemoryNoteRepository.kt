package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteNotFoundException
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryNoteRepository(
    loggerFactory: Logger.Factory
): NoteRepository{

    val logger = loggerFactory.create("InMemoryNoteRepository")

    private val notes = HashMap<Note.Id, Note>()

    private val mutex = Mutex()

    private val listeners = mutableSetOf<NoteRepository.Listener>()

    // 参照されているNoteのId => 参照しているNoteのId
    private val referencedNoteIds = mutableMapOf<Note.Id, MutableSet<Note.Id>>()

    init {
        listeners.add { ev ->
            if (ev is NoteRepository.Event.Deleted) {
                synchronized(referencedNoteIds) {
                    referencedNoteIds.remove(ev.noteId)?.forEach { related ->
                        removeRaw(related)
                    }
                }
            }
            else if (ev is NoteRepository.Event.Updated) {
                synchronized(referencedNoteIds) {
                    referencedNoteIds[ev.noteId]?.forEach { related ->
                        val relatedNote = find(related)
                        if(relatedNote?.replyId == )
                        createOrUpdate()
                    }
                }
            }
        }
    }

    override fun addEventListener(listener: NoteRepository.Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override suspend fun get(noteId: Note.Id): Note {
        return find(noteId)?: throw NoteNotFoundException(noteId)
    }

    /**
     * @param note 追加するノート
     * @return ノートが新たに追加されるとtrue、上書きされた場合はfalseが返されます。
     */
    override suspend fun add(note: Note): AddResult {
        return createOrUpdate(note)
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
        return removeRaw(noteId)
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

    private fun removeRaw(noteId: Note.Id): Boolean {
        synchronized(notes) {
            val n = this.notes[noteId]
            this.notes.remove(noteId)
            if(n == null){
                return false
            }
            publish(NoteRepository.Event.Deleted(noteId = noteId))
            return true
        }
    }

    private fun find(noteId: Note.Id): Note? {
        synchronized(notes) {
            return this.notes[noteId]
        }
    }

    private fun createOrUpdate(note: Note): AddResult {
        synchronized(notes) {
            val n = this.notes[note.id]
            if(n != null && n.instanceUpdatedAt != note.instanceUpdatedAt){
                return AddResult.CANCEL
            }
            this.notes[note.id] = note
            note.updated()

            return if(n == null){
                publish(NoteRepository.Event.Created(note.id, note))
                addRelation(note)
                AddResult.CREATED
            } else {
                publish(NoteRepository.Event.Updated(note.id, note))
                AddResult.UPDATED
            }
        }
    }

    private fun addRelation(note: Note) {
        synchronized(referencedNoteIds) {
            if(note.renoteId != null) {
                val sets =referencedNoteIds[note.renoteId]?: mutableSetOf()

                // 循環参照をチェックする
                // NOTE 構造上循環参照が発生すると無限ループが発生する可能性がある
                val refMe = referencedNoteIds[note.id]
                if(refMe.isNullOrEmpty() || !refMe.contains(note.renoteId)) {
                    sets.add(note.id)
                }
                referencedNoteIds[note.renoteId] = sets
            }

            if(note.replyId != null) {
                val sets = referencedNoteIds[note.renoteId]?: mutableSetOf()

                val refMe = referencedNoteIds[note.id]
                if(refMe.isNullOrEmpty() || !refMe.contains(note.replyId)) {
                    sets.add(note.id)
                }
                referencedNoteIds[note.replyId] = sets
            }


        }
    }

    private fun publish(e: NoteRepository.Event) {
        synchronized(listeners) {
            listeners.forEach { listener ->
                listener.on(e)
            }
        }
    }

}