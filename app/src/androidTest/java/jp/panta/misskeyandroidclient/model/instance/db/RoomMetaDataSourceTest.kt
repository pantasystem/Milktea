package jp.panta.misskeyandroidclient.model.instance.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.instance.db.RoomMetaDataSource
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaDataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RoomMetaDataSourceTest {

    private lateinit var metaRepository: MetaDataSource

    private lateinit var database: DataBase

    private lateinit var sampleMeta: Meta
    private lateinit var emojis: List<Emoji>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()

        metaRepository = RoomMetaDataSource(database.metaDAO(), database.emojiAliasDAO(), database)

        emojis = listOf(
            Emoji("wakaru"),
            Emoji("kawaii"),
            Emoji("ai"),
            Emoji("test"),
            Emoji("nemui"),
            Emoji("hoge")
        )
        sampleMeta = Meta(
            bannerUrl = "https://hogehoge.io/hogehoge.jpg",
            cacheRemoteFiles = true,
            description = "hogehogeTest",
            disableGlobalTimeline = false,
            disableLocalTimeline = false,
            disableRegistration = false,
            driveCapacityPerLocalUserMb = 1000,
            driveCapacityPerRemoteUserMb = 2000,
            enableDiscordIntegration = true,
            enableEmail = false,
            enableEmojiReaction = true,
            enableGithubIntegration = true,
            enableRecaptcha = true,
            enableServiceWorker = true,
            enableTwitterIntegration = true,
            errorImageUrl = "https://error.img",
            feedbackUrl = "https://feedback.com",
            iconUrl = "https://favicon.png",
            maintainerEmail = "",
            maintainerName = "",
            mascotImageUrl = "",
            maxNoteTextLength = 500,
            name = "",
            recaptchaSiteKey = "key",
            secure = true,
            swPublicKey = "swPublicKey",
            toSUrl = "toSUrl",
            version = "12.0.1",
            emojis = emojis,
            uri = "https://test.misskey.io"
        )

    }

    @Test
    fun addAndGetMetaTest() {
        runBlocking {
            val added = metaRepository.add(sampleMeta)
            val got = metaRepository.get(added.uri)
            assertNotNull(got)
            assertEquals(added.copy(emojis = emptyList()), got?.copy(emojis = emptyList()))
            assertEquals(added.emojis?.size!!, got?.emojis?.size!!)
            assertEquals(
                added.emojis?.map { it.name }?.toSet(),
                got.emojis?.map { it.name }?.toSet()
            )
        }
    }

    @Test
    fun addMetaCheckEmojisDiff() {
        runBlocking {
            val newEmojis = emojis.subList(0, 3)
            val newMeta = sampleMeta.copy(emojis = newEmojis)
            metaRepository.add(newMeta)
            val updatedMeta = metaRepository.get(newMeta.uri)
            assertEquals(
                newEmojis.map { it.name }.toSet(),
                updatedMeta?.emojis?.map { it.name }?.toSet()
            )
        }

    }

    @Test
    fun doubleAddTest() {
        val emojis = sampleMeta.emojis ?: emptyList()
        val arrayList = ArrayList<Emoji>(emojis)
        arrayList.add(Emoji("added"))
        val updated = sampleMeta.copy(emojis = arrayList)
        runBlocking {
            val added = metaRepository.add(updated)
            val got = metaRepository.get(added.uri)
            assertNotNull(got)
            assertEquals(added.copy(emojis = emptyList()), got?.copy(emojis = emptyList()))
            assertEquals(added.emojis?.size!!, got?.emojis?.size!!)

        }
    }
}