package jp.panta.misskeyandroidclient.model.account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.MakeDefaultPagesUseCase
import net.pantasystem.milktea.model.account.PageDefaultStringsJp
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class MakeDefaultPagesUseCaseTest {

    lateinit var makeDefaultPagesUseCase: MakeDefaultPagesUseCase
    lateinit var account: Account

    @BeforeEach
    fun setup() {
        makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp(),
            nodeInfoRepository = object : NodeInfoRepository {
                override suspend fun find(host: String): Result<NodeInfo> = Result.success(get(host))

                override fun get(host: String): NodeInfo {
                    return NodeInfo(
                        host = "", version = "", software = NodeInfo.Software(
                            name = "misskey",
                            version = ""
                        )
                    )
                }

                override fun observe(host: String): Flow<NodeInfo?> = flowOf()
                override suspend fun sync(host: String): Result<Unit> = Result.success(Unit)
                override suspend fun syncAll(): Result<Unit> = Result.success(Unit)
            }
        )
        account = Account(
            "remoteId",
            "https://misskey.io",
            "",
            instanceType = Account.InstanceType.MISSKEY,
            "",
        )
    }

    @Test
    fun disableGlobalTimeline() {
        val meta = Meta(
            "",
            disableGlobalTimeline = true,
            disableLocalTimeline = false,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(3, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.SOCIAL, PageType.SOCIAL, PageType.HOME).sorted(), types)
    }

    @Test
    fun disableLocalTimeline() {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = true,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(3, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.GLOBAL, PageType.HOME, PageType.HOME).sorted(), types)
    }

    @Test
    fun onlyEnableHomeTimeline() {
        val meta = Meta(
            "",
            disableGlobalTimeline = true,
            disableLocalTimeline = true,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(2, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.HOME, PageType.HOME), types)
    }

    @Test
    fun enabledAll() {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = false,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(4, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.HOME, PageType.SOCIAL, PageType.GLOBAL, PageType.SOCIAL).sorted(), types)
    }

    @Test
    fun weightIncrementedByOrder() {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = false,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        pages.forEachIndexed { index, page ->
            assertEquals(index, page.weight)
        }
    }
}