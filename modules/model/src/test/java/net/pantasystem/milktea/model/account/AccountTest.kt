package net.pantasystem.milktea.model.account

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class AccountTest {

    @Test
    fun getNormalizedInstanceDomain() {
        val account = Account(
            remoteId = "abc",
            instanceDomain = "https://misskey.pantasystem.com",
            instanceType = Account.InstanceType.MISSKEY,
            token = "test",
            userName = "Panta"
        )
        Assertions.assertEquals("https://misskey.pantasystem.com", account.normalizedInstanceUri)
    }
}