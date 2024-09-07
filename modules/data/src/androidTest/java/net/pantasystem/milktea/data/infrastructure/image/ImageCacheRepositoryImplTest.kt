package net.pantasystem.milktea.data.infrastructure.image

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.model.image.ImageCache
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.days

class ImageCacheRepositoryImplTest {


    lateinit var repository: ImageCacheRepositoryImpl
    lateinit var dao: ImageCacheDAO
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        repository = ImageCacheRepositoryImpl(
            okHttpClientProvider = DefaultOkHttpClientProvider(),
            context = context,
            coroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
            database.imageCacheDAO(),
        )
        dao = database.imageCacheDAO()
    }

    @Test
    fun deleteExpiredCaches() = runBlocking {

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
            dao.upsert(
                ImageCacheEntity.from(it)
            )
        }

        Assert.assertEquals(5, dao.count())

        repository.deleteExpiredCaches()

        Assert.assertEquals(3, dao.count())

        Assert.assertEquals(
            setOf(
                data[2],
                data[3],
                data[4]
            ).map {
                  it.sourceUrl
            },
            dao.findAll().map { it.toModel() }.toSet().map {
                it.sourceUrl
            }
        )
    }
}