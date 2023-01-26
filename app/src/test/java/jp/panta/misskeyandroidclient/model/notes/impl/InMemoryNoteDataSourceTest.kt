package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.logger.TestLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.data.infrastructure.notes.impl.InMemoryNoteDataSource
import net.pantasystem.milktea.data.infrastructure.toNote
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

        val dto = NoteDTO(
            "",
            Clock.System.now(),
            renoteCount = 0,
            replyCount = 0,
            userId = "hoge",
            user = UserDTO("hoge", "hogeName")
        )
        val note = dto.toNote(
            account, NodeInfo(
                host = "", version = "", software = NodeInfo.Software(
                    name = "misskey",
                    version = ""
                )

            )
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


    @Test
    fun testUpdateNote(): Unit = runBlocking {
        val noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())

        val dto = NoteDTO(
            "note-1",
            Clock.System.now(),
            renoteCount = 0,
            replyCount = 0,
            userId = "hoge",
            user = UserDTO("hoge", "hogeName")
        )
        val note = dto.toNote(
            account, NodeInfo(
                host = "", version = "", software = NodeInfo.Software(
                    name = "misskey",
                    version = ""
                )

            )
        )
        noteDataSource.add(
            note
        )

        val dtoParsed = dto.toNote(
            account, NodeInfo(
                host = "", version = "", software = NodeInfo.Software(
                    name = "misskey",
                    version = ""
                )

            )
        )
        assertEquals(AddResult.Updated, noteDataSource.add(dtoParsed).getOrThrow())

        assertTrue(dtoParsed === noteDataSource.get(dtoParsed.id).getOrThrow())
    }
}