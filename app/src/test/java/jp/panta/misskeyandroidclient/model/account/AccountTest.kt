package jp.panta.misskeyandroidclient.model.account

import junit.framework.TestCase
import net.pantasystem.milktea.data.model.account.Account

class AccountTest : TestCase() {



    fun testGetInstanceDomainWhenHttps() {
        val account = Account(
            instanceDomain = "https://example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        assertEquals("example.com", account.getHost())
    }

    fun testGetInstanceDomainWhenHttp() {
        val account = Account(
            instanceDomain = "http://example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        assertEquals("example.com", account.getHost())
    }

    fun testGetInstanceDomainWhenSchemaLess() {
        val account = Account(
            instanceDomain = "example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        assertEquals("example.com", account.getHost())
    }

}