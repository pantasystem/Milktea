package net.pantasystem.milktea.data.infrastructure.note.impl.sqlite

import androidx.room.Transaction
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.DefaultDispatcher
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.note.NoteNotFoundException
import net.pantasystem.milktea.model.note.NoteResult
import net.pantasystem.milktea.model.note.NoteThreadContext
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class SQLiteNoteDataSource @Inject constructor(
    private val noteDAO: NoteDAO,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val database: DataBase,
    private val noteThreadDAO: NoteThreadDAO,
) : NoteDataSource {

    private var listeners = setOf<NoteDataSource.Listener>()
    private val listenersLock = Mutex()

    private val lock = Mutex()
    private var deleteNoteIds = mutableSetOf<Note.Id>()

    private val clearStorageLock = Mutex()

    override fun addEventListener(listener: NoteDataSource.Listener): Unit = runBlocking {
        listeners = listeners.toMutableSet().apply {
            add(listener)
        }
    }

    override suspend fun getIn(noteIds: List<Note.Id>): Result<List<Note>> =
        runCancellableCatching {
            val ids = noteIds.map {
                NoteEntity.makeEntityId(it)
            }.distinct()
            if (ids.isEmpty()) {
                return@runCancellableCatching emptyList()
            }
            val entities = withContext(ioDispatcher) {
                noteDAO.getIn(ids)
            }
            withContext(defaultDispatcher) {
                entities.map {
                    it.toModel()
                }
            }
        }

    override suspend fun get(noteId: Note.Id): Result<Note> = runCancellableCatching {
        if (deleteNoteIds.contains(noteId)) {
            return Result.failure(Exception("Note is deleted"))
        }
        val entity = withContext(ioDispatcher) {
            noteDAO.get(NoteEntity.makeEntityId(noteId))
        }
        entity?.toModel() ?: throw NoteNotFoundException(noteId)
    }

    override suspend fun getWithState(noteId: Note.Id): Result<NoteResult> =
        runCancellableCatching {
            if (deleteNoteIds.contains(noteId)) {
                return@runCancellableCatching NoteResult.Deleted
            }
            val entity = withContext(ioDispatcher) {
                noteDAO.get(NoteEntity.makeEntityId(noteId))
            }
            when (entity) {
                null -> NoteResult.NotFound
                else -> NoteResult.Success(entity.toModel())
            }
        }

    override suspend fun findByReplyId(id: Note.Id): Result<List<Note>> = runCancellableCatching {
        val entities = withContext(ioDispatcher) {
            noteDAO.findByReplyId(id.accountId, id.noteId)
        }
        withContext(defaultDispatcher) {
            entities.map {
                it.toModel()
            }
        }
    }

    override suspend fun exists(noteId: Note.Id): Boolean {
        return runCancellableCatching {
            val entity = withContext(ioDispatcher) {
                noteDAO.get(NoteEntity.makeEntityId(noteId))
            }
            entity != null
        }.getOrDefault(false)
    }

    override suspend fun delete(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        val entityId = NoteEntity.makeEntityId(noteId)
        val count = withContext(ioDispatcher) {
            noteDAO.count(entityId)
        }
        withContext(ioDispatcher) {
            noteDAO.delete(entityId)
        }
        lock.withLock {
            deleteNoteIds.add(noteId)
        }
        if (count > 0) {
            publish(NoteDataSource.Event.Deleted(noteId))
        }

        count > 0
    }

    @Transaction
    override suspend fun add(note: Note): Result<AddResult> = runCancellableCatching {
        val relationEntity = NoteWithRelation.fromModel(note)
        // exists check
        val existsEntity = withContext(ioDispatcher) {
            noteDAO.get(relationEntity.note.id)
        }
        val entity = relationEntity.note
        val needInsert = existsEntity == null
        withContext(ioDispatcher) {
            database.withTransaction {
                if (existsEntity == null) {
                    noteDAO.insert(entity)
                } else {
                    noteDAO.update(entity)
                }
                when (note.type) {
                    is Note.Type.Mastodon -> {
                        if (needInsert) {
                            relationEntity.mastodonMentions?.let {
                                noteDAO.insertMastodonMentions(
                                    it
                                )
                            }
                            relationEntity.mastodonTags?.let {
                                noteDAO.insertMastodonTags(
                                    it
                                )
                            }
                        }
                    }

                    is Note.Type.Misskey -> Unit
                }

                if (needInsert) {
                    relationEntity.noteFiles?.let {
                        noteDAO.insertNoteFiles(it)
                    }
                    relationEntity.visibleUserIds?.let {
                        noteDAO.insertVisibleIds(it)
                    }
                }

                replaceReactionCountsIfNeed(
                    relationEntity.reactionCounts?.associateBy { it.reaction } ?: emptyMap(),
                    existsEntity?.reactionCounts?.associateBy { it.reaction } ?: emptyMap()
                )
                replaceCustomEmojisIfNeed(
                    relationEntity.customEmojis?.associateBy { it.id } ?: emptyMap(),
                    existsEntity?.customEmojis?.associateBy { it.id } ?: emptyMap()
                )

                replacePollChoicesIfNeed(
                    relationEntity.pollChoices ?: emptyList(),
                    existsEntity?.pollChoices ?: emptyList()
                )
            }


        }

        if (existsEntity == null) AddResult.Created else AddResult.Updated
    }

    override suspend fun addAll(notes: List<Note>): Result<List<AddResult>> =
        runCancellableCatching {
            val noteRelations = notes.map {
                NoteWithRelation.fromModel(it)
            }

            val reactionCountsMap = noteRelations.mapNotNull {
                it.reactionCounts
            }.flatten().associateBy {
                it.id
            }

            val customEmojis = noteRelations.mapNotNull {
                it.customEmojis
            }.flatten().associateBy {
                it.id
            }

            val pollChoices = noteRelations.mapNotNull {
                it.pollChoices
            }.flatten()

            withContext(ioDispatcher) {
                database.withTransaction {
                    val existsNotes = noteDAO.getIn(
                        notes.map {
                            NoteEntity.makeEntityId(it.id)
                        },
                    ).associateBy {
                        it.note.id
                    }

                    val needInsertNotes = noteRelations.filter {
                        existsNotes[it.note.id] == null
                    }

                    val needUpdateNotes = noteRelations.filter {
                        existsNotes[it.note.id] != null
                    }
                    val existsReactionCountsMap = existsNotes.mapNotNull {
                        it.value.reactionCounts
                    }.flatten().associateBy {
                        it.id
                    }

                    val existsPollChoices = existsNotes.mapNotNull {
                        it.value.pollChoices
                    }.flatten()

                    val mastodonNotes = needInsertNotes.filter {
                        it.note.type == "mastodon"
                    }
                    val mastodonMentions = mastodonNotes.mapNotNull {
                        it.mastodonMentions
                    }.flatten()
                    val mastodonTags = mastodonNotes.mapNotNull {
                        it.mastodonTags
                    }.flatten()
                    val noteFiles = needInsertNotes.mapNotNull {
                        it.noteFiles
                    }.flatten()

                    val existsCustomEmojis = existsNotes.mapNotNull {
                        it.value.customEmojis
                    }.flatten().associateBy {
                        it.id
                    }

                    noteDAO.insertAll(needInsertNotes.map { it.note })
                    needUpdateNotes.forEach {
                        noteDAO.update(it.note)
                    }

                    replaceCustomEmojisIfNeed(customEmojis, existsCustomEmojis)
                    replaceReactionCountsIfNeed(reactionCountsMap, existsReactionCountsMap)
                    replacePollChoicesIfNeed(pollChoices, existsPollChoices)


                    noteDAO.insertMastodonMentions(mastodonMentions)
                    noteDAO.insertMastodonTags(mastodonTags)
                    noteDAO.insertNoteFiles(noteFiles)

                    noteRelations.map {
                        if (existsNotes[it.note.id] == null) AddResult.Created else AddResult.Updated
                    }
                }

            }
        }

    override suspend fun clear(): Result<Unit> {
        return runCancellableCatching {
            clearStorageLock.withLock {
                withContext(ioDispatcher) {
                    noteDAO.clear()
                }
            }
        }
    }

    override suspend fun addNoteThreadContext(
        noteId: Note.Id,
        context: NoteThreadContext
    ): Result<Unit> {
        return runCancellableCatching {
            val entity = NoteThreadEntity(
                id = NoteEntity.makeEntityId(noteId),
                accountId = noteId.accountId,
                targetNoteId = noteId.noteId
            )
            val exists = withContext(ioDispatcher) {
                noteThreadDAO.select(entity.id)
            }
            withContext(ioDispatcher) {
                if (exists == null) {
                    noteThreadDAO.insert(entity)
                } else {
                    noteThreadDAO.update(entity)
                    noteThreadDAO.detachAncestors(entity.id)
                    noteThreadDAO.detachDescendants(entity.id)
                }

                noteThreadDAO.attachAncestors(context.ancestors.map {
                    NoteAncestorEntity(
                        threadId = entity.id,
                        noteId = NoteEntity.makeEntityId(it.id)
                    )
                })
                noteThreadDAO.attachDescendants(context.descendants.map {
                    NoteDescendantEntity(
                        threadId = entity.id,
                        noteId = NoteEntity.makeEntityId(it.id)
                    )
                })
            }
        }
    }

    override suspend fun clearNoteThreadContext(noteId: Note.Id): Result<Unit> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                noteThreadDAO.delete(NoteEntity.makeEntityId(noteId))
            }
        }

    override suspend fun findNoteThreadContext(noteId: Note.Id): Result<NoteThreadContext> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                val entity = noteThreadDAO.selectWithRelation(NoteEntity.makeEntityId(noteId))
                    ?: return@withContext NoteThreadContext(
                        ancestors = emptyList(),
                        descendants = emptyList()
                    )

                NoteThreadContext(
                    ancestors = noteDAO.getIn(entity.ancestors.map { it.noteId })
                        .map { it.toModel() },
                    descendants = noteDAO.getIn(entity.descendants.map { it.noteId })
                        .map { it.toModel() }
                )
            }
        }

    override suspend fun deleteByUserId(userId: User.Id): Result<Int> = runCancellableCatching {
        val count = withContext(ioDispatcher) {
            noteDAO.deleteByUserId(userId.accountId, userId.id)
        }
        count
    }

    override fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>> {
        val ids = noteIds.map {
            NoteEntity.makeEntityId(it)
        }.distinct()
        if (ids.isEmpty()) {
            return flowOf(emptyList())
        }
        return noteDAO.observeByIds(ids).flowOn(ioDispatcher).map { notes ->
            notes.map {
                it.toModel()
            }
        }.flowOn(defaultDispatcher)
    }

    override fun observeOne(noteId: Note.Id): Flow<Note?> {
        return noteDAO.observeById(NoteEntity.makeEntityId(noteId)).flowOn(ioDispatcher).map {
            it?.toModel()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeNoteThreadContext(noteId: Note.Id): Flow<NoteThreadContext?> {
        return noteThreadDAO.observeWithRelation(NoteEntity.makeEntityId(noteId))
            .flowOn(ioDispatcher).flatMapLatest { relation ->
                val ids = (relation?.ancestors?.map { it.noteId }
                    ?: emptyList()) + (relation?.descendants?.map { it.noteId } ?: emptyList())
                combine(noteDAO.observeByIds(ids)) { array ->
                    val map = array.toList().flatten().associateBy {
                        it.note.id
                    }
                    NoteThreadContext(
                        ancestors = (relation?.ancestors ?: emptyList()).mapNotNull {
                            map[it.noteId]?.toModel()
                        },
                        descendants = (relation?.descendants ?: emptyList()).mapNotNull {
                            map[it.noteId]?.toModel()
                        }
                    )
                }

            }
    }

    override suspend fun findLocalCount(): Result<Long> = runCancellableCatching {
        withContext(ioDispatcher) {
            noteDAO.count()
        }
    }

    private fun publish(ev: NoteDataSource.Event) = runBlocking {
        listenersLock.withLock {
            listeners.forEach {
                it.on(ev)
            }
        }
    }

    private suspend fun replaceReactionCountsIfNeed(
        reactionCountsMap: Map<String, ReactionCountEntity>,
        existsNoteReactionCountMap: Map<String, ReactionCountEntity>,
    ) {
        val deleteList = existsNoteReactionCountMap.filter {
            !reactionCountsMap.containsKey(it.key)
        }.values.toList()
        val insertList = reactionCountsMap.filter { (_, value) ->
            value.id !in existsNoteReactionCountMap
        }.values.toList()

        val updateList = reactionCountsMap.filter { (_, value) ->
            value.id in existsNoteReactionCountMap && value != existsNoteReactionCountMap[value.id]
        }.values.toList()

        withContext(ioDispatcher) {
            noteDAO.insertReactionCounts(insertList)
            noteDAO.deleteReactionCounts(deleteList.map { it.id })
            updateList.forEach {
                noteDAO.updateReactionCount(it)
            }
        }

    }

    private suspend fun replaceCustomEmojisIfNeed(
        customEmojis: Map<String, NoteCustomEmojiEntity>,
        existsCustomEmojis: Map<String, NoteCustomEmojiEntity>,
    ) {
        val deleteList = existsCustomEmojis.filter {
            !customEmojis.containsKey(it.key)
        }.values

        val updateOrInsertList = customEmojis.mapNotNull { emoji ->
            val exists = existsCustomEmojis[emoji.key]
            if (exists == null || exists != emoji.value) {
                emoji.value
            } else {
                null
            }
        }

        withContext(ioDispatcher) {
            noteDAO.insertCustomEmojis(updateOrInsertList)
            noteDAO.deleteCustomEmojis(deleteList.map { it.id })
        }
    }

    private suspend fun replacePollChoicesIfNeed(
        pollChoices: List<NotePollChoiceEntity>,
        existsPollChoices: List<NotePollChoiceEntity>,
    ) {
        // deleteの調整は必要ない
        val updateOrInsertList = pollChoices.mapNotNull { choice ->
            val exists = existsPollChoices.find {
                it.id == choice.id
            }
            if (exists == null || exists != choice) {
                choice
            } else {
                null
            }
        }
        noteDAO.insertPollChoices(updateOrInsertList)
    }

}