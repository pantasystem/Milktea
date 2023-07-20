package net.pantasystem.milktea.data.infrastructure.account.db

import net.pantasystem.milktea.model.account.Account
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class AccountInstanceTypeConverterTest {
    @Test
    fun convert_GiveMisskey() {

        Assertions.assertEquals(
            Account.InstanceType.MISSKEY,
            AccountInstanceTypeConverter().convert("misskey")
        )
    }

    @Test
    fun convert_GiveMastodon() {
        Assertions.assertEquals(
            Account.InstanceType.MASTODON,
            AccountInstanceTypeConverter().convert("mastodon")
        )
    }

    @Test
    fun convert_GivePleroma() {
        Assertions.assertEquals(
            Account.InstanceType.PLEROMA,
            AccountInstanceTypeConverter().convert("pleroma")
        )
    }

    @Test
    fun convert_GiveFirefish() {
        Assertions.assertEquals(
            Account.InstanceType.FIREFISH,
            AccountInstanceTypeConverter().convert("firefish")
        )
    }

    @Test
    fun convert_GiveInstanceTypeMisskey() {
        Assertions.assertEquals(
            "misskey",
            AccountInstanceTypeConverter().convert(Account.InstanceType.MISSKEY)
        )
    }

    @Test
    fun convert_GiveInstanceTypeMastodon() {
        Assertions.assertEquals(
            "mastodon",
            AccountInstanceTypeConverter().convert(Account.InstanceType.MASTODON)
        )
    }

    @Test
    fun convert_GiveInstanceTypePleroma() {
        Assertions.assertEquals(
            "pleroma",
            AccountInstanceTypeConverter().convert(Account.InstanceType.PLEROMA)
        )
    }

    @Test
    fun convert_GiveInstanceTypeFirefish() {
        Assertions.assertEquals(
            "firefish",
            AccountInstanceTypeConverter().convert(Account.InstanceType.FIREFISH)
        )
    }
}