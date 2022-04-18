package jp.panta.misskeyandroidclient.model.account

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountInstanceTypeConverter
import org.junit.Assert.*
import org.junit.Test

class AccountInstanceTypeConverterTest {

    @Test
    fun convertFromEnum() {
        val converter = net.pantasystem.milktea.model.account.AccountInstanceTypeConverter()
        assertEquals("misskey", converter.convert(net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY))
        assertEquals("mastodon", converter.convert(net.pantasystem.milktea.model.account.Account.InstanceType.MASTODON))
    }

    @Test
    fun convertFromString() {
        val converter = net.pantasystem.milktea.model.account.AccountInstanceTypeConverter()
        assertEquals(net.pantasystem.milktea.model.account.Account.InstanceType.MASTODON, converter.convert("mastodon"))
        assertEquals(net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY, converter.convert("misskey"))
    }
}