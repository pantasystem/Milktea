package jp.panta.misskeyandroidclient.model.account

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.MakeDefaultPagesUseCase
import net.pantasystem.milktea.model.account.PageDefaultStringsJp
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.instance.Meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class MakeDefaultPagesUseCaseTest {

    lateinit var makeDefaultPagesUseCase: MakeDefaultPagesUseCase
    lateinit var account: Account

    @BeforeEach
    fun setup() {
        makeDefaultPagesUseCase = MakeDefaultPagesUseCase(
            PageDefaultStringsJp()
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
        assertEquals(2, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.SOCIAL, PageType.HOME).sorted(), types)
    }

    @Test
    fun disableLocalTimeline() {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = true,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(2, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.GLOBAL, PageType.HOME).sorted(), types)
    }

    @Test
    fun onlyEnableHomeTimeline() {
        val meta = Meta(
            "",
            disableGlobalTimeline = true,
            disableLocalTimeline = true,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(1, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.HOME), types)
    }

    @Test
    fun enabledAll() {
        val meta = Meta(
            "",
            disableGlobalTimeline = false,
            disableLocalTimeline = false,
        )
        val pages = makeDefaultPagesUseCase(account, meta)
        assertEquals(3, pages.size)
        val types = pages.map {
            it.pageParams.type
        }.sorted()
        assertEquals(listOf(PageType.HOME, PageType.SOCIAL, PageType.GLOBAL).sorted(), types)
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