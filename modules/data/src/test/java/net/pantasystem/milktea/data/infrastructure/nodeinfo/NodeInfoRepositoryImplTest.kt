package net.pantasystem.milktea.data.infrastructure.nodeinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilder
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilderImpl
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
            nodeInfoAPIBuilder = mock(),
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
            nodeInfoAPIBuilder = mock(),
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


}