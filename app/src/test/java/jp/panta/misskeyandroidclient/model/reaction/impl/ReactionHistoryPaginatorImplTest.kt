package jp.panta.misskeyandroidclient.model.reaction.impl

//import net.pantasystem.milktea.api.misskey.MisskeyAPIProvider
//import net.pantasystem.milktea.data.api.misskey.RequestReactionHistoryDTO
//import jp.panta.misskeyandroidclient.logger.TestLogger
//import jp.panta.misskeyandroidclient.model.account.Account
//import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
//import jp.panta.misskeyandroidclient.model.notes.Note
//import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistory
//import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
//import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryPaginator
//import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryRequest
//import jp.panta.misskeyandroidclient.model.notes.reaction.impl.ReactionHistoryPaginatorImpl
//import jp.panta.misskeyandroidclient.model.users.impl.InMemoryUserDataSource
//import jp.panta.misskeyandroidclient.util.EncryptionStub
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.runBlocking
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.junit.runners.JUnit4
//
//@RunWith(JUnit4::class)
//class ReactionHistoryPaginatorImplTest {

//    private lateinit var accountRepository: TestAccountRepository
//    private val misskeyAPIProvider = MisskeyAPIProvider()
//    private lateinit var reactionHistoryPaginatorFactory: ReactionHistoryPaginator.Factory
//
//    private lateinit var dataSource: DataSource
//
//
//    class DataSource(
//        var addAllListener: (list: List<ReactionHistory>)-> Unit = {}
//    ) : ReactionHistoryDataSource {
//        override fun findAll(): Flow<List<ReactionHistory>> {
//            TODO("Not yet implemented")
//        }
//
//
//
//        override suspend fun add(reactionHistory: ReactionHistory) {
//        }
//
//        override suspend fun addAll(reactionHistories: List<ReactionHistory>) {
//            addAllListener.invoke(reactionHistories)
//        }
//
//        override suspend fun clear(noteId: Note.Id) {
//        }
//
//        override fun filter(noteId: Note.Id, type: String?): Flow<List<ReactionHistory>> {
//            TODO("Not yet implemented")
//        }
//
//    }
//    @Before
//    fun setUp(): Unit = runBlocking {
//        accountRepository = TestAccountRepository()
//        accountRepository.accounts.clear()
//        dataSource = DataSource()
//        reactionHistoryPaginatorFactory = ReactionHistoryPaginatorImpl.Factory(
//            dataSource,
//            misskeyAPIProvider,
//            accountRepository,
//            EncryptionStub(),
//            InMemoryUserDataSource(TestLogger.Factory())
//        )
//        // TODO テスト後必ずTokenを削除すること
//        val account = Account(
//            remoteId = "",
//            instanceDomain = "https://misskey.io",
//            userName = "Panta",
//            encryptedToken = ""
//        )
//        accountRepository.setCurrentAccount(accountRepository.add(account))
//
//    }
//
//    @Test
//    fun testLoad(): Unit = runBlocking{
//
//        val account = accountRepository.getCurrentAccount().getOrThrow()
//        val req = ReactionHistoryRequest(Note.Id(account.accountId, "7zzafqsm9a"), null)
//        val paginator = reactionHistoryPaginatorFactory.create(req)
//        dataSource.addAllListener = {
//            assertTrue(it.isNotEmpty())
//        }
//
//        paginator.next()
//
//    }
//
//    @Test
//    fun testPaginate(): Unit = runBlocking {
//        val noteId = "7zzafqsm9a"
//        val account = accountRepository.getCurrentAccount().getOrThrow()
//        val req = ReactionHistoryRequest(Note.Id(account.accountId, noteId), null)
//        val paginator = reactionHistoryPaginatorFactory.create(req)
//
//        val histories = mutableListOf<ReactionHistory>()
//        dataSource.addAllListener = {
//            assertTrue(it.isNotEmpty())
//            histories.addAll(it)
//        }
//
//        for(n in 0 until 5) {
//            assertTrue(paginator.next())
//        }
//        val api = misskeyAPIProvider.get(account.instanceDomain)
//        val body = api.reactions(RequestReactionHistoryDTO(i = account.getI(EncryptionStub()), noteId = noteId, limit = 5 * 20, type = null)).body()
//
//        assertNotNull(body)
//        val except = body!!.map {
//            it.id
//        }
//
//        val ids = histories.map {
//            it.id.reactionId
//        }
//        assertEquals(except, ids)
//    }
//}