package net.pantasystem.milktea.data.infrastructure.image

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.data.infrastructure.emoji.MyObjectBox
import net.pantasystem.milktea.model.image.ImageCache
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.days

class ImageCacheRepositoryImplTest {


    lateinit var repository: ImageCacheRepositoryImpl
    lateinit var store: BoxStore
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        store = MyObjectBox.builder().androidContext(context).buildDefault()
        repository = ImageCacheRepositoryImpl(
            boxStore = store,
            okHttpClientProvider = DefaultOkHttpClientProvider(),
            context = context,
            coroutineDispatcher = kotlinx.coroutines.Dispatchers.Default
        )
        store.removeAllObjects()
    }

    @Test
    fun deleteExpiredCaches() = runBlocking {

        val cacheStore = store.boxFor<ImageCacheRecord>()
        val data = listOf(
            ImageCache(
                sourceUrl = "https://example.com/image2.png",
                cachedAt = Clock.System.now() - 8.days,
                cachePath = "path/to/cache2.png",
                width = 100,
                height = 100,
            ),
            ImageCache(
                sourceUrl = "https://example.com/image3.png",
                cachedAt = Clock.System.now() - 7.days,
                cachePath = "path/to/cache3.png",
                width = 100,
                height = 100,
            ),
            ImageCache(
                sourceUrl = "https://example.com/image4.png",
                cachedAt = Clock.System.now() - 6.days,
                cachePath = "path/to/cache4.png",
                width = 100,
                height = 100,
            ),
            ImageCache(
                sourceUrl = "https://example.com/image5.png",
                cachedAt = Clock.System.now() - 6.days,
                cachePath = "path/to/cache5.png",
                width = 100,
                height = 100,
            ),
            ImageCache(
                sourceUrl = "https://example.com/image1.png",
                cachedAt = Clock.System.now() - 6.days,
                cachePath = "path/to/cache1.png",
                width = 100,
                height = 100,
            )
        )
        data.forEach {
            cacheStore.put(
                ImageCacheRecord.from(it)
            )
        }

        Assert.assertEquals(5, cacheStore.count())

        repository.deleteExpiredCaches()

        Assert.assertEquals(3, cacheStore.count())

        Assert.assertEquals(
            setOf(
                data[2],
                data[3],
                data[4]
            ).map {
                  it.sourceUrl
            },
            cacheStore.all.map { it.toModel() }.toSet().map {
                it.sourceUrl
            }
        )
    }
}