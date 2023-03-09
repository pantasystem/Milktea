package net.pantasystem.milktea.data.infrastructure.nodeinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.api.activitypub.NodeInfoDTO
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoDao
import net.pantasystem.milktea.data.infrastructure.nodeinfo.db.NodeInfoRecord
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class NodeInfoRepositoryImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun find_WhenHasCache() = runTest {
        val cache = NodeInfoCache()
        val nodeInfo = NodeInfo("misskey.io", "", NodeInfo.Software("", ""))
        cache.put("misskey.io", nodeInfo)
        val impl = NodeInfoRepositoryImpl(
            fetcher = mock(),
            cache = cache,
            nodeInfoDao = mock(),
            ioDispatcher = Dispatchers.Default
        )
        assertEquals(nodeInfo, impl.find("misskey.io").getOrThrow())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun find_WhenHasDiskCache() = runTest {
        val nodeInfo = NodeInfo("misskey.io", "", NodeInfo.Software("", ""))
        val cache = NodeInfoCache()
        assertNull(cache.get("misskey.io"))
        val impl = NodeInfoRepositoryImpl(
            fetcher = mock(),
            cache = cache,
            nodeInfoDao = mock() {
                onBlocking {
                    find(any())
                } doReturn NodeInfoRecord("misskey.io", "", "", "")
            },
            ioDispatcher = Dispatchers.Default
        )
        assertEquals(nodeInfo, impl.find("misskey.io").getOrThrow())
        assertEquals(nodeInfo, cache.get("misskey.io"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun find_WhenNoCache() = runTest {
        val nodeInfo = NodeInfo("misskey.io", "", NodeInfo.Software("", ""))
        val cache = NodeInfoCache()
        assertNull(cache.get("misskey.io"))

        val nodeInfoDao: NodeInfoDao = mock() {
            onBlocking {
                find(any())
            } doReturn null

            onBlocking {
                insert(any())
            } doReturn 1

            onBlocking {
                update(any())
            }
        }
        val impl = NodeInfoRepositoryImpl(
            fetcher = object : NodeInfoFetcher {
                override suspend fun fetch(host: String): NodeInfoDTO {
                    return NodeInfoDTO("", NodeInfoDTO.SoftwareDTO("", ""))
                }
            },
            cache = cache,
            nodeInfoDao = nodeInfoDao,
            ioDispatcher = Dispatchers.Default
        )
        assertEquals(nodeInfo, impl.find("misskey.io").getOrThrow())
        assertEquals(nodeInfo, cache.get("misskey.io"))

    }

}