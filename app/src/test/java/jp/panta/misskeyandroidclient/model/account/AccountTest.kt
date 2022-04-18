package jp.panta.misskeyandroidclient.model.account

import junit.framework.TestCase
import net.pantasystem.milktea.model.account.Account

class AccountTest : TestCase() {



    fun testGetInstanceDomainWhenHttps() {
        val account = net.pantasystem.milktea.model.account.Account(
            instanceDomain = "https://example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId",
            instanceType = net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY
        )
        assertEquals("example.com", account.getHost())
    }

    fun testGetInstanceDomainWhenHttp() {
        val account = net.pantasystem.milktea.model.account.Account(
            instanceDomain = "http://example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId",
            instanceType = net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY
        )
        assertEquals("example.com", account.getHost())
    }

    fun testGetInstanceDomainWhenSchemaLess() {
        val account = net.pantasystem.milktea.model.account.Account(
            instanceDomain = "example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId",
            instanceType = net.pantasystem.milktea.model.account.Account.InstanceType.MISSKEY
        )
        assertEquals("example.com", account.getHost())
    }

}