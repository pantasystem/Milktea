package net.pantasystem.milktea.data.infrastructure.note.draft

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.note.draft.DraftNote
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class DraftNoteRepositoryImplTest {

    @Test
    fun save() = runTest {
        val draftNote = DraftNote(
            accountId = 1,
            text = "text",
            cw = "cw",
            channelId = null,
            draftFiles = null,
            draftPoll = null,
            replyId = null,
            renoteId = null,
            visibility = "public",
            visibleUserIds = null,
        )
        val impl = DraftNoteRepositoryImpl(
            draftNoteDao = mock() {
                onBlocking {
                    insert(any())
                } doReturn 1L
                onBlocking {
                    deleteDraftJunctionFilesByDraftNoteId(any())
                }
                onBlocking {
                    insertFileRefs(any())
                } doReturn emptyList()
                onBlocking {
                    getDraftNote(any(), any())
                } doReturn draftNote
            },
            loggerFactory = object : Logger.Factory {
                override fun create(tag: String): Logger {
                    return mock(
                        stubOnly = true
                    )
                }
            },
            ioDispatcher = Dispatchers.Default,
        )

        impl.save(draftNote)

        verifyBlocking(impl.draftNoteDao) {
            insert(any())
            deleteDraftJunctionFilesByDraftNoteId(any())
            insertFileRefs(any())
            getDraftNote(any(), any())
        }
    }
}