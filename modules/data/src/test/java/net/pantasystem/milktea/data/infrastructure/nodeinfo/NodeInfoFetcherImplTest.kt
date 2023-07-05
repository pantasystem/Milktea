package net.pantasystem.milktea.data.infrastructure.nodeinfo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.api.NodeInfoAPIBuilderImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class NodeInfoFetcherImplTest {

    private val loggerFactory = object : Logger.Factory {
        override fun create(tag: String): Logger {
            return object : Logger {
                override val defaultTag: String
                    get() = tag

                override fun debug(msg: String, tag: String, e: Throwable?) = Unit
                override fun debug(tag: String, e: Throwable?, message: () -> String) = Unit
                override fun error(msg: String, e: Throwable?, tag: String) = Unit
                override fun info(msg: String, tag: String, e: Throwable?) = Unit
                override fun warning(msg: String, tag: String, e: Throwable?) = Unit
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetch_GiveMisskeyIO() = runTest {
        val impl = NodeInfoFetcherImpl(NodeInfoAPIBuilderImpl(DefaultOkHttpClientProvider()), loggerFactory)
        assertNotNull(impl.fetch("misskey.io"))
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun fetch_GiveMisskeyDev() = runTest {
//        val impl = NodeInfoFetcherImpl(NodeInfoAPIBuilderImpl(DefaultOkHttpClientProvider()), loggerFactory)
//        assertNotNull(impl.fetch("misskey.dev"))
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetch_GiveMastodonSocial() = runTest {
        val impl = NodeInfoFetcherImpl(NodeInfoAPIBuilderImpl(DefaultOkHttpClientProvider()), loggerFactory)
        assertNotNull(impl.fetch("mastodon.social"))
    }
}