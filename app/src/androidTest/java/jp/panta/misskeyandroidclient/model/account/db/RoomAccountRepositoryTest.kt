package jp.panta.misskeyandroidclient.model.account.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.IllegalStateException

class RoomAccountRepositoryTest{

    private lateinit var database: DataBase

    private lateinit var roomAccountRepository: RoomAccountRepository

    private lateinit var accountDAO: AccountDAO

    @Before
    fun setupRepository(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        roomAccountRepository = RoomAccountRepository(database.accountDAO(), database.pageDAO(), context.getSharedPreferences("test", Context.MODE_PRIVATE))
        accountDAO = database.accountDAO()
    }

    @Test
    fun addAccountTest(){
        val remoteId = "hogehoge"
        val instanceDomain = "http://misskey.io"
        val userName = "Panta"
        val account = Account(remoteId, instanceDomain, userName, "hogehogehoge")
        runBlocking {
            val result = roomAccountRepository.add(account)

            assertEquals(result.userName, account.userName)
            assertEquals(result.instanceDomain, account.instanceDomain)
            assertNotEquals(result.accountId, 0)
            assertEquals(result.accountId, 1)
            assert(result.accountId < 0)
            println(result)

            val account2 = Account(remoteId, instanceDomain, "Test", "hogehogehoge")

            val result2 = roomAccountRepository.add(account2)
            assertEquals(result2.userName, "Test")
            assertEquals(result2.instanceDomain, account.instanceDomain)
            assertNotEquals(result2.accountId, 0)
            assertEquals(result2.accountId, 2)
            assert(result2.accountId < 0)
            println(result2)
        }

    }

    @Test
    fun addAccountUpdateTest(){
        val remoteId = "hogehoge"
        val instanceDomain = "http://misskey.io"
        val userName = "Panta"
        val account = Account(remoteId, instanceDomain, userName, "hogehogehoge")
        runBlocking {
            val result = roomAccountRepository.add(account)

            val updated = result.copy(pages = listOf(Page(result.accountId, "hoge", 0, Pageable.Favorite)))
            assertEquals(result.accountId, updated.accountId)
            assertEquals(updated.accountId, 1)
            val updatedResult = roomAccountRepository.add(updated, true)
            assertEquals(updatedResult.pages.size, 1)

            val getResult = roomAccountRepository.get(updatedResult.accountId)
            assertEquals(getResult.pages.size, 1)

            assertNotEquals(getResult.pages.first().pageId, 0)
        }
    }

    @Test
    fun insertAccountTest(){
        val remoteId = "hogehoge"
        val instanceDomain = "http://misskey.io"
        val userName = "Panta"
        val account = Account(remoteId, instanceDomain, userName, "hogehogehoge")
        runBlocking {
            val resultId = accountDAO.insert(account)
            assertNotEquals(resultId, 0)
            val result = accountDAO.get(resultId)!!
            assertEquals(result.userName, account.userName)
            assertEquals(result.instanceDomain, account.instanceDomain)
            assertNotEquals(result.accountId, 0)
            assertEquals(result.accountId, 1)
            assert(result.accountId < 0)
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