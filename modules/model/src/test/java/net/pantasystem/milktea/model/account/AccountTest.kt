package net.pantasystem.milktea.model.account

import org.junit.Assert
import org.junit.Test

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
        Assert.assertEquals("https://misskey.pantasystem.com", account.normalizedInstanceDomain)
    }
}