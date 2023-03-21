package jp.panta.misskeyandroidclient.model.account

import net.pantasystem.milktea.model.account.Account
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class AccountTest {


    @Test
    fun testGetInstanceDomainWhenHttps() {
        val account = Account(
            instanceDomain = "https://example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("example.com", account.getHost())
    }

    @Test
    fun testGetInstanceDomainWhenHttp() {
        val account = Account(
            instanceDomain = "http://example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("example.com", account.getHost())
    }

    @Test
    fun testGetInstanceDomainWhenSchemaLess() {
        val account = Account(
            instanceDomain = "example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("example.com", account.getHost())
    }


    @Test
    fun getNormalizedInstanceDomain_GiveNormal() {
        val account = Account(
            instanceDomain = "https://example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveHasBackSlash() {
        val account = Account(
            instanceDomain = "https://example.com/",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveHasPortNumber() {
        val account = Account(
            instanceDomain = "https://example.com:8080/",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://example.com:8080", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveSchemaLess() {
        val account = Account(
            instanceDomain = "example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveHasHyphen() {
        val account = Account(
            instanceDomain = "https://test-example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://test-example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveHasUnderScore() {
        val account = Account(
            instanceDomain = "https://test_example.com",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://test_example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_ManyBackSlash() {
        val account = Account(
            instanceDomain = "https://////////////////////test_example.com ",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://test_example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_HasWww() {
        val account = Account(
            instanceDomain = "https://www.example.com ",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://www.example.com", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveSchemaLess2() {
        val account = Account(
            instanceDomain = "//www.example.com ",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://www.example.com", account.normalizedInstanceUri)
    }


    @Test
    fun getNormalizedInstanceDomain_GiveIllegalFormat() {
        val account = Account(
            instanceDomain = "http://",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("http://", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveIpAddress() {
        val account = Account(
            instanceDomain = "http://192.168.0.1",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("http://192.168.0.1", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveIpV6Address() {
        val account = Account(
            instanceDomain = "http://[2001:db8:85a3::8a2e:370:7334]",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals(
            "http://[2001:db8:85a3::8a2e:370:7334]",
            account.normalizedInstanceUri
        )
    }

    @Test
    fun getNormalizedInstanceDomain_OtherProtocol() {
        val account = Account(
            instanceDomain = "ftp://[2001:db8:85a3::8a2e:370:7334]",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals(
            "ftp://[2001:db8:85a3::8a2e:370:7334]",
            account.normalizedInstanceUri
        )

    }

    @Test
    fun getNormalizedInstanceDomain_GiveJapaneseUrl() {
        val account = Account(
            instanceDomain = "https:///みすきー.com:8080/",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://みすきー.com:8080", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveIllegalPattern() {
        val account = Account(
            instanceDomain = "https:::::::::///////みすきー.com:8080////////",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://みすきー.com:8080", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveAcct() {
        val account = Account(
            instanceDomain = "https://@Panta@misskey.io",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://misskey.io", account.normalizedInstanceUri)
    }

    @Test
    fun getNormalizedInstanceDomain_GiveAcctAndIllegalHost() {
        val account = Account(
            instanceDomain = "https://@Panta@MisSkey.io",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://misskey.io", account.normalizedInstanceUri)
    }



    @Test
    fun getNormalizedInstanceDomain_GiveAcctCase2() {
        val account = Account(
            instanceDomain = "https://@artyom24@misskey.io",
            userName = "",
            token = "",
            remoteId = "remoteId",
            instanceType = Account.InstanceType.MISSKEY
        )
        Assertions.assertEquals("https://misskey.io", account.normalizedInstanceUri)
    }

    @Test
    fun getGetHost_GiveSubdomainCase1() {
        val account = Account(
            remoteId = "",
            instanceDomain = "https://mk.iaia.moe",
            userName = "",
            instanceType = Account.InstanceType.MISSKEY,
            token = ""
        )
        Assertions.assertEquals("mk.iaia.moe", account.getHost())
    }
}