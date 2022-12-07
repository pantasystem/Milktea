package jp.panta.misskeyandroidclient.model.account.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.account.db.AccountDAO
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.data.infrastructure.account.db.RoomAccountRepository
import net.pantasystem.milktea.data.infrastructure.auth.KeyStoreSystemEncryption
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class RoomAccountRepositoryTest {

    private lateinit var database: DataBase

    private lateinit var roomAccountRepository: RoomAccountRepository

    private lateinit var accountDAO: AccountDAO

    private lateinit var encryption: Encryption

    @Before
    fun setupRepository() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        encryption = KeyStoreSystemEncryption(context)

        roomAccountRepository = RoomAccountRepository(
            database,
            context.getSharedPreferences("test", Context.MODE_PRIVATE),
            database.accountDAO(),
            database.pageDAO(),
            encryption = encryption,
        )
        accountDAO = database.accountDAO()
    }

    @Test
    fun addAccountTest() {
        val remoteId = "hogehoge"
        val instanceDomain = "http://misskey.io"
        val userName = "Panta"
        val account = Account(
            remoteId,
            instanceDomain,
            userName,
            Account.InstanceType.MISSKEY,
            "hogehogehoge"
        )
        runBlocking {
            val result = roomAccountRepository.add(account).getOrThrow()

            assertEquals(account.userName, result.userName)
            assertEquals(account.instanceDomain, result.instanceDomain)
            assertNotEquals(result.accountId, 0)
            assertEquals(1, result.accountId)
            println(result)

            val account2 = Account(
                remoteId,
                instanceDomain,
                "Test",
                Account.InstanceType.MISSKEY,
                "hogehogehoge"
            )

            val result2 = roomAccountRepository.add(account2).getOrThrow()
            assertEquals("Test", result2.userName)
            assertEquals(account.instanceDomain, result2.instanceDomain)
            assertEquals(1, result2.accountId)
            assert(result2.accountId > 0)
            println(result2)
        }

    }

    @Test
    fun addAccountUpdateTest() {
        val remoteId = "hogehoge"
        val instanceDomain = "http://misskey.io"
        val userName = "Panta"
        val account = Account(
            remoteId,
            instanceDomain,
            userName,
            Account.InstanceType.MISSKEY,
            "hogehogehoge"
        )
        runBlocking {
            val result = roomAccountRepository.add(account).getOrThrow()

            val updated =
                result.copy(pages = listOf(Page(result.accountId, "hoge", 0, Pageable.Favorite)))
            assertEquals(result.accountId, updated.accountId)
            assertEquals(updated.accountId, 1)
            val updatedResult = roomAccountRepository.add(updated, true).getOrThrow()
            assertEquals(updatedResult.pages.size, 1)

            val getResult = roomAccountRepository.get(updatedResult.accountId).getOrThrow()
            assertEquals(getResult.pages.size, 1)

            assertNotEquals(getResult.pages.first().pageId, 0)
        }
    }

    @Test
    fun insertAccountTest() {
        val remoteId = "hogehoge"
        val instanceDomain = "http://misskey.io"
        val userName = "Panta"
        val account = Account(
            remoteId,
            instanceDomain,
            userName,
            Account.InstanceType.MISSKEY,
            "hogehogehoge"
        )
        runBlocking {
            val resultId = accountDAO.insert(AccountRecord.from(account, encryption))
            assertNotEquals(0, resultId)
            val result = accountDAO.get(resultId)!!
            assertEquals(account.userName, result.userName)
            assertEquals(account.instanceDomain, result.instanceDomain)
            assertNotEquals(0, result.accountId)
            assertEquals(1, result.accountId)
            println(result)

            /*val account2 = Account(remoteId, instanceDomain, "Test", "hogehogehoge")

            val result2 = roomAccountRepository.add(account2)
            assertEquals(result2.userName, "Test")
            assertEquals(result2.instanceDomain, account.instanceDomain)
            assertNotEquals(result2.accountId, 0)
            assertEquals(result2.accountId, 2)
            assert(result2.accountId < 0)
            println(result2)*/
        }
    }
}