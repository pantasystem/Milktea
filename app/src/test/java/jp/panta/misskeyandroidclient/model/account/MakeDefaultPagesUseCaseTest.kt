package jp.panta.misskeyandroidclient.model.account

import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.MakeDefaultPagesUseCase
import net.pantasystem.milktea.model.account.PageDefaultStringsJp
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


class MakeDefaultPagesUseCaseTest {

    @Test
    fun disableGlobalTimeline() = runTest {
        val account = Account(
            "remoteId",
            "https://misskey.io",
            "",
            instanceType = Account.InstanceType.MISSKEY,
            "",
        )
        val meta = Meta(
            "",
            disableGlobalTimeline = true,
            disableLocalTimeline = false,
        )
        val nodeInfo = NodeInfo(
            host = "", version = "", software = NodeInfo.Software(
                name = "misskey",
                version = ""
            )
        )
        val makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp(),
            mock() {
                on {
                    get(any())
                } doReturn nodeInfo
                onBlocking {
                    find(any())
                } doReturn Result.success(nodeInfo)
            },
            mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(meta)
            },
        )
        val pages = makeDefaultPagesUseCase(account)
        assertEquals(3, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.SOCIAL, PageType.SOCIAL, PageType.HOME).sorted(), types)
        assertEquals(
            listOf(
                Pageable.HomeTimeline(),
                Pageable.HybridTimeline(),
                Pageable.HybridTimeline(withFiles = true)
            ),
            pages.map {
                it.pageable()
            }
        )
    }

    @Test
    fun disableLocalTimeline() = runTest {
        val account = Account(
            "remoteId",
            "https://misskey.io",
            "",
            instanceType = Account.InstanceType.MISSKEY,
            "",
        )
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = true,
        )
        val nodeInfo = NodeInfo(
            host = "", version = "", software = NodeInfo.Software(
                name = "misskey",
                version = ""
            )
        )
        val makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp(),
            mock() {
                on {
                    get(any())
                } doReturn nodeInfo
                onBlocking {
                    find(any())
                } doReturn Result.success(nodeInfo)
            },
            mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(meta)
            },
        )

        val pages = makeDefaultPagesUseCase(account)
        assertEquals(3, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.GLOBAL, PageType.HOME, PageType.HOME).sorted(), types)
        assertEquals(
            listOf(
                Pageable.HomeTimeline(),
                Pageable.HomeTimeline(withFiles = true),
                Pageable.GlobalTimeline()
            ),
            pages.map {
                it.pageable()
            }
        )
    }

    @Test
    fun onlyEnableHomeTimeline() = runTest {
        val account = Account(
            "remoteId",
            "https://misskey.io",
            "",
            instanceType = Account.InstanceType.MISSKEY,
            "",
        )
        val meta = Meta(
            "",
            disableGlobalTimeline = true,
            disableLocalTimeline = true,
        )
        val nodeInfo = NodeInfo(
            host = "", version = "", software = NodeInfo.Software(
                name = "misskey",
                version = ""
            )
        )
        val makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp(),
            mock() {
                on {
                    get(any())
                } doReturn nodeInfo
                onBlocking {
                    find(any())
                } doReturn Result.success(nodeInfo)
            },
            mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(meta)
            },
        )


        val pages = makeDefaultPagesUseCase(account)
        assertEquals(2, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.HOME, PageType.HOME), types)
        assertEquals(
            listOf(Pageable.HomeTimeline(), Pageable.HomeTimeline(withFiles = true)),
            pages.map {
                it.pageable()
            }
        )
    }

    @Test
    fun enabledAll() = runTest {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = false,
        )
        val account = Account(
            "remoteId",
            "https://misskey.io",
            "",
            instanceType = Account.InstanceType.MISSKEY,
            "",
        )
        val nodeInfo = NodeInfo(
            host = "", version = "", software = NodeInfo.Software(
                name = "misskey",
                version = ""
            )
        )
        val makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp(),
            mock() {
                on {
                    get(any())
                } doReturn nodeInfo
                onBlocking {
                    find(any())
                } doReturn Result.success(nodeInfo)
            },
            mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(meta)
            },
        )
        val pages = makeDefaultPagesUseCase(account)
        assertEquals(4, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(
            listOf(
                PageType.HOME,
                PageType.SOCIAL,
                PageType.GLOBAL,
                PageType.SOCIAL
            ).sorted(), types
        )
        assertEquals(
            listOf(
                Pageable.HomeTimeline(),
                Pageable.HybridTimeline(),
                Pageable.HybridTimeline(withFiles = true),
                Pageable.GlobalTimeline()
            ),
            pages.map {
                it.pageable()
            }
        )
    }

    @Test
    fun weightIncrementedByOrder() = runTest {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = false,
        )
        val account = Account(
            "remoteId",
            "https://misskey.io",
            "",
            instanceType = Account.InstanceType.MISSKEY,
            "",
        )
        val nodeInfo = NodeInfo(
            host = "", version = "", software = NodeInfo.Software(
                name = "misskey",
                version = ""
            )
        )
        val makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp(),
            mock() {
                on {
                    get(any())
                } doReturn nodeInfo
                onBlocking {
                    find(any())
                } doReturn Result.success(nodeInfo)
            },
            mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(meta)
            },
        )

        val pages = makeDefaultPagesUseCase(account)
        pages.forEachIndexed { index, page ->
            assertEquals(index, page.weight)
        }
    }
}