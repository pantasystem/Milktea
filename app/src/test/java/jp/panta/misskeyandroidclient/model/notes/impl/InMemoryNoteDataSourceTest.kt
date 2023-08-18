package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.logger.TestLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.data.infrastructure.notes.impl.InMemoryNoteDataSource
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryNoteDataSourceTest {

    private lateinit var loggerFactory: Logger.Factory
    private lateinit var account: Account

    @BeforeEach
    fun setUp() {
        loggerFactory = TestLogger.Factory()
        account = Account(
            remoteId = "piyo",
            instanceDomain = "",
            token = "",
            userName = "piyoName",
            instanceType = Account.InstanceType.MISSKEY
        )
    }

    @Test
    fun testAdd() {
        val noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())

        val note = Note.make(
            Note.Id(0L, ""),
            userId = User.Id(0L, ""),
        )
        runBlocking {
            val result = noteDataSource.add(
                note
            ).getOrThrow()
            delay(10)

            assertEquals(AddResult.Created, result)
            delay(10)

            assertEquals(AddResult.Updated, noteDataSource.add(note).getOrThrow())
            delay(10)


        }

    }



}