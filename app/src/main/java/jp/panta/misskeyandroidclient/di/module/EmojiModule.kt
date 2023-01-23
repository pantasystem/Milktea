package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.CheckEmojiAndroidImpl
import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.emoji.Utf8EmojiRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.emoji.Utf8EmojisDAO
import net.pantasystem.milktea.model.emoji.UtfEmojiRepository
import net.pantasystem.milktea.model.notes.reaction.CheckEmoji
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EmojiModule {

    @Singleton
    @Provides
    fun provideCheckEmoji(
        utfEmojiRepository: UtfEmojiRepository
    ): CheckEmoji {
        return CheckEmojiAndroidImpl(utfEmojiRepository)
    }

    @Singleton
    @Provides
    fun provideUtf8EmojiRepository(
        coroutineScope: CoroutineScope,
        loggerFactory: Logger.Factory,
        emojisDAO: Utf8EmojisDAO,
    ): UtfEmojiRepository {
        return Utf8EmojiRepositoryImpl(
            coroutineScope = coroutineScope,
            loggerFactory = loggerFactory,
            utf8EmojisDAO = emojisDAO
        )
    }

    @Singleton
    @Provides
    fun provideUtf8EmojiDao(database: DataBase): Utf8EmojisDAO {
        return database.utf8EmojiDAO()
    }
}