package net.pantasystem.milktea.note.detail.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.filter.WordFilterService
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRelationGetter
import net.pantasystem.milktea.model.notes.NoteThreadContext
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import javax.inject.Inject

class NoteDetailNotesFlowBuilder(
    private val cache: PlaneNoteViewDataCache,
    private val noteWordFilterService: WordFilterService,
    private val noteRelationGetter: NoteRelationGetter,
    private val currentAccountWatcher: CurrentAccountWatcher,
    private val noteTranslationStore: NoteTranslationStore,
    private val viewModelScope: CoroutineScope,
    private val noteDataSource: NoteDataSource,
    private val emojiRepository: CustomEmojiRepository,
    private val configRepository: LocalConfigRepository,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    ) {

    class Factory @Inject constructor(
        private val noteWordFilterService: WordFilterService,
        private val noteRelationGetter: NoteRelationGetter,
        private val emojiRepository: CustomEmojiRepository,
        private val configRepository: LocalConfigRepository,
        private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
        private val noteDataSource: NoteDataSource,
        private val noteTranslationStore: NoteTranslationStore,

    ) {
        fun create(
            cache: PlaneNoteViewDataCache,
            currentAccountWatcher: CurrentAccountWatcher,
            scope: CoroutineScope,
        ): NoteDetailNotesFlowBuilder {
            return NoteDetailNotesFlowBuilder(
                cache,
                noteWordFilterService,
                noteRelationGetter,
                currentAccountWatcher,
                noteTranslationStore,
                scope,
                noteDataSource,
                emojiRepository,
                configRepository,
                noteCaptureAPIAdapter,
            )
        }
    }

    fun build(
        show: Pageable.Show,
        noteFlow: Flow<Note?>,
        threadContext: StateFlow<NoteThreadContext>
    ): Flow<List<PlaneNoteViewData>> {
        return combine(noteFlow, threadContext) { note, thread ->
            val relatedConversation = noteRelationGetter.getIn(thread.ancestors.map { it.id }).filterNot {
                noteWordFilterService.isShouldFilterNote(show, it)
            }.map {
                NoteType.Conversation(it)
            }
            val repliesMap = thread.descendants.groupBy {
                it.replyId
            }
            val relatedChildren = noteRelationGetter.getIn((repliesMap[note?.id] ?: emptyList()).map {
                it.id
            }).filterNot {
                noteWordFilterService.isShouldFilterNote(show, it)
            }.map { childNote ->
                NoteType.Children(childNote,
                    noteRelationGetter.getIn(repliesMap[childNote.note.id]?.map { it.id }
                        ?: emptyList())
                )
            }
            val relatedNote =
                noteRelationGetter.getIn(if (note == null) emptyList() else listOf(note.id)).filterNot {
                    noteWordFilterService.isShouldFilterNote(show, it)
                }.map {
                    NoteType.Detail(it)
                }
            relatedConversation + relatedNote + relatedChildren
        }.map { notes ->
            notes.map { note ->
                when (note) {
                    is NoteType.Children -> {
                        NoteConversationViewData(
                            note.note,
                            currentAccountWatcher.getAccount(),
                            noteTranslationStore,
                            viewModelScope,
                            noteDataSource,
                            emojiRepository,
                            configRepository,
                        ).also {
                            it.capture()
                            cache.put(it)
                        }.apply {
                            this.hasConversation.postValue(false)
                            this.conversation.postValue(note.getReplies().map {
                                cache.get(it)
                            })
                        }
                    }
                    is NoteType.Conversation -> {
                        cache.get(note.note)
                    }
                    is NoteType.Detail -> {
                        NoteDetailViewData(
                            note.note,
                            currentAccountWatcher.getAccount(),
                            noteTranslationStore,
                            noteDataSource,
                            configRepository,
                            emojiRepository,
                            viewModelScope,
                        ).also {
                            it.capture()
                            cache.put(it)
                        }
                    }
                }
            }
        }
    }

    private fun <T : PlaneNoteViewData> T.capture(): T {
        val self = this
        self.capture(noteCaptureAPIAdapter) {
            it.launchIn(viewModelScope + Dispatchers.IO)
        }
        return this
    }
}