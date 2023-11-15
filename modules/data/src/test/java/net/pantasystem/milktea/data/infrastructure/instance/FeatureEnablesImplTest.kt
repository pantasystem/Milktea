package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class FeatureEnablesImplTest {

    private val v11Meta = Meta(
        uri = "https://misskey.dev",
        version = "11.37.1-20230209223723"
    )

    private val v12Meta = Meta(
        uri = "https://example.com",
        version = "12"
    )

    private val v1274Meta = Meta(
        uri = "https://example.com",
        version = "12.74.9"
    )

    private val v1275Meta = Meta(
        uri = "https://example.com",
        version = "12.75.0"
    )

    private val v1361Meta = Meta(
        uri = "https://example.com",
        version = "13.6.1"
    )

    private val v1362Meta = Meta(
        uri = "https://example.com",
        version = "13.6.2"
    )

    private val calckeyV13Meta = Meta(
        uri = "https://example.com",
        version = "13.6.2"
    )

    @Test
    fun enableFeatures_GiveMisskeyAndV11(): Unit = runBlocking {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        v11Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "misskey.dev",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "misskey",
                            version = "11.37.1-20230209223723"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://misskey.dev")
        Assertions.assertEquals(
            setOf(
                FeatureType.Messaging,
                FeatureType.Drive,
                FeatureType.Group,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveMisskeyV12() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        v12Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "misskey",
                            version = "12"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Group,
                FeatureType.Drive,
                FeatureType.Messaging,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveMisskeyV12_74_0() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        v1274Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "misskey",
                            version = "12.74.9"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Group,
                FeatureType.Drive,
                FeatureType.Messaging,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveMisskeyV12_75_0() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        v1275Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "misskey",
                            version = "12.75.0"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Group,
                FeatureType.Drive,
                FeatureType.Messaging,
                FeatureType.Gallery,
                FeatureType.Antenna,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveMisskeyV13_6_1() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        v1361Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "misskey",
                            version = "13.6.1"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Group,
                FeatureType.Drive,
                FeatureType.Messaging,
                FeatureType.Gallery,
                FeatureType.Antenna,
                FeatureType.ReactionAcceptance,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveMisskeyV13_6_2() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        v1362Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "misskey",
                            version = "13.6.2"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Drive,
                FeatureType.Gallery,
                FeatureType.Antenna,
                FeatureType.ReactionAcceptance,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveCalckeyV13() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Misskey(
                        NodeInfo("", "", NodeInfo.Software("misskey", "")),
                        calckeyV13Meta
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "calckey",
                            version = "13.8.0"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Drive,
                FeatureType.Gallery,
                FeatureType.Antenna,
                FeatureType.Group,
                FeatureType.Messaging,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveMastodon() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Mastodon(
                        NodeInfo(
                            host = "example.com",
                            version = "2.0",
                            software = NodeInfo.Software(
                                name = "mastodon",
                                version = "3.0.0"
                            )
                        ),
                        MastodonInstanceInfo(
                            "",
                            "",
                            "",
                            "",
                            "",
                            MastodonInstanceInfo.Urls(""),
                            null,
                            null,
                            null
                        )
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "mastodon",
                            version = "3.0.0"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Bookmark
            ),
            result,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun enableFeatures_GiveFirefish() = runTest {
        val impl = FeatureEnablesImpl(
            instanceInfoService = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    InstanceInfoType.Firefish(
                        NodeInfo(
                            host = "example.com",
                            version = "2.0",
                            software = NodeInfo.Software(
                                name = "firefish",
                                version = "1.0.0"
                            )
                        ), Meta("https://example.com", "1.0.0")
                    )
                )
            },
            nodeInfoRepository = mock() {
                onBlocking {
                    find(any())
                } doReturn Result.success(
                    NodeInfo(
                        host = "example.com",
                        version = "2.0",
                        software = NodeInfo.Software(
                            name = "firefish",
                            version = "1.0.0"
                        )
                    )
                )
            },
            ioDispatcher = Dispatchers.Default
        )
        val result = impl.enableFeatures("https://example.com")
        Assertions.assertEquals(
            setOf(
                FeatureType.Channel,
                FeatureType.UserReactionHistory,
                FeatureType.Clip,
                FeatureType.Drive,
                FeatureType.Gallery,
                FeatureType.Antenna,
                FeatureType.Group,
                FeatureType.Messaging,
                FeatureType.PostReactionUsers,
                FeatureType.PostLocalOnlyVisibility,
            ),
            result,
        )
    }

}