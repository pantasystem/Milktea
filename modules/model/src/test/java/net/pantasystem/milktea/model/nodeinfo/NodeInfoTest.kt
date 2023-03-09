package net.pantasystem.milktea.model.nodeinfo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class NodeInfoTest {

    @Test
    fun type_GiveMisskey() {
        val nodeInfo = NodeInfo(
            version = "2.0",
            host = "example.com",
            software = NodeInfo.Software(
                name = "misskey",
                version = "13.0.0"
            )
        )
        assertEquals(
            NodeInfo.SoftwareType.Misskey.Normal("misskey", "13.0.0"),
            nodeInfo.type
        )
    }

    @Test
    fun type_GiveCalckey() {
        val nodeInfo = NodeInfo(
            version = "2.0",
            host = "example.com",
            software = NodeInfo.Software(
                name = "calckey",
                version = "13.0.0"
            )
        )
        assertEquals(
            NodeInfo.SoftwareType.Misskey.Calckey("calckey", "13.0.0"),
            nodeInfo.type
        )
    }

    @Test
    fun type_GiveFoundkey() {
        val nodeInfo = NodeInfo(
            version = "2.0",
            host = "example.com",
            software = NodeInfo.Software(
                name = "foundkey",
                version = "13.0.0"
            )
        )
        assertEquals(
            NodeInfo.SoftwareType.Misskey.Foundkey("foundkey", "13.0.0"),
            nodeInfo.type
        )
    }

    @Test
    fun type_GiveMeisskey() {
        val nodeInfo = NodeInfo(
            version = "2.0",
            host = "example.com",
            software = NodeInfo.Software(
                name = "meisskey",
                version = "13.0.0"
            )
        )
        assertEquals(
            NodeInfo.SoftwareType.Misskey.Meisskey("meisskey", "13.0.0"),
            nodeInfo.type
        )
    }

    @Test
    fun type_GiveMastodon() {
        val nodeInfo = NodeInfo(
            version = "2.0",
            host = "example.com",
            software = NodeInfo.Software(
                name = "mastodon",
                version = "3.0"
            )
        )
        assertEquals(
            NodeInfo.SoftwareType.Mastodon.Normal("mastodon", "3.0"),
            nodeInfo.type
        )
    }

    @Test
    fun type_GiveFedibird() {
        val nodeInfo = NodeInfo(
            version = "2.0",
            host = "example.com",
            software = NodeInfo.Software(
                name = "fedibird",
                version = "3.0"
            )
        )
        assertEquals(
            NodeInfo.SoftwareType.Mastodon.Fedibird("fedibird", "3.0"),
            nodeInfo.type
        )
    }
}