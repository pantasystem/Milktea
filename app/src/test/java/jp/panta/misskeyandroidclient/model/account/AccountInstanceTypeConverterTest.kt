package jp.panta.misskeyandroidclient.model.account

import net.pantasystem.milktea.data.infrastructure.account.db.AccountInstanceTypeConverter
import net.pantasystem.milktea.model.account.Account
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccountInstanceTypeConverterTest {

    @Test
    fun convertFromEnum() {
        val converter = AccountInstanceTypeConverter()
        assertEquals("misskey", converter.convert(Account.InstanceType.MISSKEY))
        assertEquals("mastodon", converter.convert(Account.InstanceType.MASTODON))
    }

    @Test
    fun convertFromString() {
        val converter = AccountInstanceTypeConverter()
        assertEquals(Account.InstanceType.MASTODON, converter.convert("mastodon"))
        assertEquals(Account.InstanceType.MISSKEY, converter.convert("misskey"))
    }
}