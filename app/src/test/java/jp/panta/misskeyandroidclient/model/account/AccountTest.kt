package jp.panta.misskeyandroidclient.model.account

import junit.framework.TestCase

class AccountTest : TestCase() {



    fun testGetInstanceDomainWhenHttps() {
        val account = Account(
            instanceDomain = "https://example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId"
        )
        assertEquals("example.com", account.getHost())
    }

    fun testGetInstanceDomainWhenHttp() {
        val account = Account(
            instanceDomain = "http://example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId"
        )
        assertEquals("example.com", account.getHost())
    }

    fun testGetInstanceDomainWhenSchemaLess() {
        val account = Account(
            instanceDomain = "example.com",
            userName = "",
            encryptedToken = "",
            remoteId = "remoteId"
        )
        assertEquals("example.com", account.getHost())
    }

}