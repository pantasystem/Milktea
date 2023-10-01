package jp.panta.misskeyandroidclient.model.instance.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.instance.db.RoomMetaDataSource
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaDataSource
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RoomMetaDataSourceTest {

    private lateinit var metaRepository: MetaDataSource

    private lateinit var database: DataBase

    private lateinit var sampleMeta: Meta

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()

        metaRepository = RoomMetaDataSource(database.metaDAO(), database)


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
            uri = "https://test.misskey.io"
        )

    }

    @Test
    fun addAndGetMetaTest() {
        runBlocking {
            val added = metaRepository.add(sampleMeta)
            val got = metaRepository.get(added.uri)
            assertNotNull(got)

        }
    }




}