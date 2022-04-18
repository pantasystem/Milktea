package jp.panta.misskeyandroidclient.model.instance

import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaCache
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

class MetaCacheTest {

    lateinit var metaCache: MetaCache

    @Before
    fun setup() {
        metaCache = MetaCache()
    }

    @Test
    fun put() {
        runBlocking {
            metaCache.put("https://misskey.io", Meta("https://misskey.io"))
            assertNotNull(metaCache.get("https://misskey.io"))
        }
    }

    @Test
    fun get() {
        assertNull(metaCache.get("https://misskey.io"))
        runBlocking {
            metaCache.put("https://misskey.io", Meta("https://misskey.io"))
        }
        assertNotNull(metaCache.get("https://misskey.io"))
    }
}