package jp.panta.misskeyandroidclient.model.notes.draft

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountDao
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DraftNoteDaoTest{

    lateinit var draftNoteDao: DraftNoteDao
    lateinit var accountDao: AccountDao

    @Before
    fun setUp(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db  = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        draftNoteDao = db.draftNoteDao()
        accountDao = db.accountDao()
    }

    @Test
    fun writeAndReadTest(){
        val testAccountId = "PantaIsCute"

        val draftNoteTestText = "Hello world!!"

        val draftNoteTestChoices = listOf("A", "B", "C", "D")

        val visibilityUserIds = listOf("AB", "CD", "EF", "GH")

        accountDao.insert(Account(testAccountId))

        val id = draftNoteDao.fullInsert(
            DraftNote(
                accountId = testAccountId,
                text = draftNoteTestText,
                draftPoll = DraftPoll(
                    draftNoteTestChoices,
                    false,
                    null
                ),
                visibleUserIds = visibilityUserIds
            )
        )

        val savedDraftNotes = draftNoteDao.findDraftNotesByAccount(testAccountId)
        assertEquals(savedDraftNotes.isNotEmpty(), true)
        assertEquals(savedDraftNotes.size, 1)

        val savedDraftNote = savedDraftNotes.first()
        assertEquals(savedDraftNote.accountId, testAccountId)
        assertEquals(savedDraftNote.text, draftNoteTestText)
        assertEquals(savedDraftNote.draftPoll?.choices?.size, draftNoteTestChoices.size)
        assertEquals(visibilityUserIds, savedDraftNote.visibleUserIds)

        val searched = draftNoteDao.searchDraftNotes(testAccountId, "ello")
        assertEquals(searched.size, 1)
        val searchedNote = searched.first()
        assertEquals(searchedNote.text, draftNoteTestText)


        val draftNote = draftNoteDao.getDraftNote(testAccountId, id!!)!!
        assertEquals(draftNote.text, draftNoteTestText)

        draftNoteDao.deleteDraftNote(draftNote)

        val afterDeletedDraftNote = draftNoteDao.getDraftNote(testAccountId, id)

        assertEquals(afterDeletedDraftNote, null)

        val afterDeletedDraftNotes = draftNoteDao.findDraftNotesByAccount(testAccountId)
        assertEquals(afterDeletedDraftNotes.isEmpty(), true)


    }


}